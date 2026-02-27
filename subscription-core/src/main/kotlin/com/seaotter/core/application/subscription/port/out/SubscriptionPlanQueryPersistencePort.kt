package com.seaotter.core.application.subscription.port.out

import com.seaotter.core.domain.subscription.SubscriptionPlan

interface SubscriptionPlanQueryPersistencePort {

  fun findById(enterpriseId: String, planId: String): SubscriptionPlan?

  fun findAllByStoreIdAndSubscriptionProductId(
    enterpriseId: String,
    storeId: String? = null,
    productId: String? = null
  ): List<SubscriptionPlan>

  fun findAllByIdList(enterpriseId: String, idList: List<String>): List<SubscriptionPlan>


  fun findAllByProductIdList(enterpriseId: String, productIdList: List<String>): List<SubscriptionPlan>
}