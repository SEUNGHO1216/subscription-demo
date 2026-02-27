package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.billing.service.PaymentCommandService
import com.seaotter.core.application.subscription.dto.SubscriptionDTO
import com.seaotter.core.application.subscription.dto.SubscriptionKey
import com.seaotter.core.domain.subscription.BillingStrategy
import com.seaotter.core.domain.subscription.InvoiceState
import com.seaotter.core.domain.subscription.SubscriptionBillingPolicy
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.core.domain.subscription.SubscriptionStatus
import com.seaotter.core.domain.subscription.dto.SubscriptionStartRequestDTO
import com.seaotter.core.domain.subscription.service.SubscriptionFactory
import com.seaotter.core.application.subscription.port.out.SubscriptionCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceQueryPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionQueryPersistencePort
import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.Subscription
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class SubscriptionCommandService(
  private val factory: SubscriptionFactory,
  private val subscriptionCommandPersistencePort: SubscriptionCommandPersistencePort,
  private val subscriptionQueryPersistencePort: SubscriptionQueryPersistencePort,
  private val subscriptionInvoiceCommandPersistencePort: SubscriptionInvoiceCommandPersistencePort,
  private val subscriptionInvoiceQueryPersistencePort: SubscriptionInvoiceQueryPersistencePort,
  private val paymentCommandService: PaymentCommandService,
) {

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
  @Transactional
  fun generateInvoice(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
  ) {
    subscriptionQueryPersistencePort.findAllByNextBillingDateAndStatus(
      enterpriseId = enterpriseId,
      id = id,
      date = date,
      status = SubscriptionStatus.ACTIVE
    )
      .map {
        try {
          if (it.nextBillingAt != null) {
            SubscriptionInvoice(
              enterpriseId = it.enterpriseId,
              subscriptionId = it.id,
              memberId = it.memberId,
              storeId = it.storeId,
              payVersion = it.payVersion,
              planSnapshot = it.planSnapshot,
              state = InvoiceState(
                price = getInvoicePrice(it)
              ),
              targetBillingAt = it.nextBillingAt!!,
              billKey = it.billingKey,
              lockGeneratingInvoice = it.isLockGeneratingInvoice
            ).run { subscriptionInvoiceCommandPersistencePort.save(this) }
            subscriptionCommandPersistencePort.save(it.lockGeneratingInvoice())
          }
        } catch (e: Exception) {
          // sentry 500 알림 처리
          println("[로그 대체 필요] 송장 생성 중 에러 발생 : ${e.message}")
        }
      }
  }

  // anchor Days 를 지정한 후불 타입의 구독 첫 결제는 일할 후불 계산이다.
  fun getInvoicePrice(subscription: Subscription): Long {
    val snapshot = subscription.planSnapshot
    if (
      subscription.payVersion == 0L &&
      snapshot.billingPolicy.billingStrategy == BillingStrategy.POSTPAID &&
      !snapshot.billingPolicy.anchorPayDays.isNullOrEmpty()
    ) {
      return _getProrationPrice(subscription)
    }
    return snapshot.billingPolicy.convertToLongTypePrice(
      snapshot.priceInfo.defaultPrice
    )
  }


  private fun _getProrationPrice(subscription: Subscription): Long {
    val start = subscription.createdAt.toLocalDate() // 가입일
    val end = subscription.nextBillingAt?.toLocalDate() // 시점 상 메소드 돌아가는 당일 결제일
    val useDays = ChronoUnit.DAYS.between(start, end)

    val billingPolicy = subscription.planSnapshot.billingPolicy
    val totalTermDays = subscription.planSnapshot.billingPolicy.countDays(subscription.nextBillingAt!!)

    return billingPolicy.convertToLongTypePrice(
      subscription.planSnapshot.priceInfo.defaultPrice.divide(totalTermDays.toBigDecimal(), 10, RoundingMode.HALF_UP)
        .run { this.multiply(useDays.toBigDecimal()) }
    )
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
          // sentry 500 알림 처리
          println("[로그 대체 필요] 구독 해지 완료 중 에러 발생 : ${e.message}")
        }
      }
  }

  // 타겟 결제일인 송장 + 결제 실패 송장 2주 이내 건 대상으로 결제 스케줄러
  @Transactional
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
  @Transactional
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

  fun payProcess(targetInvoiceList: List<SubscriptionInvoice>) {
    val subscriptionMap = _getSubscriptionMap(targetInvoiceList)

    targetInvoiceList
      .forEach {
        try {
          val payResult = paymentCommandService.pay(
            it.enterpriseId,
            BillingKeyPayRequest(
              installment = 0,
              subscriptionInvoice = it,
            )
          )
          val subscription = requireNotNull(subscriptionMap[_mergeKey(it.enterpriseId, it.subscriptionId)]) {
            "엔터프라이즈 : ${it.enterpriseId} / 구독아이디 : ${it.subscriptionId} 에 해당하는 구독이 없습니다."
          }
          when (payResult.resultCode) {
            "3001" -> {
              it.successPay()
              subscription.paySuccess(it.payVersion)
            }

            else -> {
              it.failPay()
              subscription.payFail(it.state.retryCount)
            }
          }
          subscriptionCommandPersistencePort.save(subscription)
          subscriptionInvoiceCommandPersistencePort.save(it)
        } catch (e: Exception) {
          // sentry 500 알림 처리
          println("[로그 대체 필요] 정기 결제 중 에러 발생 : ${e.message}")
        }
      }
  }


  private fun _getSubscriptionMap(invoiceList: List<SubscriptionInvoice>): Map<String, Subscription> {

    return subscriptionQueryPersistencePort.findAllByIdList(
      invoiceList.map { SubscriptionKey(it.enterpriseId, it.subscriptionId) }
    ).associateBy {
      _mergeKey(it.enterpriseId, it.id)
    }
  }

  private fun _mergeKey(enterpriseId: String, subscriptionId: String): String {
    return "${enterpriseId}:${subscriptionId}"
  }


}

