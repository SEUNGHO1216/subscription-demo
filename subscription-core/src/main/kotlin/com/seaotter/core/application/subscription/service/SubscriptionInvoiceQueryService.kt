package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceQueryPersistencePort
import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.core.domain.subscription.SubscriptionStatus
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SubscriptionInvoiceQueryService(
  private val subscriptionInvoiceQueryPersistencePort: SubscriptionInvoiceQueryPersistencePort,
) {

  fun findById(enterpriseId: String, id: String): SubscriptionInvoice? {
    return subscriptionInvoiceQueryPersistencePort.findById(enterpriseId, id)
  }

  fun findAllBySubscriptionIdAndPayVersionNullable(
    enterpriseId: String,
    subscriptionId: String,
    payVersion: Long? = null
  ): List<SubscriptionInvoice> {
    return subscriptionInvoiceQueryPersistencePort.findAllBySubscriptionIdAndPayVersionNullable(
      enterpriseId, subscriptionId, payVersion
    )
  }

  fun findAllByTargetBillingDateAndStatusDraft_OrPaymentFailedWithin14Days(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
  ): List<SubscriptionInvoice> {
    return subscriptionInvoiceQueryPersistencePort.findAllByTargetBillingDateAndStatusDraft_OrPaymentFailedWithin14Days(
      enterpriseId = enterpriseId,
      id = id,
      date = date,
    )
  }

  fun findAllByTargetBillingDateAndStatus(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
    status: InvoiceStatus,
  ): List<SubscriptionInvoice> {
    return subscriptionInvoiceQueryPersistencePort.findAllByTargetBillingDateAndStatus(
      enterpriseId = enterpriseId,
      id = id,
      date = date,
      status = status
    )
  }
}