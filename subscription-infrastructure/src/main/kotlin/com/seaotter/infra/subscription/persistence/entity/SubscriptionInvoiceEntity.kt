package com.seaotter.infra.subscription.persistence.entity

import com.seaotter.core.domain.subscription.InvoiceState
import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.PlanSnapshot
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionInvoiceEntityId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.Version
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.LocalDateTime

@Table(
  name = "subscription_invoice",
  indexes = [
    Index(name = "idx_enterprise_subscription", columnList = "enterprise_id, subscription_id"),
    Index(name = "idx_targetbillingdate_status", columnList = "target_billing_date, status"),
    Index(name = "idx_enterprise_targetbillingdate_status", columnList = "enterprise_id, target_billing_date, status"),
  ],
  // 같은 구독의 같은 결제 시점 송장은 하나만 존재한다. 애플리케이션 검사가 동시 실행으로 뚫려도 DB가 막는다.
  uniqueConstraints = [
    UniqueConstraint(name = "uk_invoice_subscription_target", columnNames = ["enterprise_id", "subscription_id", "target_billing_at"]),
  ]
)
@Entity
class SubscriptionInvoiceEntity(
  @EmbeddedId
  val id: SubscriptionInvoiceEntityId,
  val subscriptionId: String,
  val memberId: String,
  val storeId: String,
  @Column(name = "plan_snapshot", columnDefinition = "json")
  @JdbcTypeCode(SqlTypes.JSON)
  val planSnapshot: PlanSnapshot,
  val price: Long,
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(value = SqlTypes.VARCHAR)
  val status: InvoiceStatus,
  @Column(name = "invoice_state", columnDefinition = "json")
  @JdbcTypeCode(SqlTypes.JSON)
  var state: InvoiceState = InvoiceState.default(planSnapshot),
  val payVersion: Long,
  val targetBillingAt:LocalDateTime,
  val targetBillingDate: LocalDate,
  val createdAt: LocalDateTime,
  var billKey: String,
) {

  var lastFailedAt: LocalDateTime? = null
  var resolvedAt: LocalDateTime? = null

  // 낙관적 락. 기존 행에는 0이 채워지도록 기본값을 둔다(ddl-auto update 로 컬럼이 추가될 때).
  @Version
  @Column(name = "version", columnDefinition = "bigint not null default 0")
  var version: Long? = null


  fun toDomain(): SubscriptionInvoice {
    val domain = SubscriptionInvoice(
      enterpriseId = this.id.enterpriseId,
      id = this.id.subscriptionInvoiceId,
      subscriptionId = this.subscriptionId,
      memberId = this.memberId,
      storeId = this.storeId,
      planSnapshot = this.planSnapshot,
      state = this.state,
      payVersion = this.payVersion,
      targetBillingAt = this.targetBillingAt,
      createdAt = this.createdAt,
      billKey = this.billKey,
      lockGeneratingInvoice = false
    )
    domain.lastFailedAt = this.lastFailedAt
    domain.resolvedAt = this.resolvedAt
    domain.version = this.version
    return domain
  }

  companion object {
    fun toEntity(domain: SubscriptionInvoice): SubscriptionInvoiceEntity {
      val entity = SubscriptionInvoiceEntity(
        id = SubscriptionInvoiceEntityId(
          domain.enterpriseId,
          domain.id
        ),
        subscriptionId = domain.subscriptionId,
        memberId = domain.memberId,
        storeId = domain.storeId,
        planSnapshot = domain.planSnapshot,
        price = domain.state.price,
        status = domain.state.status,
        state = domain.state,
        payVersion = domain.payVersion,
        targetBillingAt = domain.targetBillingAt,
        targetBillingDate = domain.targetBillingAt.toLocalDate(),
        createdAt = domain.createdAt,
        billKey = domain.billKey,
      )
      entity.lastFailedAt = domain.lastFailedAt
      entity.resolvedAt = domain.resolvedAt
      entity.version = domain.version
      return entity
    }
  }
}