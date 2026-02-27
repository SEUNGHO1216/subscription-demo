package com.seaotter.core.application.subscription.port.out

import com.seaotter.core.domain.subscription.SubscriptionProduct

interface SubscriptionProductCommandPersistencePort {

  fun save(subscriptionProduct: SubscriptionProduct): SubscriptionProduct
  fun saveAll(products: List<SubscriptionProduct>): List<SubscriptionProduct>
}