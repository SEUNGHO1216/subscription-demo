package com.seaotter.core.application.billing.dto

import com.seaotter.core.domain.billing.BillingKey
import com.seaotter.core.domain.billing.VanCompany
import com.seaotter.core.domain.common.BaseInfo

data class BillingKeyDTO(
  val enterpriseId: String,
  val id: String,
  val memberId: String,
  val billKey: String,
  val cardCompanyCode: String,
  val cardCompanyName: String,
  val isCheckCard: Boolean,
  val cardNumber: String,
  var isActive: Boolean,
  val baseInfo: BaseInfo? = null
) {

  constructor(billingKey: BillingKey) : this(
    enterpriseId = billingKey.enterpriseId,
    id = billingKey.id,
    memberId = billingKey.memberId,
    billKey = billingKey.billKey,
    cardCompanyCode = billingKey.cardCompanyCode,
    cardCompanyName = billingKey.cardCompanyName,
    isCheckCard = billingKey.isCheckCard,
    cardNumber = billingKey.cardNumber,
    isActive = billingKey.isActive,
    baseInfo = billingKey.baseInfo
  )
}

data class BillingKeyRegisterRequest(
  val van: VanCompany? = null,
  val memberId: String,
  val username: String,
  val telephone: String,
  val cardNumber: String, // 하이픈없이
  val expireYear: String, // YY
  val expireMonth: String, // MM
  val birthday: String, // 6자리
  val cardPassword: String,// 비밀번호 앞 2 자리
  val personalInfoUseAgree: Boolean = true,
)

data class BillingKeyRegisterResponse(
  val billKey: String,
  val cardCode: String,
  val cardName: String,
  val checkOrCreditCode: String //0: 신용카드, 1: 체크카드
)
