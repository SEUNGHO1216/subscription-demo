package com.seaotter.core.application.subscription.port.out

import com.seaotter.core.application.subscription.dto.SubscriptionProductWithPlans
import com.seaotter.core.domain.subscription.SubscriptionProduct

interface SubscriptionProductQueryPersistencePort {
  fun findById(enterpriseId: String, id: String): SubscriptionProduct?
  fun findByIdWithPlans(enterpriseId: String, id: String): SubscriptionProductWithPlans?
  fun findAllByStoreId(enterpriseId: String, storeId: String): List<SubscriptionProduct>
  fun findAllByStoreIdWithPlans(enterpriseId: String, storeId: String): List<SubscriptionProductWithPlans>
  fun findAllByIdList(enterpriseId: String, idList: List<String>): List<SubscriptionProduct>
}