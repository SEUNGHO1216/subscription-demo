package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.subscription.dto.SubscriptionPlanDTO
import com.seaotter.core.application.subscription.dto.SubscriptionProductCommandRequest
import com.seaotter.core.application.subscription.dto.SubscriptionProductDTO
import com.seaotter.core.application.subscription.port.out.SubscriptionProductCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionProductQueryPersistencePort
import com.seaotter.core.domain.subscription.SubscriptionProduct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionProductCommandService(
  private val subscriptionProductCommandPersistencePort: SubscriptionProductCommandPersistencePort,
  private val subscriptionProductQueryPersistencePort: SubscriptionProductQueryPersistencePort
) {

  @Transactional
  fun createProduct(enterpriseId: String, createRequest: SubscriptionProductCommandRequest): SubscriptionProductDTO {
    return SubscriptionProduct(
      enterpriseId = enterpriseId,
      storeId = createRequest.storeId,
      name = createRequest.name,
      description = createRequest.description,
      isActive = createRequest.isActive,
    ).let { subscriptionProductCommandPersistencePort.save(it) }
      .run { SubscriptionProductDTO(this) }
  }

  @Transactional
  fun updateProduct(
    enterpriseId: String,
    id: String,
    createRequest: SubscriptionProductCommandRequest
  ): SubscriptionProductDTO {
    return requireNotNull(subscriptionProductQueryPersistencePort.findById(enterpriseId, id)) { "해당 구독 상품이 없습니다." }
      .update(
        name = createRequest.name,
        description = createRequest.description,
        isActive = createRequest.isActive,
      )
      .let { subscriptionProductCommandPersistencePort.save(it) }
      .run { SubscriptionProductDTO(this) }
  }

  @Transactional
  fun deleteProduct(enterpriseId: String, idList: List<String>, deletedBy: String? = null) {
    subscriptionProductQueryPersistencePort.findAllByIdList(enterpriseId, idList)
      .map { it.delete(deletedBy) }
      .also { subscriptionProductCommandPersistencePort.saveAll(it) }
  }

  @Transactional
  fun activate(enterpriseId: String, idList: List<String>): List<SubscriptionProductDTO> {
    return subscriptionProductQueryPersistencePort.findAllByIdList(enterpriseId, idList)
      .map { it.activate() }
      .run { subscriptionProductCommandPersistencePort.saveAll(this) }
      .map { SubscriptionProductDTO(it) }
  }

  @Transactional
  fun inactivate(enterpriseId: String, idList: List<String>): List<SubscriptionProductDTO> {
    return subscriptionProductQueryPersistencePort.findAllByIdList(enterpriseId, idList)
      .map { it.inactivate() }
      .run { subscriptionProductCommandPersistencePort.saveAll(this) }
      .map { SubscriptionProductDTO(it) }
  }
}