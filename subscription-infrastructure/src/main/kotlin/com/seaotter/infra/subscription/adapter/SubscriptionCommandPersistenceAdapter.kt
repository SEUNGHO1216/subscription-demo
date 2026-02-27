package com.seaotter.infra.subscription.adapter

import com.seaotter.core.application.subscription.port.out.SubscriptionCommandPersistencePort
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.infra.subscription.persistence.entity.SubscriptionEntity
import com.seaotter.infra.subscription.persistence.repository.SubscriptionCommandRepository
import org.springframework.stereotype.Component

@Component
class SubscriptionCommandPersistenceAdapter(
  private val subscriptionCommandRepository: SubscriptionCommandRepository
): SubscriptionCommandPersistencePort {
  override fun save(subscription: Subscription): Subscription {
    return subscriptionCommandRepository.save(
      SubscriptionEntity.toEntity(subscription)
    ).toDomain()
  }

  override fun saveAll(subscriptionList: List<Subscription>): List<Subscription> {
    return subscriptionCommandRepository.saveAll(
      subscriptionList.map { SubscriptionEntity.toEntity(it) }
    ).map { it.toDomain() }
  }
}