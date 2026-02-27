package com.seaotter.infra.subscription.persistence.entity.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class SubscriptionPlanEntityId(
  @Column(name = "enterprise_id")
  val enterpriseId: String,
  @Column(name = "subscription_plan_id")
  val subscriptionPlanId: String
): Serializable