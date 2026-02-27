package com.seaotter.infra.billing.persistence.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class BillingKeyEntityId(
  @Column(name = "enterprise_id")
  val enterpriseId: String,
  @Column(name = "billing_key_id")
  val billingKeyId: String
): Serializable
