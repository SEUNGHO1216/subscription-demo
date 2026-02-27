package com.seaotter.core.application.subscription.port.out

import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionInvoice

interface SubscriptionCommandPersistencePort {
  fun save(subscription: Subscription): Subscription
  fun saveAll(subscriptionList: List<Subscription>): List<Subscription>
}