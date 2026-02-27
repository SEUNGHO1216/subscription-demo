package com.seaotter.core.domain.subscription.client

import com.seaotter.core.domain.subscription.Subscription

interface SubscriptionCommandPersistenceClient {

  fun save(subscription: Subscription): Subscription
}