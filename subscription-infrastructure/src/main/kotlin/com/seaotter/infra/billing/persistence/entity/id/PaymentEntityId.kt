package com.seaotter.infra.billing.persistence.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class PaymentEntityId(
  @Column(name = "enterprise_id")
  val enterpriseId: String,
  @Column(name = "payment_id")
  val paymentId: String
): Serializable
