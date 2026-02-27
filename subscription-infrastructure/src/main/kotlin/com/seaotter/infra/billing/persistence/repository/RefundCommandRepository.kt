package com.seaotter.infra.billing.persistence.repository

import com.seaotter.infra.billing.persistence.entity.RefundEntity
import com.seaotter.infra.billing.persistence.entity.id.RefundEntityId
import org.springframework.data.jpa.repository.JpaRepository

interface RefundCommandRepository: JpaRepository<RefundEntity, RefundEntityId> {
}