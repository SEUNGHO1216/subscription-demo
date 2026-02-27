package com.seaotter.infra.subscription.adapter

import com.seaotter.core.application.subscription.dto.SubscriptionKey
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceQueryPersistencePort
import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.infra.subscription.persistence.repository.query.SubscriptionInvoiceQueryRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class SubscriptionInvoiceQueryPersistenceAdapter(
  private val subscriptionInvoiceQueryRepository: SubscriptionInvoiceQueryRepository
): SubscriptionInvoiceQueryPersistencePort {

  override fun findById(
    enterpriseId: String,
    id: String
  ): SubscriptionInvoice? {
    return subscriptionInvoiceQueryRepository.findById(enterpriseId, id)
      ?.toDomain()
  }

  override fun findAllBySubscriptionIdAndPayVersionNullable(
    enterpriseId: String,
    subscriptionId: String,
    payVersion: Long?
  ): List<SubscriptionInvoice> {
    return subscriptionInvoiceQueryRepository.findAllBySubscriptionIdAndPayVersionNullable(enterpriseId, subscriptionId, payVersion)
      .map { it.toDomain() }
  }

  override fun findAllByTargetBillingDateAndStatusDraft_OrPaymentFailedWithin14Days(
    enterpriseId: String?,
    id: String?,
    date: LocalDate
  ): List<SubscriptionInvoice> {
    val payFailedInvoiceList = subscriptionInvoiceQueryRepository.findAllByPaymentFailedWithin14Days(
      enterpriseId,
      id,
      date
    )
    val draftInvoiceList = subscriptionInvoiceQueryRepository.findAllByTargetBillingDateAndStatusEqual(
      enterpriseId,
      id,
      date,
      InvoiceStatus.DRAFT
    )
    return  (payFailedInvoiceList + draftInvoiceList)
      .associateBy { SubscriptionKey(it.id.enterpriseId, it.subscriptionId )}
      .values
      .map { it.toDomain() }

  }

  override fun findAllByTargetBillingDateAndStatus(
    enterpriseId: String?,
    id: String?,
    date: LocalDate,
    status: InvoiceStatus
  ): List<SubscriptionInvoice> {
    return subscriptionInvoiceQueryRepository.findAllByTargetBillingDateAndStatusEqual(
      enterpriseId, id, date, status
    ).map { it.toDomain() }
  }
}