package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.subscription.dto.SubscriptionProductDTO
import com.seaotter.core.application.subscription.dto.SubscriptionProductWithPlansResponse
import com.seaotter.core.application.subscription.port.out.SubscriptionProductQueryPersistencePort
import org.springframework.stereotype.Service

@Service
class SubscriptionProductQueryService(
  private val subscriptionProductQueryPersistencePort: SubscriptionProductQueryPersistencePort
) {

  fun findById(enterpriseId: String, id: String): SubscriptionProductDTO? {
    return subscriptionProductQueryPersistencePort.findById(enterpriseId, id)
      ?.let { SubscriptionProductDTO(it) }
  }

  fun findByIdWithPlans(enterpriseId: String, id: String): SubscriptionProductWithPlansResponse? {
    return subscriptionProductQueryPersistencePort.findByIdWithPlans(enterpriseId, id)
      ?.let { SubscriptionProductWithPlansResponse(it) }
  }

  fun findAllByStoreId(enterpriseId: String, storeId: String): List<SubscriptionProductDTO> {
    return subscriptionProductQueryPersistencePort.findAllByStoreId(enterpriseId, storeId)
      .map { SubscriptionProductDTO(it) }
  }

  fun findAllByStoreIdWithPlans(enterpriseId: String, storeId: String): List<SubscriptionProductWithPlansResponse> {
    return subscriptionProductQueryPersistencePort.findAllByStoreIdWithPlans(enterpriseId, storeId)
      .map { SubscriptionProductWithPlansResponse(it) }
  }

  fun findAllByIdList(enterpriseId: String, idList: List<String>): List<SubscriptionProductDTO> {
    return subscriptionProductQueryPersistencePort.findAllByIdList(enterpriseId, idList)
      .map { SubscriptionProductDTO(it) }
  }
}