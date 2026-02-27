package com.seaotter.infra.billing.adapter

import com.seaotter.core.application.billing.port.RefundCommandPersistencePort
import com.seaotter.core.domain.billing.Refund
import com.seaotter.infra.billing.persistence.entity.RefundEntity
import com.seaotter.infra.billing.persistence.repository.RefundCommandRepository
import org.springframework.stereotype.Component

@Component
class RefundCommandPersistenceAdapter(
  private val refundCommandRepository: RefundCommandRepository
): RefundCommandPersistencePort {

  override fun save(refund: Refund): Refund {
    return refundCommandRepository.save(
      RefundEntity.toEntity(refund)
    ).toDomain()
  }
}