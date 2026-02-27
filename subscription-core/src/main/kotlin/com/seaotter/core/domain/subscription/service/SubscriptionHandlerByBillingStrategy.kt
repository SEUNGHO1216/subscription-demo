package com.seaotter.core.domain.subscription.service

import com.seaotter.core.domain.billing.dto.BillingKeyPayRequest
import com.seaotter.core.domain.billing.dto.BillingKeyPayResponse
import com.seaotter.core.domain.subscription.BillingStrategy
import com.seaotter.core.domain.subscription.InvoiceState
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.core.domain.subscription.client.SubscriptionBillingClient
import com.seaotter.core.domain.subscription.client.SubscriptionCommandPersistenceClient
import com.seaotter.core.domain.subscription.client.SubscriptionInvoicePersistenceClient
import com.seaotter.core.domain.subscription.dto.SubscriptionStartRequestDTO
import org.springframework.stereotype.Component
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

interface SubscriptionHandlerByBillingStrategy {

  fun startSubscription(request: SubscriptionStartRequestDTO): Subscription
  fun terminateSubscription(subscription: Subscription): Subscription
  fun cancelTerminateSubscription(subscription: Subscription): Subscription
}

@Component
class PrepaidSubscriptionStrategy(
  private val billingStrategyHelper: BillingStrategyHelper,
  private val subscriptionCommandPersistenceClient: SubscriptionCommandPersistenceClient,
  private val subscriptionInvoicePersistenceClient: SubscriptionInvoicePersistenceClient,
  private val subscriptionBillingClient: SubscriptionBillingClient
) : SubscriptionHandlerByBillingStrategy {

  override fun startSubscription(request: SubscriptionStartRequestDTO): Subscription {

    // 1. create subscription
    val subscription = billingStrategyHelper.createSubscription(request)
    // 2. invoice
    val invoice = createInvoice(subscription)
    // 3. pay
    val payResult = subscriptionBillingClient.pay(
      enterpriseId = subscription.enterpriseId,
      BillingKeyPayRequest(
        van = request.van,
        installment = request.installment,
        subscriptionInvoice = invoice
      )
    )
    // 4. invoice pay success / invoice pay fail
    billingStrategyHelper.payPostProcess(payResult, invoice, subscription)

    subscriptionCommandPersistenceClient.save(subscription)
    subscriptionInvoicePersistenceClient.save(invoice)
    return subscription

  }

  override fun terminateSubscription(subscription: Subscription): Subscription {
    // 1. terminate request
    return subscriptionCommandPersistenceClient.save(subscription.terminateRequest())

    // 스케줄러가 돌면서
    // 2. terminate complete 처리
  }

  override fun cancelTerminateSubscription(subscription: Subscription): Subscription {
    // 1. 다시 상태값만 바꿔준다
    return subscriptionCommandPersistenceClient.save(subscription.cancelTerminateRequest())
    // 2. invoice 와 payment 는 아직 만들어진게 없으니 그대로 둔다
  }

  fun createInvoice(subscription: Subscription): SubscriptionInvoice {
    val snapshot = subscription.planSnapshot
    return SubscriptionInvoice(
      enterpriseId = subscription.enterpriseId,
      subscriptionId = subscription.id,
      memberId = subscription.memberId,
      storeId = subscription.storeId,
      payVersion = subscription.payVersion,
      planSnapshot = snapshot,
      state = InvoiceState(
        price = _getProrationPrice(subscription)
        // 사용량에 따른 초과금액 부여 없다는 전제하에 지금은 defaultPrice 만 바영
      ),
      // isPrepaidWhenCreate 아규먼트는 '생성 시' 에만 true 가 될 여지가 있다. 선불 결제일시 최초 결제에만 true
      targetBillingAt = subscription.createdAt,
      billKey = subscription.billingKey,
      lockGeneratingInvoice = subscription.isLockGeneratingInvoice
    )
  }

  private fun _getProrationPrice(subscription: Subscription): Long {
    require(subscription.nextBillingAt != null) { "다음 결제일 지정이 되지 않았습니다. 재시도해주세요." }
    if (!subscription.planSnapshot.billingPolicy.anchorPayDays.isNullOrEmpty()) {
      val start = LocalDate.now()
      val end = subscription.nextBillingAt?.toLocalDate()
      val useDays = ChronoUnit.DAYS.between(start, end)

      val billingPolicy = subscription.planSnapshot.billingPolicy
      val totalTermDays = subscription.planSnapshot.billingPolicy.countDays(subscription.nextBillingAt!!)

      return billingPolicy.convertToLongTypePrice(
        subscription.planSnapshot.priceInfo.defaultPrice.divide(totalTermDays.toBigDecimal(), 10, RoundingMode.HALF_UP)
          .run { this.multiply(useDays.toBigDecimal()) }
      )
    }
    return subscription.planSnapshot.billingPolicy.convertToLongTypePrice(
      subscription.planSnapshot.priceInfo.defaultPrice
    )
  }
}

@Component
class PostpaidSubscriptionStrategy(
  private val billingStrategyHelper: BillingStrategyHelper,
  private val subscriptionCommandPersistenceClient: SubscriptionCommandPersistenceClient,
  private val subscriptionInvoicePersistenceClient: SubscriptionInvoicePersistenceClient,
  private val subscriptionBillingClient: SubscriptionBillingClient
) : SubscriptionHandlerByBillingStrategy {

  override fun startSubscription(request: SubscriptionStartRequestDTO): Subscription {
    // 1. create subscription
    val subscription = billingStrategyHelper.createSubscription(request)
    // 2. activate
    subscription.toggleActive()
    subscriptionCommandPersistenceClient.save(subscription)
    return subscription
  }

  override fun terminateSubscription(subscription: Subscription): Subscription {
    // 1. terminate request
    subscription.terminateRequest()
    // 2. invoice
    val invoice = createInvoice(subscription)
    // 3. pay
    val payResult = subscriptionBillingClient.pay(
      enterpriseId = subscription.enterpriseId,
      BillingKeyPayRequest(
        installment = 0,
        subscriptionInvoice = invoice
      )
    )
    // 4. invoice pay success / invoice pay fail
    billingStrategyHelper.payPostProcess(payResult, invoice, subscription)

    subscriptionCommandPersistenceClient.save(subscription)
    subscriptionInvoicePersistenceClient.save(invoice)
    return subscription
  }

  override fun cancelTerminateSubscription(subscription: Subscription): Subscription {
    // 1. subscription 먼저 취소해제 -> ACTIVE 전환 (스케줄러에 의해서 송장 안 만들어짐)
    val subscription = subscription.cancelTerminateRequest()
    // 2. invoice DISCARD 처리 -> 스케줄러에 의해서 결제처리 안됨
    val invoiceList = subscriptionInvoicePersistenceClient.findBySubscriptionIdAndPayVersion(
      subscription.enterpriseId,
      subscription.id,
      subscription.payVersion
    ).map { invoice -> invoice.discard() }
    subscriptionInvoicePersistenceClient.saveAll(invoiceList)
    return subscriptionCommandPersistenceClient.save(subscription)
  }

  fun createInvoice(subscription: Subscription): SubscriptionInvoice {
    val snapshot = subscription.planSnapshot
    return SubscriptionInvoice(
      enterpriseId = subscription.enterpriseId,
      subscriptionId = subscription.id,
      memberId = subscription.memberId,
      storeId = subscription.storeId,
      payVersion = subscription.payVersion,
      planSnapshot = snapshot,
      state = InvoiceState(
        price = _getProrationPrice(subscription),
        // 사용량에 따른 초과금액 부여 없다는 전제하에 지금은 defaultPrice 만 바영
      ),
      // isPrepaidWhenCreate 아규먼트는 '생성 시' 에만 true 가 될 여지가 있다. 선불 결제일시 최초 결제에만 true
      targetBillingAt = LocalDateTime.now(),
      billKey = subscription.billingKey,
      lockGeneratingInvoice = subscription.isLockGeneratingInvoice

    )
  }

  private fun _getProrationPrice(subscription: Subscription): Long {
    if (subscription.nextBillingAt == null) return 0
    val start = LocalDate.now()
    val end = subscription.nextBillingAt?.toLocalDate()
    val leftDays = ChronoUnit.DAYS.between(start, end) + 1

    val billingPolicy = subscription.planSnapshot.billingPolicy
    val totalTermDays = subscription.planSnapshot.billingPolicy.countDays(subscription.nextBillingAt!!)
    val useDays = totalTermDays - leftDays

    return billingPolicy.convertToLongTypePrice(
      subscription.planSnapshot.priceInfo.defaultPrice.divide(totalTermDays.toBigDecimal(), 10, RoundingMode.HALF_UP)
        .run { this.multiply(useDays.toBigDecimal()) }
    )
  }

}

@Component
class BillingStrategyHelper() {

  fun createSubscription(subscriptionStartRequestDTO: SubscriptionStartRequestDTO): Subscription {
    return Subscription.create(
      enterpriseId = subscriptionStartRequestDTO.enterpriseId,
      storeId = subscriptionStartRequestDTO.storeId,
      planId = subscriptionStartRequestDTO.planId,
      planVersion = subscriptionStartRequestDTO.planVersion,
      memberId = subscriptionStartRequestDTO.memberId,
      billingKeyId = subscriptionStartRequestDTO.billingKey,
      subscriptionProductId = subscriptionStartRequestDTO.subscriptionProductId,
      planSnapshot = subscriptionStartRequestDTO.planSnapshot,
    )
  }

  fun payPostProcess(payResult: BillingKeyPayResponse, invoice: SubscriptionInvoice, subscription: Subscription) {
    when (payResult.resultCode) {
      "3001" -> {
        invoice.successPay()
        subscription.paySuccess(invoice.payVersion)
      }

      else -> {
        invoice.failPay()
        subscription.payFail(invoice.state.retryCount)
      }
    }
  }
}