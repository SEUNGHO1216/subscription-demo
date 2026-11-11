package com.seaotter.core.domain.subscription

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

object SubscriptionFixture {

  fun monthlyPolicy(
    strategy: BillingStrategy = BillingStrategy.POSTPAID,
    anchorPayDay: Int? = null,
    roundMode: RoundingMode = RoundingMode.HALF_UP,
    retryCount: Int = 3,
  ) = FlatBillingPolicy(
    cycle = 1,
    cycleUnit = CycleUnit.MONTH,
    billingStrategy = strategy,
    anchorPayDays = anchorPayDay?.let { listOf(it) },
    roundMode = roundMode,
    retryCount = retryCount,
  )

  fun snapshot(
    price: Long = 10_000,
    policy: SubscriptionBillingPolicy = monthlyPolicy(),
    name: String = "베이직",
  ) = PlanSnapshot(
    version = 1,
    name = name,
    grade = "BASIC",
    priceInfo = SubscriptionPricing(defaultPrice = BigDecimal.valueOf(price)),
    billingPolicy = policy,
  )

  fun subscription(
    snapshot: PlanSnapshot = snapshot(),
    status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 11, 10, 0),
    nextBillingAt: LocalDateTime? = null,
    payVersion: Long = 0,
    enterpriseId: String = "ent-test",
  ) = Subscription(
    enterpriseId = enterpriseId,
    storeId = "store-1",
    planId = "plan-1",
    planVersion = 1,
    payVersion = payVersion,
    memberId = "member-1",
    billingKey = "bill-key-1",
    subscriptionProductId = "product-1",
    planSnapshot = snapshot,
    status = status,
    createdAt = createdAt,
  ).also { it.nextBillingAt = nextBillingAt }

  fun invoice(
    subscription: Subscription,
    targetBillingAt: LocalDateTime = subscription.nextBillingAt ?: LocalDateTime.of(2026, 2, 1, 10, 0),
    price: Long = 10_000,
  ) = SubscriptionInvoice(
    enterpriseId = subscription.enterpriseId,
    subscriptionId = subscription.id,
    memberId = subscription.memberId,
    storeId = subscription.storeId,
    planSnapshot = subscription.planSnapshot,
    state = InvoiceState(price = price),
    payVersion = subscription.payVersion,
    targetBillingAt = targetBillingAt,
    billKey = subscription.billingKey,
    lockGeneratingInvoice = false,
  )
}
