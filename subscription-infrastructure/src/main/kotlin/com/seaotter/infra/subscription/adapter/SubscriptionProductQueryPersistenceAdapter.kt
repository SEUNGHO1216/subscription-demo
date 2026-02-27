package com.seaotter.infra.subscription.adapter

import com.seaotter.core.application.subscription.dto.SubscriptionProductWithPlans
import com.seaotter.core.application.subscription.port.out.SubscriptionProductQueryPersistencePort
import com.seaotter.core.domain.subscription.SubscriptionProduct
import com.seaotter.infra.subscription.persistence.repository.query.SubscriptionPlanQueryRepository
import com.seaotter.infra.subscription.persistence.repository.query.SubscriptionProductQueryRepository
import org.springframework.stereotype.Component

@Component
class SubscriptionProductQueryPersistenceAdapter(
  private val subscriptionProductQueryRepository: SubscriptionProductQueryRepository,
  private val subscriptionPlanQueryRepository: SubscriptionPlanQueryRepository
) : SubscriptionProductQueryPersistencePort {

  override fun findById(
    enterpriseId: String,
    id: String
  ): SubscriptionProduct? {
    return subscriptionProductQueryRepository.findById(enterpriseId, id)
      ?.toDomain()
  }

  override fun findByIdWithPlans(
    enterpriseId: String,
    id: String
  ): SubscriptionProductWithPlans? {
    return subscriptionProductQueryRepository.findById(enterpriseId, id)?.let {
      val plans = subscriptionPlanQueryRepository.findAllByStoreIdAndSubscriptionProductId(
        it.id.enterpriseId,
        it.storeId,
        it.id.subscriptionProductId
      )
      SubscriptionProductWithPlans(
        it.toDomain(),
        plans.map { it.toDomain() }
      )
    }
  }

  override fun findAllByStoreId(
    enterpriseId: String,
    storeId: String
  ): List<SubscriptionProduct> {
    return subscriptionProductQueryRepository.findAllByStoreId(enterpriseId, storeId)
      .map { it.toDomain() }
  }

  override fun findAllByStoreIdWithPlans(
    enterpriseId: String,
    storeId: String
  ): List<SubscriptionProductWithPlans> {
    val productList = subscriptionProductQueryRepository.findAllByStoreId(enterpriseId, storeId)

    val plansMap = subscriptionPlanQueryRepository.findAllByProductIdList(
      enterpriseId,
      productList.map { it.id.subscriptionProductId }
    ).groupBy { it.subscriptionProductId }

    return productList.map {
      SubscriptionProductWithPlans(
        it.toDomain(),
        plansMap[it.id.subscriptionProductId]?.map { it.toDomain() } ?: emptyList()
      )
    }
  }

  override fun findAllByIdList(
    enterpriseId: String,
    idList: List<String>
  ): List<SubscriptionProduct> {
    return subscriptionProductQueryRepository.findAllByIdList(enterpriseId, idList)
      .map { it.toDomain() }
  }

}