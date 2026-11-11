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

  // 낙관적 락 버전. 새로 만든 송장은 null 이다.
  var version: Long? = null

  init {
    require(!lockGeneratingInvoice) {"현재 INVOICE 를 생성할 수 없게 조치되어있습니다."}
  }

  fun isBillable(): Boolean = this.state.status in BILLABLE_STATUSES

  // PG 를 호출하기 전에 송장을 선점한다.
  // 여러 인스턴스가 같은 송장을 동시에 집어도, 이 상태 변경을 먼저 커밋한 한 곳만 결제로 넘어간다(낙관적 락).
  fun startProcessing(): SubscriptionInvoice {
    check(isBillable()) { "결제할 수 없는 송장 상태입니다. status=${this.state.status}" }
    this.state = this.state.copy(status = InvoiceStatus.PROCESSING)
    return this
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
  DRAFT,
  // 결제 요청을 보내는 중. PG 응답을 받기 전에 프로세스가 죽거나 응답이 없으면 이 상태로 남는다.
  // 결과를 모르는 상태이므로 자동 재시도하지 않고, PG 거래 내역과 대조해 수동으로 정리한다.
  PROCESSING,
  RESOLVED, PAYMENT_ON_FAILURE, DISCARDED
}

private val BILLABLE_STATUSES = setOf(InvoiceStatus.DRAFT, InvoiceStatus.PAYMENT_ON_FAILURE)