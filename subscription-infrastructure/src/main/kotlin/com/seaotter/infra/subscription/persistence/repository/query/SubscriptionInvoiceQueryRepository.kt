package com.seaotter.infra.subscription.persistence.repository.query

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.infra.subscription.persistence.entity.QSubscriptionEntity
import com.seaotter.infra.subscription.persistence.entity.QSubscriptionInvoiceEntity
import com.seaotter.infra.subscription.persistence.entity.SubscriptionInvoiceEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionInvoiceEntityId
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class SubscriptionInvoiceQueryRepository(
  private val jpaQueryFactory: JPAQueryFactory,
  private val subscriptionInvoice: QSubscriptionInvoiceEntity = QSubscriptionInvoiceEntity.subscriptionInvoiceEntity
) {

  fun findById(enterpriseId: String, id: String): SubscriptionInvoiceEntity? {
    return jpaQueryFactory.selectFrom(subscriptionInvoice)
      .where(subscriptionInvoice.id.eq(SubscriptionInvoiceEntityId(enterpriseId, id)))
      .fetchFirst()
  }

  fun findAllBySubscriptionIdAndPayVersionNullable(
    enterpriseId: String,
    subscriptionId: String,
    payVersion: Long?
  ): List<SubscriptionInvoiceEntity> {
    return jpaQueryFactory.selectFrom(subscriptionInvoice)
      .where(
        subscriptionInvoice.id.enterpriseId.eq(enterpriseId)
          .and(subscriptionInvoice.subscriptionId.eq(subscriptionId))
          .and(if (payVersion == null) null else subscriptionInvoice.payVersion.eq(payVersion))
      ).fetch()
  }

  fun findAllByTargetBillingAtIsTodayAndStatusDraft_OrPaymentFailedWithin14Days(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate
  ): List<SubscriptionInvoiceEntity> {

    val builder = BooleanBuilder(
      subscriptionInvoice.targetBillingDate.eq(date)
        .and(subscriptionInvoice.status.eq(InvoiceStatus.DRAFT))
        .or(filterPaymentFailedStatusWithin14Days(date))
    )
    return jpaQueryFactory.selectFrom(subscriptionInvoice)
      .where(builder)
      .fetch()

  }

  // 자동 -> 사실상 DRAFT 인것만
  // 수동 -> PAYMENT_ON_FAILURE 도 가능은 함
  fun findAllByTargetBillingDateAndStatusEqual(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
    status: InvoiceStatus
  ): List<SubscriptionInvoiceEntity> {
    val builder = BooleanBuilder()
    if (enterpriseId != null && id == null) {
      builder.and(subscriptionInvoice.id.enterpriseId.eq(enterpriseId))
    }
    if (enterpriseId != null && id != null) {
      builder.and(subscriptionInvoice.id.eq(SubscriptionInvoiceEntityId(enterpriseId,id)))
    }

    builder.and(
      subscriptionInvoice.targetBillingDate.eq(date)
        .and(subscriptionInvoice.status.eq(status))
    )

    return jpaQueryFactory.selectFrom(subscriptionInvoice)
      .where(builder)
      .fetch()

  }

  fun findAllByPaymentFailedWithin14Days(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate
  ): List<SubscriptionInvoiceEntity> {
    val builder = BooleanBuilder()
    if (enterpriseId != null && id == null) {
      builder.and(subscriptionInvoice.id.enterpriseId.eq(enterpriseId))
    }
    if (enterpriseId != null && id != null) {
      builder.and(subscriptionInvoice.id.eq(SubscriptionInvoiceEntityId(enterpriseId,id)))
    }
    builder.and(filterPaymentFailedStatusWithin14Days(date))

    return jpaQueryFactory.selectFrom(subscriptionInvoice)
      .where(builder)
      .fetch()

  }

  fun filterPaymentFailedStatusWithin14Days(date: LocalDate): BooleanExpression {
    val minus14Date = date.minusDays(14)
    return subscriptionInvoice.targetBillingDate.goe(minus14Date)
      .and(subscriptionInvoice.status.eq(InvoiceStatus.PAYMENT_ON_FAILURE))
  }
}