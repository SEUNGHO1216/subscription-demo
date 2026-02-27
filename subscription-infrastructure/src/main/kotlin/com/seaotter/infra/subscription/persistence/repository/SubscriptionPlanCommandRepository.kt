package com.seaotter.infra.subscription.persistence.repository

import com.seaotter.infra.subscription.persistence.entity.SubscriptionPlanEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionPlanEntityId
import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionPlanCommandRepository: JpaRepository<SubscriptionPlanEntity, SubscriptionPlanEntityId> {
}