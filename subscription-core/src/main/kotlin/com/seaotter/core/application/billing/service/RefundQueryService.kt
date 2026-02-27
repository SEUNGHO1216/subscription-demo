package com.seaotter.core.application.billing.service

import com.seaotter.core.application.billing.port.RefundQueryPersistencePort
import com.seaotter.core.domain.billing.Refund
import org.springframework.stereotype.Service

@Service
class RefundQueryService(
  private val refundQueryPersistencePort: RefundQueryPersistencePort
) {

  fun findAllByMemberId(enterpriseId: String, memberId: String): List<Refund> {
    return refundQueryPersistencePort.findAllByMemberId(enterpriseId, memberId)
  }

  fun findAllByPaymentId(enterpriseId: String, paymentId: String): List<Refund> {
    return refundQueryPersistencePort.findAllByPaymentId(
      enterpriseId = enterpriseId,
      paymentId = paymentId
    )
  }

}