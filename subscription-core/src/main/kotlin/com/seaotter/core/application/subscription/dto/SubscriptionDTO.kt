package com.seaotter.core.application.subscription.dto

import com.seaotter.core.domain.common.BaseInfo
import com.seaotter.core.domain.subscription.PlanSnapshot
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionBillingPolicy
import com.seaotter.core.domain.subscription.SubscriptionPlan
import com.seaotter.core.domain.subscription.SubscriptionPricing
import com.seaotter.core.domain.subscription.SubscriptionProduct
import com.seaotter.core.domain.subscription.SubscriptionStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class SubscriptionProductDTO(
  val id: String,
  val enterpriseId: String, // gemini, netflix 등
  val storeId: String,
  val name: String, // gemini pro, netflix standard
  var description: String? = null,
  val isActive: Boolean = false,
  val baseInfo: BaseInfo? = null,
) {
  constructor(product: SubscriptionProduct) : this(
    id = product.id,
    enterpriseId = product.enterpriseId,
    storeId = product.storeId,
    name = product.name,
    description = product.description,
    isActive = product.isActive,
    baseInfo = product.baseInfo,
  )
}

data class SubscriptionProductCommandRequest(
  val enterpriseId: String, // gemini, netflix 등
  val storeId: String,
  val name: String, // gemini pro, netflix standard
  var description: String? = null,
  val isActive: Boolean = false,
)

data class SubscriptionPlanDTO(
  val id: String,
  val enterpriseId: String, // gemini, netflix 등
  val storeId: String,
  val subscriptionProductId: String,
  val version: Int,
  val name: String, // gemini pro, netflix standard
  var description: String? = null,
  val grade: String? = null,
  val priceInfo: SubscriptionPricing,
  var billingPolicy: SubscriptionBillingPolicy,
  val isActive: Boolean,
  val baseInfo: BaseInfo? = null,
) {
  constructor(plan: SubscriptionPlan) : this(
    id = plan.id,
    enterpriseId = plan.enterpriseId,
    storeId = plan.storeId,
    subscriptionProductId = plan.subscriptionProductId,
    version = plan.version,
    name = plan.name,
    description = plan.description,
    grade = plan.grade,
    priceInfo = plan.priceInfo,
    billingPolicy = plan.billingPolicy,
    isActive = plan.isActive,
    baseInfo = plan.baseInfo,
  )
}

data class SubscriptionPlanCommandRequest(
  val enterpriseId: String, // gemini, netflix 등
  val storeId: String,
  val subscriptionProductId: String,
  val name: String, // gemini pro, netflix standard
  var description: String? = null,
  val grade: String? = null,
  val priceInfo: SubscriptionPricing,
  var billingPolicy: SubscriptionBillingPolicy,
  val isActive: Boolean,
)



data class SubscriptionDTO(
  val enterpriseId: String,
  val id: String,
  val storeId: String,
  val planId: String,
  val planVersion: Int,
  val memberId: String,
  val billingKey: String?=null,
  val subscriptionProductId: String,
  val planSnapshot: PlanSnapshot,
  val status: SubscriptionStatus,
  val createdAt: LocalDateTime,
  val payVersion: Long? = null,
  val nextBillingAt: LocalDateTime? = null,
  val cancelRequestedAt: LocalDateTime? = null,
  val canceledAt: LocalDateTime? = null,
  val nextSnapshot: PlanSnapshot? = null,
) {
  constructor(subscription: Subscription): this(
    enterpriseId = subscription.enterpriseId,
    id = subscription.id,
    planId = subscription.planId,
    storeId = subscription.storeId,
    planVersion = subscription.planVersion,
    memberId = subscription.memberId,
    billingKey = subscription.billingKey,
    subscriptionProductId = subscription.subscriptionProductId,
    planSnapshot = subscription.planSnapshot,
    payVersion = subscription.payVersion,
    status = subscription.status,
    createdAt = subscription.createdAt,
    nextBillingAt = subscription.nextBillingAt,
    cancelRequestedAt = subscription.cancelRequestedAt,
    canceledAt = subscription.canceledAt,
    nextSnapshot = subscription.nextSnapshot,
  )
}

data class SubscriptionKey(
  val enterpriseId: String,
  val subscriptionId: String
)

data class ManuallySchedulerCommandRequest(
  val enterpriseId: String? = null,
  val id: String? = null,
  val date: LocalDate
)


data class SubscriptionProductWithPlans(
  val subscriptionProduct: SubscriptionProduct,
  val subscriptionPlanList: List<SubscriptionPlan>
)

data class SubscriptionProductWithPlansResponse(
  val subscriptionProduct: SubscriptionProductDTO,
  val subscriptionPlanList: List<SubscriptionPlanDTO>
) {
  constructor(productWithPlans: SubscriptionProductWithPlans): this(
    subscriptionProduct = SubscriptionProductDTO(productWithPlans.subscriptionProduct),
    subscriptionPlanList = productWithPlans.subscriptionPlanList.map { SubscriptionPlanDTO(it) }
  )
}

