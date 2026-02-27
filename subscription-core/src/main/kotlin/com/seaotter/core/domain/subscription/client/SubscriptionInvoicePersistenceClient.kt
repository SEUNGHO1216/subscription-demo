package com.seaotter.core.domain.subscription.client

import com.seaotter.core.domain.subscription.SubscriptionInvoice

interface SubscriptionInvoicePersistenceClient {
  fun save(invoice: SubscriptionInvoice): SubscriptionInvoice
  fun saveAll(invoiceList: List<SubscriptionInvoice>): List<SubscriptionInvoice>
  fun findBySubscriptionIdAndPayVersion(enterpriseId: String, subscriptionId: String, payVersion: Long): List<SubscriptionInvoice>
}