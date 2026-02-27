package com.seaotter.infra.subscription.persistence.entity

import com.seaotter.core.domain.subscription.PlanSnapshot
import com.seaotter.core.domain.subscription.Subscription
import com.seaotter.core.domain.subscription.SubscriptionStatus
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionEntityId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Table(
  name = "subscription",
  indexes = [
    Index(name = "idx_enterprise_member", columnList = "enterprise_id, member_id"),
    Index(name = "idx_nextbillingdate_status", columnList = "next_billing_date, status"),
    Index(name = "idx_enterprise_nextbillingdate_status", columnList = "enterprise_id, next_billing_date, status"),
  ]
)
@Entity
class SubscriptionEntity(
  @EmbeddedId
  val id: SubscriptionEntityId,
  val storeId: String,
  val planId: String,
  val planVersion: Int,
  val memberId: String,
  val billingKey: String,
  val subscriptionProductId: String,
  val name: String,
  @Column(name = "plan_snapshot", columnDefinition = "json")
  @JdbcTypeCode(SqlTypes.JSON)
  var planSnapshot: PlanSnapshot,
  val grade: String,
  @Column(precision = 19, scale = 2)
  val price: BigDecimal,
  var payVersion: Long = 1,
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(value = SqlTypes.VARCHAR)
  var status: SubscriptionStatus, // ACTIVE, TERMINATE_READY, PAYMENT_ON_FAILURE, INACTIVE, TERMINATED
  val createdAt: LocalDateTime = LocalDateTime.now(), // entity 에서는 date, time 나눠서 저장하자.
  var isLockGeneratingInvoice: Boolean,
) {
  var nextBillingAt: LocalDateTime? = null // entity 에서는 date, time 나눠서 저장하자.
  var nextBillingDate: LocalDate? = null // entity 에서는 date, time 나눠서 저장하자.
  var cancelRequestedAt: LocalDateTime? = null
  var canceledAt: LocalDateTime? = null
  @Column(name = "next_snapshot", columnDefinition = "json")
  @JdbcTypeCode(SqlTypes.JSON)
  var nextSnapshot: PlanSnapshot? = null // 스냅샷 계약 정보가 바뀌어서 아예 새로운 subscription 을 만들 재료

  fun toDomain(): Subscription {
    val subscription = Subscription(
      enterpriseId = this.id.enterpriseId,
      id = this.id.subscriptionId,
      storeId = storeId,
      planId = planId,
      planVersion = planVersion,
      memberId = memberId,
      billingKey = billingKey,
      subscriptionProductId = subscriptionProductId,
      planSnapshot = planSnapshot,
      payVersion = payVersion,
      status = status,
      createdAt = createdAt,
      isLockGeneratingInvoice = isLockGeneratingInvoice,
    )
    subscription.nextBillingAt = nextBillingAt
    subscription.cancelRequestedAt = cancelRequestedAt
    subscription.canceledAt = canceledAt
    subscription.nextSnapshot = nextSnapshot
    return subscription
  }

  companion object {
    fun toEntity(domain: Subscription): SubscriptionEntity {
      val entity = SubscriptionEntity(
        id = SubscriptionEntityId(
          domain.enterpriseId,
          domain.id
        ),
        storeId = domain.storeId,
        planId = domain.planId,
        planVersion = domain.planVersion,
        memberId = domain.memberId,
        billingKey = domain.billingKey,
        subscriptionProductId = domain.subscriptionProductId,
        name = domain.planSnapshot.name,
        planSnapshot = domain.planSnapshot,
        grade = domain.planSnapshot.grade,
        price = domain.planSnapshot.priceInfo.defaultPrice,
        payVersion = domain.payVersion,
        status = domain.status,
        createdAt = domain.createdAt,
        isLockGeneratingInvoice = domain.isLockGeneratingInvoice,
      )
      entity.nextBillingAt = domain.nextBillingAt
      entity.nextBillingDate = domain.nextBillingAt?.toLocalDate()
      entity.cancelRequestedAt = domain.cancelRequestedAt
      entity.canceledAt = domain.canceledAt
      entity.nextSnapshot = domain.nextSnapshot
      return entity
    }
  }
}

