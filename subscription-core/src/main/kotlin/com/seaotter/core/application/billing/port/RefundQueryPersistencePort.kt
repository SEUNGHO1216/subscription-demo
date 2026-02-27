package com.seaotter.core.application.billing.port

import com.seaotter.core.domain.billing.Refund

interface RefundQueryPersistencePort {
  fun findAllByMemberId(enterpriseId: String, memberId: String): List<Refund>
  fun findAllByPaymentId(enterpriseId: String, paymentId: String): List<Refund>
}