package com.seaotter

import com.seaotter.core.application.billing.port.BillingKeyPgPort
import com.seaotter.core.application.subscription.port.out.SubscriptionCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceQueryPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionQueryPersistencePort
import com.seaotter.core.application.subscription.service.SubscriptionCommandService
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayCancelResponse
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse
import com.seaotter.core.application.billing.dto.BillingKeyRegisterRequest
import com.seaotter.core.application.billing.dto.BillingKeyRegisterResponse
import com.seaotter.core.domain.subscription.BillingStrategy
import com.seaotter.core.domain.subscription.CycleUnit
import com.seaotter.core.domain.subscription.FlatBillingPolicy
import com.seaotter.core.domain.subscription.InvoiceState
import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.PlanSnapshot
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.core.domain.subscription.SubscriptionPricing
import com.seaotter.core.domain.subscription.SubscriptionStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 여러 인스턴스에서 스케줄러가 같은 시각에 돌 때를 스레드로 흉내 내, 실제 MySQL 에서 확인한다.
 *  - 송장 발행을 동시에 해도 같은 회차 송장은 하나만 생긴다.
 *  - 정기결제를 동시에 돌려도 PG 결제 요청은 송장당 한 번만 나간다.
 *  - PG 응답을 못 받은 송장은 자동으로 다시 결제하지 않는다.
 */
@SpringBootTest(properties = ["DB_PASSWORD=unused"])
@Testcontainers
@Import(BillingConcurrencyIntegrationTest.FakePgConfig::class)
class BillingConcurrencyIntegrationTest {

  companion object {
    @Container
    @ServiceConnection
    @JvmStatic
    val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0")

    private const val THREADS = 8
  }

  @Autowired lateinit var subscriptionCommandService: SubscriptionCommandService
  @Autowired lateinit var subscriptionCommandPort: SubscriptionCommandPersistencePort
  @Autowired lateinit var subscriptionQueryPort: SubscriptionQueryPersistencePort
  @Autowired lateinit var invoiceCommandPort: SubscriptionInvoiceCommandPersistencePort
  @Autowired lateinit var invoiceQueryPort: SubscriptionInvoiceQueryPersistencePort
  @Autowired lateinit var fakePg: FakePg

  private val enterpriseId = "ent-${UUID.randomUUID().toString().take(8)}"
  private val today: LocalDate = LocalDate.now()

  @AfterEach
  fun resetPg() = fakePg.reset()

  @Test
  fun `여러 곳에서 동시에 송장을 발행해도 같은 회차 송장은 하나만 생긴다`() {
    val sub = subscriptionCommandPort.save(activeSubscription(nextBillingAt = today.atTime(9, 0)))

    val summaries = runConcurrently { subscriptionCommandService.generateInvoice(enterpriseId = enterpriseId, date = today) }

    val invoices = invoiceQueryPort.findAllBySubscriptionIdAndPayVersionNullable(enterpriseId, sub.id, null)
    assertEquals(1, invoices.size)
    assertEquals(1, summaries.sumOf { it.issued })
    assertEquals(0, summaries.sumOf { it.failed })
    assertTrue(subscriptionQueryPort.findById(enterpriseId, sub.id)!!.isLockGeneratingInvoice)
  }

  @Test
  fun `여러 곳에서 동시에 정기결제를 돌려도 PG 결제 요청은 송장당 한 번만 나간다`() {
    val (sub, invoice) = dueInvoice()
    fakePg.delayMillis = 300 // PG 응답을 기다리는 사이에 다른 스레드가 끼어들 틈을 넓힌다

    runConcurrently { subscriptionCommandService.integratedBillingSubscription(date = today) }

    assertEquals(1, fakePg.callCount(invoice.id))
    assertEquals(InvoiceStatus.RESOLVED, invoiceQueryPort.findById(enterpriseId, invoice.id)!!.state.status)
    val paid = subscriptionQueryPort.findById(enterpriseId, sub.id)!!
    assertEquals(1L, paid.payVersion)
    assertFalse(paid.isLockGeneratingInvoice)
    assertEquals(today.plusMonths(1).atTime(9, 0), paid.nextBillingAt)
  }

  @Test
  fun `PG 응답을 받지 못한 송장은 처리 중으로 남고 다음 실행에서 자동으로 다시 결제하지 않는다`() {
    val (_, invoice) = dueInvoice()
    fakePg.failWithTimeout += invoice.id

    subscriptionCommandService.integratedBillingSubscription(date = today)
    subscriptionCommandService.integratedBillingSubscription(date = today)

    assertEquals(1, fakePg.callCount(invoice.id))
    assertEquals(InvoiceStatus.PROCESSING, invoiceQueryPort.findById(enterpriseId, invoice.id)!!.state.status)
  }

  @Test
  fun `한 건의 PG 오류가 같은 배치의 다른 결제 결과를 되돌리지 않는다`() {
    val (_, broken) = dueInvoice()
    val (okSub, ok) = dueInvoice()
    fakePg.failWithTimeout += broken.id

    subscriptionCommandService.integratedBillingSubscription(date = today)

    // 이전 구현은 배치 전체가 한 트랜잭션이라, 이미 돈이 나간 결제까지 '미결제'로 롤백됐다
    assertEquals(InvoiceStatus.RESOLVED, invoiceQueryPort.findById(enterpriseId, ok.id)!!.state.status)
    assertEquals(1L, subscriptionQueryPort.findById(enterpriseId, okSub.id)!!.payVersion)
    assertEquals(InvoiceStatus.PROCESSING, invoiceQueryPort.findById(enterpriseId, broken.id)!!.state.status)
  }

  @Test
  fun `같은 구독의 같은 결제 시점 송장은 DB가 두 번째 저장을 거부한다`() {
    val sub = subscriptionCommandPort.save(activeSubscription(nextBillingAt = today.atTime(9, 0)))
    invoiceCommandPort.save(invoiceOf(sub))

    assertThrows<DataIntegrityViolationException> { invoiceCommandPort.save(invoiceOf(sub)) }
  }

  @Test
  fun `먼저 읽은 구독으로 늦게 저장하면 낙관적 락이 거부한다`() {
    val saved = subscriptionCommandPort.save(activeSubscription(nextBillingAt = today.atTime(9, 0)))
    val first = subscriptionQueryPort.findById(enterpriseId, saved.id)!!
    val second = subscriptionQueryPort.findById(enterpriseId, saved.id)!!

    subscriptionCommandPort.save(first.lockGeneratingInvoice())

    assertThrows<OptimisticLockingFailureException> { subscriptionCommandPort.save(second.lockGeneratingInvoice()) }
  }

  // ---- 테스트 데이터 ----

  private fun dueInvoice(): Pair<Subscription, SubscriptionInvoice> {
    val sub = subscriptionCommandPort.save(activeSubscription(nextBillingAt = today.atTime(9, 0)).lockGeneratingInvoice())
    val invoice = invoiceCommandPort.save(invoiceOf(sub))
    return sub to invoice
  }

  private fun activeSubscription(nextBillingAt: LocalDateTime) = Subscription(
    enterpriseId = enterpriseId,
    storeId = "store-1",
    planId = "plan-1",
    planVersion = 1,
    memberId = "member-1",
    billingKey = "bill-key-1",
    subscriptionProductId = "product-1",
    planSnapshot = PlanSnapshot(
      version = 1,
      name = "베이직",
      grade = "BASIC",
      priceInfo = SubscriptionPricing(defaultPrice = BigDecimal.valueOf(10_000)),
      billingPolicy = FlatBillingPolicy(cycle = 1, cycleUnit = CycleUnit.MONTH, billingStrategy = BillingStrategy.POSTPAID),
    ),
    status = SubscriptionStatus.ACTIVE,
    createdAt = nextBillingAt.minusMonths(1),
  ).also { it.nextBillingAt = nextBillingAt }

  private fun invoiceOf(sub: Subscription) = SubscriptionInvoice(
    enterpriseId = sub.enterpriseId,
    subscriptionId = sub.id,
    memberId = sub.memberId,
    storeId = sub.storeId,
    planSnapshot = sub.planSnapshot,
    state = InvoiceState(price = 10_000),
    payVersion = sub.payVersion,
    targetBillingAt = sub.nextBillingAt!!,
    billKey = sub.billingKey,
    lockGeneratingInvoice = false,
  )

  private fun <T> runConcurrently(task: () -> T): List<T> {
    val pool = Executors.newFixedThreadPool(THREADS)
    val start = CountDownLatch(1)
    val futures = (1..THREADS).map { pool.submit<T> { start.await(); task() } }
    start.countDown()
    val results = futures.map { it.get(60, TimeUnit.SECONDS) }
    pool.shutdown()
    return results
  }

  // ---- PG 가짜 구현: 실제 나이스페이먼츠를 호출하지 않고, 송장별 호출 횟수를 센다 ----

  class FakePg : BillingKeyPgPort {
    private val calls = ConcurrentHashMap<String, AtomicInteger>()
    val failWithTimeout: MutableSet<String> = ConcurrentHashMap.newKeySet()
    @Volatile var delayMillis: Long = 0

    fun callCount(invoiceId: String) = calls[invoiceId]?.get() ?: 0

    fun reset() {
      calls.clear()
      failWithTimeout.clear()
      delayMillis = 0
    }

    override fun pay(billingKeyPayRequest: BillingKeyPayRequest): BillingKeyPayResponse {
      val invoiceId = billingKeyPayRequest.subscriptionInvoiceId
      calls.computeIfAbsent(invoiceId) { AtomicInteger() }.incrementAndGet()
      if (delayMillis > 0) Thread.sleep(delayMillis)
      if (invoiceId in failWithTimeout) throw RuntimeException("Read timed out (가짜 PG)")
      return BillingKeyPayResponse(
        billingKey = billingKeyPayRequest.billKey,
        orderId = billingKeyPayRequest.orderId ?: UUID.randomUUID().toString(),
        amount = billingKeyPayRequest.amount,
        resultCode = "3001",
        resultMsg = "카드 결제 성공",
        tid = "fake-tid-${UUID.randomUUID()}",
        authCode = "00000000",
        authDate = "260101000000",
        cardCode = "01",
        cardName = "가짜카드",
        cardNumber = "0000-****-****-0000",
        installment = 0,
        isCheckCard = false,
        canPartiallyCancel = true,
      )
    }

    override fun register(billingKeyRegisterRequest: BillingKeyRegisterRequest): BillingKeyRegisterResponse =
      throw UnsupportedOperationException()

    override fun payCancel(billingKeyPayCancelRequest: BillingKeyPayCancelRequest): BillingKeyPayCancelResponse =
      throw UnsupportedOperationException()
  }

  @TestConfiguration
  class FakePgConfig {
    @Bean
    @Primary
    fun fakePg() = FakePg()
  }
}
