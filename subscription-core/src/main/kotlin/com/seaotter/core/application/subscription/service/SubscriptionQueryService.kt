package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.subscription.port.out.SubscriptionQueryPersistencePort
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionStatus
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SubscriptionQueryService(
  private val subscriptionQueryPersistencePort: SubscriptionQueryPersistencePort,
) {
  fun findById(enterpriseId: String, id: String): Subscription? {
    return subscriptionQueryPersistencePort.findById(enterpriseId, id)
  }

  fun findAllByMemberId(enterpriseId: String, memberId: String): List<Subscription> {
    return subscriptionQueryPersistencePort.findAllByMemberId(enterpriseId, memberId)
  }

  fun findAllByNextBillingDateAndStatus(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
    status: SubscriptionStatus
  ): List<Subscription> {
    return subscriptionQueryPersistencePort.findAllByNextBillingDateAndStatus(
      enterpriseId = enterpriseId,
      id = id,
      date = date,
      status = status
    )
  }
}