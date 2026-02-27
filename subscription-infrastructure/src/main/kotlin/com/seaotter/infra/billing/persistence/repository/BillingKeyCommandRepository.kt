package com.seaotter.infra.billing.persistence.repository

import com.seaotter.infra.billing.persistence.entity.BillingKeyEntity
import com.seaotter.infra.billing.persistence.entity.id.BillingKeyEntityId
import org.springframework.data.jpa.repository.JpaRepository

interface BillingKeyCommandRepository: JpaRepository<BillingKeyEntity, BillingKeyEntityId> {
}