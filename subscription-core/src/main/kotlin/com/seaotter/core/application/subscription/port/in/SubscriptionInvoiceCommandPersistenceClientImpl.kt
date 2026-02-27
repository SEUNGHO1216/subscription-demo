package com.seaotter.core.application.subscription.port.`in`

import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceQueryPersistencePort
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.core.domain.subscription.client.SubscriptionInvoicePersistenceClient
import org.springframework.stereotype.Component

@Component
class SubscriptionInvoiceCommandPersistenceClientImpl(
  private val subscriptionInvoiceCommandPersistencePort: SubscriptionInvoiceCommandPersistencePort,
  private val subscriptionInvoiceQueryPersistencePort: SubscriptionInvoiceQueryPersistencePort
): SubscriptionInvoicePersistenceClient {
  override fun save(invoice: SubscriptionInvoice): SubscriptionInvoice {
    return subscriptionInvoiceCommandPersistencePort.save(invoice)
  }

  override fun saveAll(invoiceList: List<SubscriptionInvoice>): List<SubscriptionInvoice> {
    return subscriptionInvoiceCommandPersistencePort.saveAll(invoiceList)
  }

  override fun findBySubscriptionIdAndPayVersion(
    enterpriseId: String,
    subscriptionId: String,
    payVersion: Long
  ): List<SubscriptionInvoice> {
    return subscriptionInvoiceQueryPersistencePort.findAllBySubscriptionIdAndPayVersionNullable(enterpriseId, subscriptionId, payVersion)
  }
}