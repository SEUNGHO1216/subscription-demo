package com.seaotter.infra.subscription.persistence.repository

import com.seaotter.infra.subscription.persistence.entity.SubscriptionProductEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionProductEntityId
import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionProductCommandRepository: JpaRepository<SubscriptionProductEntity, SubscriptionProductEntityId> {
}