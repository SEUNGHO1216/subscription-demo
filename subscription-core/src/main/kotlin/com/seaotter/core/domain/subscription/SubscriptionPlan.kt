package com.seaotter.core.domain.subscription

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.github.f4b6a3.ulid.UlidCreator
import com.seaotter.core.domain.common.BaseInfo
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class SubscriptionPlan(
  val enterpriseId: String,
  val id: String = UlidCreator.getUlid().toString() + UUID.randomUUID().toString().split("-")[0], // version 정보도 들어가자
  val storeId: String,
  val subscriptionProductId: String,
  var version: Int = 1,
  var name: String,
  var grade: String? = null,
  var description: String? = null,
  var priceInfo: SubscriptionPricing,
  var billingPolicy: SubscriptionBillingPolicy,
  var isActive: Boolean = false,
  var baseInfo: BaseInfo? = null,
) {

  fun update(
    name: String,
    grade: String? = null,
    description: String? = null,
    priceInfo: SubscriptionPricing,
    billingPolicy: SubscriptionBillingPolicy,
  ): SubscriptionPlan {
    if (this._needVersionUp(name, grade) ||
      this.priceInfo.needVersionUp(priceInfo) ||
      this.billingPolicy.needVersionUp(billingPolicy)
    ) {
      version++
    }
    this.name = name
    this.grade = grade
    this.description = description
    this.priceInfo = priceInfo
    this.billingPolicy = billingPolicy

    return this
  }

  private fun _needVersionUp(
    name: String,
    grade: String?,
  ): Boolean {
    return this.name != name || this.grade != grade
  }

  fun delete(deletedBy: String?): SubscriptionPlan {
    this.baseInfo = this.baseInfo?.copy(deletedAt = LocalDateTime.now(), deletedBy = deletedBy)
    this.isActive = false
    return this
  }

  fun activate(): SubscriptionPlan {
    if(this.baseInfo?.deletedAt == null) {
      this.isActive = true
    }
    return this
  }

  fun inactivate(): SubscriptionPlan {
    if(this.baseInfo?.deletedAt == null) {
      this.isActive = false
    }
    return this
  }

}

data class SubscriptionPricing(
  val defaultPrice: BigDecimal = BigDecimal.ZERO,
  val supplyPrice: BigDecimal = BigDecimal.ZERO,
  val rawPrice: BigDecimal = BigDecimal.ZERO,
) {
  fun needVersionUp(newSubscriptionPricing: SubscriptionPricing): Boolean {
    return this.defaultPrice != newSubscriptionPricing.defaultPrice ||
      this.supplyPrice != newSubscriptionPricing.supplyPrice ||
      this.rawPrice != newSubscriptionPricing.rawPrice
  }
}

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "billingType"
)
@JsonSubTypes(
  JsonSubTypes.Type(value = FlatBillingPolicy::class, name = "FLAT"),
)
interface SubscriptionBillingPolicy {
  val billingType: BillingType // FLAT, USAGE, SEAT
  val cycle: Int
  val cycleUnit: CycleUnit
  val billingStrategy: BillingStrategy // 선불, 후불
  val anchorPayDays: List<Int>?
  val roundMode: RoundingMode
  val retryCount: Int

  fun needVersionUp(newBillingPolicy: SubscriptionBillingPolicy): Boolean

  fun convertToLongTypePrice(decimalPrice: BigDecimal): Long {
    return when (this.roundMode) {
      RoundingMode.HALF_UP -> decimalPrice.setScale(0, RoundingMode.HALF_UP).toLong()
      RoundingMode.FLOOR -> decimalPrice.setScale(0, RoundingMode.FLOOR).toLong()
      RoundingMode.CEILING -> decimalPrice.setScale(0, RoundingMode.CEILING).toLong()
      else -> throw IllegalArgumentException("Not supported rounding mode")
    }
  }

  fun countDays(nextBillingAt: LocalDateTime): Long {
    return when (this.cycleUnit) {
      CycleUnit.DAY -> cycle.toLong()
      CycleUnit.MONTH -> {
        val start = nextBillingAt.minusMonths(cycle.toLong()).toLocalDate()
        (ChronoUnit.DAYS.between(start, nextBillingAt.toLocalDate()))
      }
      CycleUnit.YEAR -> {
        val start = nextBillingAt.minusYears(cycle.toLong()).toLocalDate()
        (ChronoUnit.DAYS.between(start, nextBillingAt.toLocalDate()))
      }
    }
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FlatBillingPolicy(
  override val billingType: BillingType = BillingType.FLAT, // PERIOD(기간), USAGE, SEAT
  override val cycle: Int,
  override val cycleUnit: CycleUnit,
  override val billingStrategy: BillingStrategy,
  override val anchorPayDays: List<Int>? = null, // PREPAID -> 가입일 ~ 결제일 일할 선불, 결제일 ~ 결제일 선불 / POSTPAID -> 가입일 ~ 결제일 일할 후불, 결제일 ~ 결제일 후불
  override val roundMode: RoundingMode = RoundingMode.HALF_UP,
  override val retryCount: Int = 3,
) : SubscriptionBillingPolicy {
  override fun needVersionUp(newBillingPolicy: SubscriptionBillingPolicy): Boolean {
    return this.billingType != newBillingPolicy.billingType ||
      this.cycleUnit != newBillingPolicy.cycleUnit ||
      this.cycle != newBillingPolicy.cycle
  }
}


enum class BillingType {
  FLAT, USAGE, SEAT
}

enum class CycleUnit {
  DAY, MONTH, YEAR
}

enum class BillingStrategy {
  PREPAID, POSTPAID, HYBRID
}

enum class RetryCondition {
  COUNT, PERIOD
}

data class PlanSnapshot(
  val version: Int,
  val name: String,
  val grade: String,
  val description: String? = null,
  val priceInfo: SubscriptionPricing,
  val billingPolicy: SubscriptionBillingPolicy,
)
