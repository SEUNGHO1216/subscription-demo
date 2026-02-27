package com.seaotter.infra.subscription.adapter

import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceCommandPersistencePort
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.infra.subscription.persistence.entity.SubscriptionInvoiceEntity
import com.seaotter.infra.subscription.persistence.repository.SubscriptionInvoiceCommandRepository
import org.springframework.stereotype.Component

@Component
class SubscriptionInvoiceCommandPersistenceAdapter(
  private val subscriptionInvoiceCommandRepository: SubscriptionInvoiceCommandRepository
): SubscriptionInvoiceCommandPersistencePort {
  override fun save(subscriptionInvoice: SubscriptionInvoice): SubscriptionInvoice {
    return subscriptionInvoiceCommandRepository.save(
      SubscriptionInvoiceEntity.toEntity(subscriptionInvoice)
    ).toDomain()
  }

  override fun saveAll(subscriptionInvoices: List<SubscriptionInvoice>): List<SubscriptionInvoice> {
    return subscriptionInvoiceCommandRepository.saveAll(
      subscriptionInvoices.map { SubscriptionInvoiceEntity.toEntity(it) }
    ).map { it.toDomain() }
  }
}