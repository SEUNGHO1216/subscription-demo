package com.seaotter.infra.subscription.adapter

import com.seaotter.core.application.subscription.port.out.SubscriptionPlanCommandPersistencePort
import com.seaotter.core.domain.subscription.SubscriptionPlan
import com.seaotter.infra.subscription.persistence.entity.SubscriptionPlanEntity
import com.seaotter.infra.subscription.persistence.repository.SubscriptionPlanCommandRepository
import org.springframework.stereotype.Component

@Component
class SubscriptionPlanCommandPersistenceAdapter(
  private val planCommandRepository: SubscriptionPlanCommandRepository
): SubscriptionPlanCommandPersistencePort {

  override fun save(subscriptionPlan: SubscriptionPlan): SubscriptionPlan {
    return planCommandRepository.save(
      SubscriptionPlanEntity.toEntity(subscriptionPlan)
    ).toDomain()
  }

  override fun saveAll(plans: List<SubscriptionPlan>): List<SubscriptionPlan> {
    return planCommandRepository.saveAll(
      plans.map { SubscriptionPlanEntity.toEntity(it) }
    ).map { it.toDomain() }
  }
}