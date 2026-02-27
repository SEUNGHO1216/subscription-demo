package com.seaotter.infra.billing.adapter

import com.seaotter.core.application.billing.port.PaymentCommandPersistencePort
import com.seaotter.core.domain.billing.Payment
import com.seaotter.infra.billing.persistence.entity.PaymentEntity
import com.seaotter.infra.billing.persistence.repository.PaymentCommandRepository
import org.springframework.stereotype.Component

@Component
class PaymentCommandPersistenceAdapter(
  private val paymentCommandRepository: PaymentCommandRepository,
): PaymentCommandPersistencePort {

  override fun save(payment: Payment): Payment {
    return paymentCommandRepository.save(
      PaymentEntity.toEntity(payment)
    ).toDomain()
  }
}