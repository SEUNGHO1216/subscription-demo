package com.seaotter.infra.billing.adapter

import com.seaotter.core.application.billing.port.PaymentQueryPersistencePort
import com.seaotter.core.domain.billing.Payment
import com.seaotter.infra.billing.persistence.repository.query.PaymentQueryRepository
import org.springframework.stereotype.Component

@Component
class PaymentQueryPersistenceAdapter(
  private val paymentQueryRepository: PaymentQueryRepository
) : PaymentQueryPersistencePort {
  override fun findById(
    enterpriseId: String,
    paymentId: String
  ): Payment? {
    return paymentQueryRepository.findById(enterpriseId, paymentId)?.toDomain()
  }

  override fun findSuccessPaymentByInvoiceId(
    enterpriseId: String,
    invoiceId: String
  ): Payment? {
    return paymentQueryRepository.findSuccessPaymentByInvoiceId(enterpriseId, invoiceId)
      ?.toDomain()
  }

  override fun findAllBySubscriptionId(
    enterpriseId: String,
    subscriptionId: String,
  ): List<Payment> {
    return paymentQueryRepository.findAllBySubscriptionId(enterpriseId, subscriptionId)
      .map { it.toDomain() }
  }

  override fun findAllByMemberId(
    enterpriseId: String,
    memberId: String
  ): List<Payment> {
    return paymentQueryRepository.findAllByMemberId(enterpriseId, memberId)
      .map { it.toDomain() }
  }
}