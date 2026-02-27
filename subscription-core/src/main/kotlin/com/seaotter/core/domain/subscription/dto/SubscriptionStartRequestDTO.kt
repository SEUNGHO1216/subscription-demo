package com.seaotter.core.domain.subscription.dto

import com.seaotter.core.domain.billing.VanCompany
import com.seaotter.core.domain.subscription.PlanSnapshot

// 1. 프론트 빌키 발급 -> 응답
// 2. 이 DTO 로 subscribe & pay
data class SubscriptionStartRequestDTO(
  val enterpriseId: String,
  val storeId: String,
  val planId: String,
  val planVersion: Int,
  val subscriptionProductId: String,
  val planSnapshot: PlanSnapshot,
  val memberId: String,
  val billingKey: String,

  val van: VanCompany,
  val installment: Int = 0,
)