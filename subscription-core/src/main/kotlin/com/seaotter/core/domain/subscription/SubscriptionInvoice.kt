package com.seaotter.core.domain.subscription

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class SubscriptionInvoice(
  val enterpriseId: String,
  val subscriptionId: String,
  val memberId: String,
  val storeId: String,
  val planSnapshot: PlanSnapshot,
  var state: InvoiceState = InvoiceState.default(planSnapshot),
  var payVersion: Long,
  val targetBillingAt: LocalDateTime,
  val createdAt: LocalDateTime = LocalDateTime.now(),
  var billKey: String,
  val id: String= "${targetBillingAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))}-${subscriptionId}-${UUID.randomUUID().toString().split("-")[0]}",
  lockGeneratingInvoice: Boolean,
) {
  var lastFailedAt: LocalDateTime? = null
  var resolvedAt: LocalDateTime? = null
  init {
    require(!lockGeneratingInvoice) {"현재 INVOICE 를 생성할 수 없게 조치되어있습니다."}
  }

  // 1. invoice 단에서 먼저 pay -> 결과에 따라 subscription 도 후처리
  fun successPay() {
    this.resolvedAt = LocalDateTime.now()
    this.payVersion = this.payVersion.inc()
    this.state = this.state.copy(
      status = InvoiceStatus.RESOLVED
    )
  }

  fun failPay() {
    this.lastFailedAt = LocalDateTime.now()
    val accRetryCnt = this.state.retryCount.inc()
    this.state = this.state.copy(
      retryCount = accRetryCnt,
      status = if(this.planSnapshot.billingPolicy.retryCount < accRetryCnt) {
        InvoiceStatus.DISCARDED
      } else {
        InvoiceStatus.PAYMENT_ON_FAILURE
      }
    )
  }

  fun discard(): SubscriptionInvoice {
    this.state = this.state.copy(
      status = InvoiceStatus.DISCARDED
    )
    return this
  }
}

data class InvoiceState(
  val price: Long,
  val retryCount: Int = 0,
  var retryDateList: MutableList<LocalDateTime> = mutableListOf(),
  val status: InvoiceStatus = InvoiceStatus.DRAFT,
) {
  companion object {
    fun default(snapshot: PlanSnapshot) = InvoiceState(
      price = snapshot.billingPolicy.convertToLongTypePrice(snapshot.priceInfo.defaultPrice),
      retryCount = 0,
      status = InvoiceStatus.DRAFT
    )
  }
}

enum class InvoiceStatus {
  DRAFT, RESOLVED, PAYMENT_ON_FAILURE, DISCARDED
}