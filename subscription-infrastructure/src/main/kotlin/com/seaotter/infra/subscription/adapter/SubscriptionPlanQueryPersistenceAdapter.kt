package com.seaotter.infra.subscription.adapter

import com.seaotter.core.application.subscription.port.out.SubscriptionPlanQueryPersistencePort
import com.seaotter.core.domain.subscription.SubscriptionPlan
import com.seaotter.infra.subscription.persistence.repository.query.SubscriptionPlanQueryRepository
import org.springframework.stereotype.Component

@Component
class SubscriptionPlanQueryPersistenceAdapter(
  private val subscriptionPlanQueryRepository: SubscriptionPlanQueryRepository
) : SubscriptionPlanQueryPersistencePort {

  override fun findById(
    enterpriseId: String,
    planId: String
  ): SubscriptionPlan? {
    return subscriptionPlanQueryRepository.findById(enterpriseId, planId)
      ?.toDomain()
  }

  override fun findAllByStoreIdAndSubscriptionProductId(
    enterpriseId: String,
    storeId: String?,
    productId: String?
  ): List<SubscriptionPlan> {
    return subscriptionPlanQueryRepository.findAllByStoreIdAndSubscriptionProductId(enterpriseId, storeId, productId)
      .map { it.toDomain() }
  }

  override fun findAllByIdList(
    enterpriseId: String,
    idList: List<String>
  ): List<SubscriptionPlan> {
    return subscriptionPlanQueryRepository.findAllByIdList(enterpriseId, idList)
      .map { it.toDomain() }
  }

  override fun findAllByProductIdList(
    enterpriseId: String,
    productIdList: List<String>
  ): List<SubscriptionPlan> {
    return subscriptionPlanQueryRepository.findAllByProductIdList(enterpriseId, productIdList)
      .map { it.toDomain() }
  }
}