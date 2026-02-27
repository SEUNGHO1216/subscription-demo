package com.seaotter.core.domain.subscription

import com.github.f4b6a3.ulid.UlidCreator
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Subscription(
  val enterpriseId: String,
  val id: String = UlidCreator.getUlid().toString() + UUID.randomUUID().toString().split("-")[0],
  val storeId: String,
  val planId: String,
  val planVersion: Int,
  var payVersion: Long=0,
  val memberId: String,
  val billingKey: String,
  val subscriptionProductId: String,
  var planSnapshot: PlanSnapshot,
  var status: SubscriptionStatus = SubscriptionStatus.PENDING, // ACTIVE, TERMINATE_READY, PAYMENT_ON_FAILURE, INACTIVE, TERMINATED
  val createdAt: LocalDateTime = LocalDateTime.now(),
  var isLockGeneratingInvoice: Boolean = false,
) {
  var nextBillingAt: LocalDateTime? = null // entity 에서는 date, time 나눠서 저장하자.
  var cancelRequestedAt: LocalDateTime? = null
  var canceledAt: LocalDateTime? = null

//  var isTerminateRequested: Boolean = false
  var nextSnapshot: PlanSnapshot? = null // 스냅샷 계약 정보가 바뀌어서 아예 새로운 subscription 을 만들 재료

  companion object {
    fun create(
      enterpriseId: String,
      storeId: String,
      planId: String,
      planVersion: Int,
      memberId: String,
      billingKeyId: String,
      subscriptionProductId: String,
      planSnapshot: PlanSnapshot,
    ): Subscription {
      val subscription = Subscription(
        enterpriseId = enterpriseId,
        storeId = storeId,
        planId = planId,
        planVersion = planVersion,
        memberId = memberId,
        billingKey = billingKeyId,
        subscriptionProductId = subscriptionProductId,
        planSnapshot = planSnapshot,
      )
      subscription.initNextBillingAt(subscription.createdAt)
      return subscription
    }
  }

  fun toggleActive(): Subscription {
    if (this.status !in listOf(SubscriptionStatus.TERMINATED, SubscriptionStatus.PAYMENT_ON_FAILURE) ) {
      this.status = SubscriptionStatus.ACTIVE
    }
    if (this.status in listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAYMENT_ON_FAILURE)) {
      this.status = SubscriptionStatus.INACTIVE
    }
    return this
  }

  fun terminateRequest(): Subscription {
    check(this.status == SubscriptionStatus.ACTIVE) { "구독 해지 요청은 활성 상태에서만 가능합니다."}
    // 결제일 당일에 결제 전 취소 요청은 안 되게 하자. 이미 송장이 나왔기 때문.
    check(this.nextBillingAt?.toLocalDate()?.isEqual(LocalDate.now()) == false) {"결제일 당일에 결제 전 해지 요청은 불가합니다."}
    this.cancelRequestedAt = LocalDateTime.now()
    this.status = SubscriptionStatus.TERMINATE_READY
    return this
  }

  fun terminateComplete(): Subscription {
    if (this.status in listOf(SubscriptionStatus.TERMINATED)) {
      throw IllegalArgumentException("구독 해지를 할 수 없는 상태입니다.")
    }
    this.canceledAt = LocalDateTime.now()
    this.status = SubscriptionStatus.TERMINATED
    return this
  }

  // 에러 시 사용 (왜 필요한가)
  fun rollbackTerminate(beforeStatus: SubscriptionStatus): Subscription {
    this.status = beforeStatus
    this.cancelRequestedAt = null
    this.canceledAt = null
    return this
  }

  // terminate ready 일 때만
  fun cancelTerminateRequest(): Subscription {
    check(this.status == SubscriptionStatus.TERMINATE_READY) { "해지 요청 상태가 아니면 해지 취소가 불가합니다." }
    check(this.nextBillingAt?.toLocalDate()?.isEqual(LocalDate.now()) == false) {"결제일 당일에 결제 전 해지 요청 취소는 불가합니다."}
    this.status = SubscriptionStatus.ACTIVE
    this.cancelRequestedAt = null
    return this
  }

  // monthly 결제에서만 적용
  fun changeBillingPolicy(newBillingPolicy: SubscriptionBillingPolicy): Subscription {
    this.nextSnapshot = this.planSnapshot.copy(
      // todo: 더 유연하게 바꿔야함
      billingPolicy = newBillingPolicy,
    )
//    initNextBillingAt(LocalDateTime.now())
    return this
  }

  // 첫 생성시, 결제일 수정 시
  fun initNextBillingAt(commandAt: LocalDateTime) {
    val billingPolicy = this.planSnapshot.billingPolicy
    when (billingPolicy.cycleUnit) {
      CycleUnit.DAY -> {
        this.nextBillingAt = commandAt.plusDays(billingPolicy.cycle.toLong())
      }

      CycleUnit.MONTH -> {
        if (billingPolicy.anchorPayDays != null && billingPolicy.anchorPayDays?.isNotEmpty() == true) {
          val today = LocalDate.now().dayOfMonth
          val payDay = billingPolicy.anchorPayDays!!.first()
          if (today < payDay) {
            this.nextBillingAt = LocalDateTime.of(
              commandAt.year,
              commandAt.monthValue,
              payDay,
              commandAt.hour,
              commandAt.minute,
              commandAt.second
            )
          } else {
            this.nextBillingAt =
              LocalDateTime.of(
                commandAt.year,
                commandAt.plusMonths(1).monthValue,
                payDay,
                commandAt.hour,
                commandAt.minute,
                commandAt.second
              )
          }
          return
        }
        this.nextBillingAt = commandAt.plusMonths(billingPolicy.cycle.toLong())
      }

      CycleUnit.YEAR -> {
        this.nextBillingAt = commandAt.plusYears(billingPolicy.cycle.toLong())
      }
    }
  }

  fun paySuccess(payVersion: Long): Subscription {
    // 후불 결제 취소(즉시 취소) 시 결제 성공하면 terminate 처리
    if (this.status == SubscriptionStatus.TERMINATE_READY) {
      return this.terminateComplete()
    }
    if (nextSnapshot != null) {
      this.planSnapshot = nextSnapshot!!
      this.nextSnapshot = null
    }
    this.status = SubscriptionStatus.ACTIVE
    this.payVersion = payVersion
    this.isLockGeneratingInvoice = false
    this.updateNextBillingAt()
    return this
  }

  fun payFail(retryCount: Int): Subscription {
    this.status = SubscriptionStatus.PAYMENT_ON_FAILURE
    this.isLockGeneratingInvoice = true

    if (this.planSnapshot.billingPolicy.retryCount < retryCount) {
      this.status = SubscriptionStatus.INACTIVE
      return this
    }
    return this
  }

  fun lockGeneratingInvoice(): Subscription {
    this.isLockGeneratingInvoice = true
    return this
  }


  // 결제 성공 시 호출
  fun updateNextBillingAt() {
    if((this.planSnapshot.billingPolicy.billingStrategy == BillingStrategy.PREPAID) && this.payVersion == 1L) {
      return
    }
    val billingPolicy = this.planSnapshot.billingPolicy
    when (billingPolicy.cycleUnit) {
      CycleUnit.DAY -> {
        this.nextBillingAt = this.nextBillingAt?.plusDays(billingPolicy.cycle.toLong())
      }

      CycleUnit.MONTH -> {
        this.nextBillingAt = this.nextBillingAt?.plusMonths(billingPolicy.cycle.toLong())
      }

      CycleUnit.YEAR -> {
        this.nextBillingAt = this.nextBillingAt?.plusYears(billingPolicy.cycle.toLong())
      }
    }
  }
}

enum class SubscriptionStatus {
  PENDING, ACTIVE, PAYMENT_ON_FAILURE, INACTIVE, TERMINATE_READY, TERMINATED
  // PENDING, INACTIVE, TERMINATED 일땐 사용불가임
}

