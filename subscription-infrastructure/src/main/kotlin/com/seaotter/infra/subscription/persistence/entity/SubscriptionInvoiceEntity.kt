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
      return entity
    }
  }
}