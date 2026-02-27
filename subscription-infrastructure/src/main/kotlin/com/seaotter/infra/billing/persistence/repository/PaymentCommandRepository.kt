package com.seaotter.infra.billing.persistence.repository

import com.seaotter.infra.billing.persistence.entity.PaymentEntity
import com.seaotter.infra.billing.persistence.entity.id.PaymentEntityId
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentCommandRepository: JpaRepository<PaymentEntity, PaymentEntityId> {
}