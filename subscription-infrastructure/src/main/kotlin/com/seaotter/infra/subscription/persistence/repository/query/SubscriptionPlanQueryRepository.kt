package com.seaotter.infra.subscription.persistence.repository.query

import com.querydsl.jpa.impl.JPAQueryFactory
import com.seaotter.core.domain.subscription.SubscriptionPlan
import com.seaotter.infra.subscription.persistence.entity.QSubscriptionPlanEntity
import com.seaotter.infra.subscription.persistence.entity.SubscriptionPlanEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionPlanEntityId
import org.springframework.stereotype.Repository

@Repository
class SubscriptionPlanQueryRepository(
  private val jpaQueryFactory: JPAQueryFactory,
  private val subscriptionPlan: QSubscriptionPlanEntity = QSubscriptionPlanEntity.subscriptionPlanEntity
) {

  fun findById(
    enterpriseId: String,
    planId: String
  ): SubscriptionPlanEntity? {
    return jpaQueryFactory.selectFrom(subscriptionPlan)
      .where(
        subscriptionPlan.id.eq(SubscriptionPlanEntityId(enterpriseId, planId))
          .and(subscriptionPlan.deletedAt.isNull)
      )
      .fetchFirst()
  }

  fun findAllByStoreIdAndSubscriptionProductId(
    enterpriseId: String,
    storeId: String?,
    productId: String?
  ): List<SubscriptionPlanEntity> {

    return jpaQueryFactory.selectFrom(subscriptionPlan)
      .where(
        subscriptionPlan.id.enterpriseId.eq(enterpriseId)
          .and(if(storeId == null) null else subscriptionPlan.storeId.eq(storeId))
          .and(if(productId == null) null else subscriptionPlan.subscriptionProductId.eq(productId))
          .and(subscriptionPlan.deletedAt.isNull)
      ).fetch()
  }

  fun findAllByIdList(
    enterpriseId: String,
    idList: List<String>
  ): List<SubscriptionPlanEntity> {
    val targetIdList = idList.map { SubscriptionPlanEntityId(enterpriseId, it) }
    return jpaQueryFactory.selectFrom(subscriptionPlan)
      .where(
        subscriptionPlan.id.`in`(targetIdList)
          .and(subscriptionPlan.deletedAt.isNull)
      ).fetch()
  }

  fun findAllByProductIdList(
    enterpriseId: String,
    productIdList: List<String>
  ): List<SubscriptionPlanEntity> {
    return jpaQueryFactory.selectFrom(subscriptionPlan)
      .where(
        subscriptionPlan.id.enterpriseId.eq(enterpriseId)
          .and(subscriptionPlan.subscriptionProductId.`in`(productIdList))
          .and(subscriptionPlan.deletedAt.isNull)
      ).fetch()
  }
}