package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.subscription.dto.SubscriptionPlanCommandRequest
import com.seaotter.core.application.subscription.dto.SubscriptionPlanDTO
import com.seaotter.core.application.subscription.port.out.SubscriptionPlanCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionPlanQueryPersistencePort
import com.seaotter.core.domain.subscription.SubscriptionPlan
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionPlanCommandService(
  private val subscriptionPlanCommandPersistencePort: SubscriptionPlanCommandPersistencePort,
  private val subscriptionPlanQueryPersistencePort: SubscriptionPlanQueryPersistencePort
) {

  @Transactional
  fun createPlan(enterpriseId: String, createRequest: SubscriptionPlanCommandRequest): SubscriptionPlanDTO {
    return SubscriptionPlan(
      enterpriseId = enterpriseId,
      storeId = createRequest.storeId,
      subscriptionProductId = createRequest.subscriptionProductId,
      name = createRequest.name,
      grade = createRequest.grade,
      description = createRequest.description,
      priceInfo = createRequest.priceInfo,
      billingPolicy = createRequest.billingPolicy,
      isActive = createRequest.isActive,
    ).run { subscriptionPlanCommandPersistencePort.save(this) }
      .run { SubscriptionPlanDTO(this) }
  }

  @Transactional
  fun updatePlan(enterpriseId: String, id: String, updateRequest: SubscriptionPlanCommandRequest): SubscriptionPlanDTO {
    return requireNotNull(subscriptionPlanQueryPersistencePort.findById(enterpriseId, id)) { "해당 구독 플랜이 존재하지 않습니다." }
      .update(
        name = updateRequest.name,
        grade = updateRequest.grade,
        description = updateRequest.description,
        priceInfo = updateRequest.priceInfo,
        billingPolicy = updateRequest.billingPolicy,
      )
      .run { subscriptionPlanCommandPersistencePort.save(this) }
      .run { SubscriptionPlanDTO(this) }
  }

  @Transactional
  fun deletePlan(enterpriseId: String, idList: List<String>, deletedBy: String? = null) {
    subscriptionPlanQueryPersistencePort.findAllByIdList(enterpriseId, idList)
      .map { it.delete(deletedBy) }
      .also { subscriptionPlanCommandPersistencePort.saveAll(it) }
  }

  @Transactional
  fun activate(enterpriseId: String, idList: List<String>): List<SubscriptionPlanDTO> {
    return subscriptionPlanQueryPersistencePort.findAllByIdList(enterpriseId, idList)
      .map { it.activate() }
      .run { subscriptionPlanCommandPersistencePort.saveAll(this) }
      .map { SubscriptionPlanDTO(it) }
  }

  @Transactional
  fun inactivate(enterpriseId: String, idList: List<String>): List<SubscriptionPlanDTO> {
    return subscriptionPlanQueryPersistencePort.findAllByIdList(enterpriseId, idList)
      .map { it.inactivate() }
      .run { subscriptionPlanCommandPersistencePort.saveAll(this) }
      .map { SubscriptionPlanDTO(it) }
  }

}