package com.seaotter.infra.subscription.persistence.repository.query

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import com.seaotter.core.domain.subscription.SubscriptionStatus
import com.seaotter.infra.subscription.persistence.entity.QSubscriptionEntity
import com.seaotter.infra.subscription.persistence.entity.SubscriptionEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionEntityId
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class SubscriptionQueryRepository(
  private val jpaQueryFactory: JPAQueryFactory,
  private val subscription: QSubscriptionEntity = QSubscriptionEntity.subscriptionEntity
) {

  fun findById(
    enterpriseId: String,
    id: String
  ): SubscriptionEntity? {
    return jpaQueryFactory.selectFrom(subscription)
      .where(subscription.id.eq(SubscriptionEntityId(enterpriseId, id)))
      .fetchFirst()
  }

  fun findAllByMemberId(
    enterpriseId: String,
    memberId: String
  ): List<SubscriptionEntity> {
    return jpaQueryFactory.selectFrom(subscription)
      .where(
        subscription.id.enterpriseId.eq(enterpriseId)
          .and(subscription.memberId.eq(memberId))
      )
      .fetch()
  }

  fun findAllByNextBillingAtIsTodayAndStatus(
    enterpriseId: String? = null,
    id: String? = null,
    date: LocalDate,
    status: SubscriptionStatus? = null
  ): List<SubscriptionEntity> {
    val builder: BooleanBuilder = BooleanBuilder()
    if(enterpriseId != null && id == null) {
      builder.and(subscription.id.enterpriseId.eq(enterpriseId))
    }
    if(enterpriseId != null && id != null) {
      builder.and(subscription.id.eq(SubscriptionEntityId(enterpriseId,id)))
    }
    builder.and(
      subscription.nextBillingDate.eq(date)
        .and(if (status == null) null else subscription.status.eq(status))
    )

    return jpaQueryFactory.selectFrom(subscription)
      .where(builder)
      .fetch()
  }

  fun findAllByIdList(
    idList: List<SubscriptionEntityId>
  ): List<SubscriptionEntity> {
    return jpaQueryFactory.selectFrom(subscription)
      .where(subscription.id.`in`(idList))
      .fetch()
  }
}