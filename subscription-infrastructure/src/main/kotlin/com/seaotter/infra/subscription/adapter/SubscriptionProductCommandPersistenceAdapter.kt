package com.seaotter.infra.subscription.adapter

import com.seaotter.core.application.subscription.port.out.SubscriptionProductCommandPersistencePort
import com.seaotter.core.domain.subscription.SubscriptionProduct
import com.seaotter.infra.subscription.persistence.entity.SubscriptionProductEntity
import com.seaotter.infra.subscription.persistence.repository.SubscriptionProductCommandRepository
import org.springframework.stereotype.Component

@Component
class SubscriptionProductCommandPersistenceAdapter(
  private val productCommandRepository: SubscriptionProductCommandRepository
): SubscriptionProductCommandPersistencePort {

  override fun save(subscriptionProduct: SubscriptionProduct): SubscriptionProduct {
    return productCommandRepository.save(SubscriptionProductEntity.toEntity(subscriptionProduct))
      .toDomain()
  }

  override fun saveAll(products: List<SubscriptionProduct>): List<SubscriptionProduct> {
    return productCommandRepository.saveAll(
      products.map { SubscriptionProductEntity.toEntity(it) }
    ).map { it.toDomain() }
  }
}