package com.seaotter.core.application.subscription.port.out

import com.seaotter.core.application.subscription.dto.SubscriptionKey
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionStatus
import java.time.LocalDate

interface SubscriptionQueryPersistencePort {
  fun findById(enterpriseId: String, id: String): Subscription?
  fun findAllByMemberId(enterpriseId: String, memberId: String): List<Subscription>
  // 스케줄러 용
  fun findAllByNextBillingDateAndStatus(enterpriseId: String? = null, id: String? = null, date: LocalDate, status: SubscriptionStatus): List<Subscription>
  // 스케줄러 용
  fun findAllByIdList(subscriptionKeyList: List<SubscriptionKey>): List<Subscription>
}