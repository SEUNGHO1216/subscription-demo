package com.seaotter.core.application.subscription.port.out

import com.seaotter.core.domain.subscription.SubscriptionInvoice


interface SubscriptionInvoiceCommandPersistencePort {
  fun save(subscriptionInvoice: SubscriptionInvoice): SubscriptionInvoice
  fun saveAll(subscriptionInvoices: List<SubscriptionInvoice>): List<SubscriptionInvoice>
}