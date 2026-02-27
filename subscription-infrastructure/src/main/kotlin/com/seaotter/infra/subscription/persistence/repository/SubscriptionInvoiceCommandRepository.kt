package com.seaotter.infra.subscription.persistence.repository

import com.seaotter.infra.subscription.persistence.entity.SubscriptionInvoiceEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionInvoiceEntityId
import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionInvoiceCommandRepository: JpaRepository<SubscriptionInvoiceEntity, SubscriptionInvoiceEntityId> {
}