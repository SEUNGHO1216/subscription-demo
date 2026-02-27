package com.seaotter.core.application.billing.service

import com.seaotter.core.application.billing.port.PaymentQueryPersistencePort
import com.seaotter.core.domain.billing.Payment
import org.springframework.stereotype.Service

@Service
class PaymentQueryService(
  private val paymentQueryPersistencePort: PaymentQueryPersistencePort,
) {

  fun findById(enterpriseId: String, id: String): Payment? {
    return paymentQueryPersistencePort.findById(enterpriseId, id)
  }

  fun findAllByMemberId(enterpriseId: String, memberId: String): List<Payment> {
    return paymentQueryPersistencePort.findAllByMemberId(enterpriseId, memberId)
  }

  fun findSuccessPaymentByInvoiceIdAndPayVersion(enterpriseId: String, invoiceId: String): Payment? {
    return paymentQueryPersistencePort.findSuccessPaymentByInvoiceId(
      enterpriseId = enterpriseId,
      invoiceId = invoiceId,
    )
  }

  fun findAllBySubscriptionId(
    enterpriseId: String,
    subscriptionId: String,
  ): List<Payment> {
    return paymentQueryPersistencePort.findAllBySubscriptionId(enterpriseId, subscriptionId)
  }
}