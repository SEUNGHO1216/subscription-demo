package com.seaotter.core.application.billing.port

import com.seaotter.core.domain.billing.Payment

interface PaymentQueryPersistencePort {

  fun findById(enterpriseId: String, paymentId: String): Payment?
  // 3001 resultCode 만 조회
  fun findSuccessPaymentByInvoiceId(enterpriseId: String, invoiceId: String): Payment?

  fun findAllBySubscriptionId(
    enterpriseId: String,
    subscriptionId: String,
  ): List<Payment>

  fun findAllByMemberId(enterpriseId: String, memberId: String): List<Payment>
}