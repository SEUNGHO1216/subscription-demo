package com.seaotter.core.domain.subscription.service

import com.seaotter.core.domain.subscription.BillingStrategy
import com.seaotter.core.domain.subscription.client.SubscriptionBillingClient
import com.seaotter.core.domain.subscription.client.SubscriptionCommandPersistenceClient
import com.seaotter.core.domain.subscription.client.SubscriptionInvoicePersistenceClient
import org.springframework.stereotype.Component

@Component
class SubscriptionFactory(
  private val subscriptionCommandPersistenceClient: SubscriptionCommandPersistenceClient,
  private val subscriptionInvoiceCommandPersistenceClient: SubscriptionInvoicePersistenceClient,
  private val subscriptionBillingClient: SubscriptionBillingClient,
  private val billingStrategyHelper: BillingStrategyHelper,
) {

  fun getStrategy(billingStrategy: BillingStrategy): SubscriptionHandlerByBillingStrategy {
    return when (billingStrategy) {
      BillingStrategy.PREPAID -> PrepaidSubscriptionStrategy(
        billingStrategyHelper,
        subscriptionCommandPersistenceClient,
        subscriptionInvoiceCommandPersistenceClient,
        subscriptionBillingClient
      )
      BillingStrategy.POSTPAID -> PostpaidSubscriptionStrategy(
        billingStrategyHelper,
        subscriptionCommandPersistenceClient,
        subscriptionInvoiceCommandPersistenceClient,
        subscriptionBillingClient
      )
      else -> throw IllegalArgumentException("Unsupported billingStrategy : $billingStrategy")
    }
  }
}