package com.seaotter.infra.subscription.adapter

import com.seaotter.core.application.subscription.dto.SubscriptionKey
import com.seaotter.core.application.subscription.port.out.SubscriptionQueryPersistencePort
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionStatus
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionEntityId
import com.seaotter.infra.subscription.persistence.repository.query.SubscriptionQueryRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SubscriptionQueryPersistenceAdapter(
  private val subscriptionQueryRepository: SubscriptionQueryRepository
): SubscriptionQueryPersistencePort {

  override fun findById(
    enterpriseId: String,
    id: String
  ): Subscription? {
    return subscriptionQueryRepository.findById(enterpriseId, id)
      ?.toDomain()
  }

  override fun findAllByMemberId(
    enterpriseId: String,
    memberId: String
  ): List<Subscription> {
    return subscriptionQueryRepository.findAllByMemberId(enterpriseId, memberId)
      .map { it.toDomain() }
  }

  override fun findAllByNextBillingDateAndStatus(
    enterpriseId: String?,
    id: String?,
    date: LocalDate,
    status: SubscriptionStatus,
  ): List<Subscription> {
    return subscriptionQueryRepository.findAllByNextBillingAtIsTodayAndStatus(enterpriseId, id, date, status)
      .map { it.toDomain() }
  }

  override fun findAllByIdList(
    subscriptionKeyList: List<SubscriptionKey>,
  ): List<Subscription> {
    return subscriptionQueryRepository.findAllByIdList(
      subscriptionKeyList.map {
        SubscriptionEntityId(it.enterpriseId, it.subscriptionId)
      }
    ).map { it.toDomain() }
  }
}