package com.seaotter.core.application.subscription.port.out

import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import java.time.LocalDate

interface SubscriptionInvoiceQueryPersistencePort {

  fun findById(enterpriseId: String, id: String): SubscriptionInvoice?
  fun findAllBySubscriptionIdAndPayVersionNullable(enterpriseId: String, subscriptionId: String, payVersion: Long? = null): List<SubscriptionInvoice>
  // 스케줄러 용
  fun findAllByTargetBillingDateAndStatusDraft_OrPaymentFailedWithin14Days(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate
  ): List<SubscriptionInvoice>

  fun findAllByTargetBillingDateAndStatus(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
    status: InvoiceStatus
  ): List<SubscriptionInvoice>

}