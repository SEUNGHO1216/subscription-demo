package com.seaotter.infra.billing.persistence.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class RefundEntityId(
  @Column(name = "enterprise_id")
  val enterpriseId: String,
  @Column(name = "refund_id")
  val refundId: String
): Serializable
