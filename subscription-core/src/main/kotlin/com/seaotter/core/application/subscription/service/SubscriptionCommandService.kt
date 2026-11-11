package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.billing.service.PaymentCommandService
import com.seaotter.core.application.subscription.dto.SubscriptionDTO
import com.seaotter.core.domain.subscription.BillingStrategy
import com.seaotter.core.domain.subscription.SubscriptionBillingPolicy
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.core.domain.subscription.SubscriptionStatus
import com.seaotter.core.domain.subscription.dto.SubscriptionStartRequestDTO
import com.seaotter.core.domain.subscription.service.SubscriptionFactory
import com.seaotter.core.application.subscription.port.out.SubscriptionCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceQueryPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionQueryPersistencePort
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.subscription.InvoiceStatus
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class SubscriptionCommandService(
  private val factory: SubscriptionFactory,
  private val subscriptionCommandPersistencePort: SubscriptionCommandPersistencePort,
  private val subscriptionQueryPersistencePort: SubscriptionQueryPersistencePort,
  private val subscriptionInvoiceQueryPersistencePort: SubscriptionInvoiceQueryPersistencePort,
  private val paymentCommandService: PaymentCommandService,
  private val subscriptionInvoiceIssuer: SubscriptionInvoiceIssuer,
  private val invoiceBillingStep: InvoiceBillingStep,
) {

  private val log = LoggerFactory.getLogger(SubscriptionCommandService::class.java)

  @Transactional
  fun startSubscription(enterpriseId: String, startRequest: SubscriptionStartRequestDTO): SubscriptionDTO {
    try {
      return factory.getStrategy(startRequest.planSnapshot.billingPolicy.billingStrategy)
        .startSubscription(startRequest)
        .run { SubscriptionDTO(this) }
    } catch (e: Exception) {
      throw RuntimeException("startSubscription 에러 : ${e.message}", e)
    }
  }

  @Transactional
  fun terminateSubscription(enterpriseId: String, id: String): SubscriptionDTO {
    try {
      return requireNotNull(subscriptionQueryPersistencePort.findById(enterpriseId, id)) { "해지하려는 구독이 없습니다." }
        .let {
          factory.getStrategy(it.planSnapshot.billingPolicy.billingStrategy)
            .terminateSubscription(it)
            .run { SubscriptionDTO(this) }
        }
    } catch (e: Exception) {
      throw RuntimeException("terminateSubscription 에러 : ${e.message}", e)
    }
  }

  @Transactional
  fun cancelTerminateSubscription(enterpriseId: String, id: String): SubscriptionDTO {
    try {
      return requireNotNull(subscriptionQueryPersistencePort.findById(enterpriseId, id)) { "해지하려는 구독이 없습니다." }
        .let {
          factory.getStrategy(it.planSnapshot.billingPolicy.billingStrategy)
            .cancelTerminateSubscription(it)
            .run { SubscriptionDTO(this) }
        }
    } catch (e: Exception) {
      throw RuntimeException("terminateSubscription 에러 : ${e.message}", e)
    }
  }

  @Transactional
  fun updatePayDate(
    enterpriseId: String,
    id: String,
    newSubscriptionBillingPolicy: SubscriptionBillingPolicy
  ): SubscriptionDTO {
    return requireNotNull(subscriptionQueryPersistencePort.findById(enterpriseId, id)) { "해지하려는 구독이 없습니다." }
      .changeBillingPolicy(newSubscriptionBillingPolicy)
      .also { subscriptionCommandPersistencePort.save(it) }
      .run { SubscriptionDTO(this) }

  }

  @Transactional
  fun toggleActivation(enterpriseId: String, id: String): SubscriptionDTO {
    return requireNotNull(subscriptionQueryPersistencePort.findById(enterpriseId, id)) { "해지하려는 구독이 없습니다." }
      .apply { this.toggleActive() }
      .also { subscriptionCommandPersistencePort.save(it) }
      .run { SubscriptionDTO(this) }
  }

  // 다음 결제일에 다다른 ACTIVE 구독들은 송장을 먼저 만든다
  // 자정에 송장 찍어내는 스케줄러에서 사용
  // 건마다 독립 트랜잭션(SubscriptionInvoiceIssuer)으로 처리하므로 여기에는 트랜잭션을 걸지 않는다.
  fun generateInvoice(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
  ): InvoiceIssueSummary {
    var issued = 0
    var skipped = 0
    var failed = 0
    subscriptionQueryPersistencePort.findAllByNextBillingDateAndStatus(
      enterpriseId = enterpriseId,
      id = id,
      date = date,
      status = SubscriptionStatus.ACTIVE
    ).forEach {
      try {
        if (subscriptionInvoiceIssuer.issue(it.enterpriseId, it.id)) issued++ else skipped++
      } catch (e: OptimisticLockingFailureException) {
        skipped++
        log.info("다른 인스턴스가 먼저 송장을 발행해 건너뜀. enterpriseId={}, subscriptionId={}", it.enterpriseId, it.id)
      } catch (e: DataIntegrityViolationException) {
        skipped++
        log.info("같은 회차 송장이 이미 있어 건너뜀. enterpriseId={}, subscriptionId={}", it.enterpriseId, it.id)
      } catch (e: Exception) {
        failed++
        log.error("송장 발행 실패. enterpriseId={}, subscriptionId={}", it.enterpriseId, it.id, e)
      }
    }
    return InvoiceIssueSummary(issued = issued, skipped = skipped, failed = failed)
      .also { log.info("송장 발행 결과 date={} {}", date, it) }
  }

  // 선불 결제의 경우 다음 결제일까지는 해지해도 사용가능하다
  // 그 해지 요청 상태의 선불건 들을 다음 결제일에 해지 완료하는 스케줄러에서 사용
  @Transactional
  fun terminateCompleteSubscription(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
  ) {
    if (id != null && enterpriseId == null) {
      throw IllegalArgumentException("id 입력 시 enterpriseId 입력도 필수입니다.")
    }
    subscriptionQueryPersistencePort.findAllByNextBillingDateAndStatus(
      enterpriseId = enterpriseId,
      id = id,
      date = date,
      status = SubscriptionStatus.TERMINATE_READY
    )
      .map {
        try {
          if (it.planSnapshot.billingPolicy.billingStrategy == BillingStrategy.PREPAID) {
            it.terminateComplete()
              .run { subscriptionCommandPersistencePort.save(this) }
          }
        } catch (e: Exception) {
          log.error("구독 해지 완료 처리 실패. enterpriseId={}, subscriptionId={}", it.enterpriseId, it.id, e)
        }
      }
  }

  // 타겟 결제일인 송장 + 결제 실패 송장 2주 이내 건 대상으로 결제 스케줄러
  // PG 호출을 트랜잭션 밖에서 하도록 payProcess 가 단계별로 트랜잭션을 나눈다. 여기에는 걸지 않는다.
  fun integratedBillingSubscription(
    enterpriseId: String? = null,
    date: LocalDate,
  ) {

    subscriptionInvoiceQueryPersistencePort.findAllByTargetBillingDateAndStatusDraft_OrPaymentFailedWithin14Days(
      enterpriseId = enterpriseId,
      date = date,  // LocalDate.now()
    ).also { payProcess(it) }

  }

  // 스웨거에서 응급시 타겟팅해서 사용하기 위함
  fun targetBillingSubscription(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
    status: InvoiceStatus,
  ) {
    subscriptionInvoiceQueryPersistencePort.findAllByTargetBillingDateAndStatus(
      enterpriseId = enterpriseId,
      id = id,
      date = date,
      status = status,
    ).also { payProcess(it) }
  }

  /**
   * 송장 한 건씩 '선점 → PG 호출 → 결과 반영' 순서로 결제한다.
   *  - 선점: 송장을 PROCESSING 으로 바꿔 커밋. 다른 인스턴스는 이 송장을 결제하지 않는다.
   *  - PG 호출: 트랜잭션 밖. 응답을 못 받으면(타임아웃 등) 결제가 됐는지 알 수 없으므로
   *    송장을 PROCESSING 으로 남기고 자동 재시도하지 않는다. 재시도하면 이중 결제가 될 수 있다.
   *  - 결과 반영: 송장과 구독 상태를 한 트랜잭션에서 함께 바꾼다.
   */
  fun payProcess(targetInvoiceList: List<SubscriptionInvoice>): BillingSummary {
    var paid = 0
    var payFailed = 0
    var skipped = 0
    var unknown = 0

    targetInvoiceList.forEach { target ->
      val claimed = try {
        invoiceBillingStep.claim(target.enterpriseId, target.id)
      } catch (e: OptimisticLockingFailureException) {
        log.info("다른 인스턴스가 먼저 송장을 선점해 건너뜀. invoiceId={}", target.id)
        null
      } catch (e: Exception) {
        // 선점 전이라 PG 는 호출하지 않았다. 이 건만 건너뛰고 다음 송장을 계속 처리한다.
        log.error("송장 선점 실패로 건너뜀. invoiceId={}", target.id, e)
        null
      }
      if (claimed == null) {
        skipped++
        return@forEach
      }

      val payResult = try {
        paymentCommandService.pay(claimed.enterpriseId, BillingKeyPayRequest(installment = 0, subscriptionInvoice = claimed))
      } catch (e: Exception) {
        unknown++
        log.error(
          "PG 결제 결과를 확인하지 못함. 송장을 PROCESSING 으로 남기고 자동 재시도하지 않음. PG 거래 내역과 대조 필요. invoiceId={}",
          claimed.id, e
        )
        return@forEach
      }

      val succeeded = payResult.resultCode == PAY_SUCCESS_CODE
      try {
        invoiceBillingStep.applyResult(claimed.enterpriseId, claimed.id, succeeded)
        if (succeeded) paid++ else payFailed++
      } catch (e: Exception) {
        unknown++
        log.error(
          "PG 결제는 끝났지만 결과 반영에 실패함. invoiceId={}, resultCode={}, tid={}",
          claimed.id, payResult.resultCode, payResult.tid, e
        )
      }
    }
    return BillingSummary(paid = paid, payFailed = payFailed, skipped = skipped, unknown = unknown)
      .also { log.info("정기결제 결과 {}", it) }
  }

  companion object {
    private const val PAY_SUCCESS_CODE = "3001"
  }
}

data class InvoiceIssueSummary(val issued: Int, val skipped: Int, val failed: Int)

data class BillingSummary(val paid: Int, val payFailed: Int, val skipped: Int, val unknown: Int)

