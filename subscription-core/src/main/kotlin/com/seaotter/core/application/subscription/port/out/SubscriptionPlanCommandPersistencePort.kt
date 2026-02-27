package com.seaotter.core.application.subscription.port.out

import com.seaotter.core.domain.subscription.SubscriptionPlan

interface SubscriptionPlanCommandPersistencePort {

  fun save(subscriptionPlan: SubscriptionPlan): SubscriptionPlan
  fun saveAll(plans: List<SubscriptionPlan>): List<SubscriptionPlan>
}