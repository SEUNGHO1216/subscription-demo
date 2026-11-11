package com.seaotter

import com.seaotter.core.application.subscription.service.SubscriptionCommandService
import com.seaotter.core.application.subscription.service.SubscriptionScheduler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 서버 여러 대에서 같은 cron 이 동시에 울리는 상황을 스레드로 흉내 내, 스케줄 작업이 한 번만 실행되는지 확인한다.
 * 락 판정은 실제 MySQL 의 shedlock 테이블에서 일어난다.
 */
@SpringBootTest(properties = ["DB_PASSWORD=unused"])
@Testcontainers
class SchedulerLockIntegrationTest {

  companion object {
    @Container
    @ServiceConnection
    @JvmStatic
    val mysql: MySQLContainer<*> = MySQLContainer("mysql:8.0")

    private const val SERVERS = 3
  }

  @Autowired lateinit var scheduler: SubscriptionScheduler
  @Autowired lateinit var jdbcTemplate: JdbcTemplate
  @MockitoSpyBean lateinit var commandService: SubscriptionCommandService

  @BeforeEach
  fun clearLocks() {
    // 행을 지우면 안 된다. ShedLock 은 한 번 만든 락 이름을 기억해 두고 다음부터 UPDATE 만 하므로,
    // 행이 없으면 UPDATE 가 0행이 되어 '다른 서버가 잡고 있다'고 판단한다. 대신 앞 테스트의 락을 만료시킨다.
    jdbcTemplate.update("UPDATE shedlock SET lock_until = CURRENT_TIMESTAMP(3) - INTERVAL 1 SECOND")
    Mockito.clearInvocations(commandService)
  }

  @Test
  fun `서버 여러 대에서 정기결제 스케줄이 동시에 울려도 한 번만 실행된다`() {
    runConcurrently { scheduler.paySubscriptionPeriodically() }

    assertEquals(1, invocationsOf("integratedBillingSubscription"))
    assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM shedlock WHERE name = 'pay-subscription'", Int::class.java))
  }

  @Test
  fun `서버 여러 대에서 송장 발행 스케줄이 동시에 울려도 한 번만 실행된다`() {
    runConcurrently { scheduler.generateInvoicePeriodically() }

    assertEquals(1, invocationsOf("generateInvoice"))
  }

  @Test
  fun `작업이 금방 끝나도 lockAtLeastFor 동안은 다른 서버가 같은 스케줄을 다시 실행하지 못한다`() {
    scheduler.paySubscriptionPeriodically()
    // 시계가 몇 초 늦은 서버가 뒤늦게 같은 cron 을 실행하는 상황
    scheduler.paySubscriptionPeriodically()

    assertEquals(1, invocationsOf("integratedBillingSubscription"))
    val lockedForAtLeast = jdbcTemplate.queryForObject(
      "SELECT TIMESTAMPDIFF(SECOND, locked_at, lock_until) FROM shedlock WHERE name = 'pay-subscription'", Long::class.java
    )!!
    assertTrue(lockedForAtLeast >= 59, "작업이 끝난 뒤에도 락은 locked_at + 1분까지 남아야 한다. 실제: ${lockedForAtLeast}초")
  }

  @Test
  fun `스케줄마다 락이 따로라서 서로 막지 않는다`() {
    scheduler.paySubscriptionPeriodically()
    scheduler.generateInvoicePeriodically()

    assertEquals(1, invocationsOf("integratedBillingSubscription"))
    assertEquals(1, invocationsOf("generateInvoice"))
  }

  private fun invocationsOf(methodName: String): Int =
    Mockito.mockingDetails(commandService).invocations.count { it.method.name == methodName }

  private fun runConcurrently(task: () -> Unit) {
    val pool = Executors.newFixedThreadPool(SERVERS)
    val ready = CountDownLatch(SERVERS)
    val start = CountDownLatch(1)
    try {
      val futures = (1..SERVERS).map {
        pool.submit {
          ready.countDown()
          start.await()
          task()
        }
      }
      ready.await()
      start.countDown()
      futures.forEach { it.get(30, TimeUnit.SECONDS) }
    } finally {
      pool.shutdown()
    }
  }
}
