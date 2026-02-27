package com.seaotter.infra.subscription.persistence.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class SubscriptionInvoiceEntityId(
  @Column(name = "enterprise_id")
  val enterpriseId: String,
  @Column(name = "subscription_invoice_id")
  val subscriptionInvoiceId: String
): Serializable