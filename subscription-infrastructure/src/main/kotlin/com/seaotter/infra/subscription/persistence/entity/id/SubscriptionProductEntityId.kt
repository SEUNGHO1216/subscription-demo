package com.seaotter.infra.subscription.persistence.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class SubscriptionProductEntityId(
  @Column(name = "enterprise_id")
  val enterpriseId: String,
  @Column(name = "subscription_product_id")
  val subscriptionProductId: String
): Serializable