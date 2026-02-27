package com.seaotter.core.application.subscription.port.`in`

import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.client.SubscriptionCommandPersistenceClient
import com.seaotter.core.application.subscription.port.out.SubscriptionCommandPersistencePort
import org.springframework.stereotype.Component

@Component
class SubscriptionCommandPersistenceClientImpl(
  private val subscriptionCommandPersistencePort: SubscriptionCommandPersistencePort
): SubscriptionCommandPersistenceClient {

  override fun save(subscription: Subscription): Subscription {
    return subscriptionCommandPersistencePort.save(subscription)
  }

}