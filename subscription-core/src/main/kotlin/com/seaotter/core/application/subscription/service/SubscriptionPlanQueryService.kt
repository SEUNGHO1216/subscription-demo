package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.subscription.dto.SubscriptionPlanDTO
import com.seaotter.core.application.subscription.port.out.SubscriptionPlanQueryPersistencePort
import org.springframework.stereotype.Service

@Service
class SubscriptionPlanQueryService(
  private val subscriptionPlanQueryPersistencePort: SubscriptionPlanQueryPersistencePort
) {

  fun findById(enterpriseId: String, id: String): SubscriptionPlanDTO? {
    return subscriptionPlanQueryPersistencePort.findById(enterpriseId, id)
      ?.let { SubscriptionPlanDTO(it) }
  }

  fun findAllByIdList(enterpriseId: String, idList: List<String>): List<SubscriptionPlanDTO> {
    return subscriptionPlanQueryPersistencePort.findAllByIdList(enterpriseId, idList)
      .map { SubscriptionPlanDTO(it) }
  }

  fun findAllByStoreIdAndSubscriptionProductId(enterpriseId: String, storeId: String?, productId: String?): List<SubscriptionPlanDTO> {
    return subscriptionPlanQueryPersistencePort.findAllByStoreIdAndSubscriptionProductId(enterpriseId, storeId, productId)
      .map { SubscriptionPlanDTO(it) }
  }

  fun findAllByProductIdList(enterpriseId: String, productIdList: List<String>): List<SubscriptionPlanDTO> {
    return subscriptionPlanQueryPersistencePort.findAllByProductIdList(enterpriseId, productIdList)
      .map { SubscriptionPlanDTO(it) }
  }
}