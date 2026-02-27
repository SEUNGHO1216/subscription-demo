package com.seaotter.core.domain.billing

import com.github.f4b6a3.ulid.UlidCreator
import com.seaotter.core.domain.common.BaseInfo
import java.time.LocalDateTime
import java.util.UUID

class BillingKey(
  val enterpriseId: String,
  val id: String = UlidCreator.getUlid().toString() + enterpriseId +UUID.randomUUID().toString().split("-")[0],
  val memberId: String,
  val billKey: String,
  val cardCompanyCode: String,
  val cardCompanyName: String,
  val isCheckCard: Boolean,
  val cardNumber: String,
  val van: VanCompany = VanCompany.NICE_PAYMENTS,
  var isActive: Boolean = true,
  val baseInfo: BaseInfo? = null
) {

  fun delete(): BillingKey {
    this.isActive = false
    this.baseInfo?.deletedAt = LocalDateTime.now()
    return this
  }
}

enum class VanCompany{
  NICE_PAYMENTS, HECTO_PAYMENTS
}