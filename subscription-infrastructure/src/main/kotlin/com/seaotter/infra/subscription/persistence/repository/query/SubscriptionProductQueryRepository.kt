package com.seaotter.infra.subscription.persistence.repository.query

import com.querydsl.jpa.impl.JPAQueryFactory
import com.seaotter.infra.subscription.persistence.entity.QSubscriptionPlanEntity
import com.seaotter.infra.subscription.persistence.entity.QSubscriptionProductEntity
import com.seaotter.infra.subscription.persistence.entity.SubscriptionProductEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionProductEntityId
import org.springframework.stereotype.Repository

@Repository
class SubscriptionProductQueryRepository(
  private val jpaQueryFactory: JPAQueryFactory,
  private val subscriptionProduct: QSubscriptionProductEntity = QSubscriptionProductEntity.subscriptionProductEntity,
) {
  fun findById(
    enterpriseId: String,
    id: String
  ): SubscriptionProductEntity? {
    return jpaQueryFactory.selectFrom(subscriptionProduct)
      .where(
        subscriptionProduct.id.eq(SubscriptionProductEntityId(enterpriseId, id))
          .and(subscriptionProduct.deletedAt.isNull())
      )
      .fetchFirst()
  }

  fun findAllByStoreId(
    enterpriseId: String,
    storeId: String
  ): List<SubscriptionProductEntity> {
    return jpaQueryFactory.selectFrom(subscriptionProduct)
      .where(
        subscriptionProduct.id.enterpriseId.eq(enterpriseId)
          .and(subscriptionProduct.storeId.eq(storeId))
          .and(subscriptionProduct.deletedAt.isNull())
      ).fetch()
  }

  fun findAllByIdList(
    enterpriseId: String,
    idList: List<String>
  ): List<SubscriptionProductEntity> {
    val productIdList = idList.map { SubscriptionProductEntityId(enterpriseId,it) }
    return jpaQueryFactory.selectFrom(subscriptionProduct)
      .where(
        subscriptionProduct.id.`in`(productIdList)
          .and(subscriptionProduct.deletedAt.isNull())
      ).fetch()
  }
}