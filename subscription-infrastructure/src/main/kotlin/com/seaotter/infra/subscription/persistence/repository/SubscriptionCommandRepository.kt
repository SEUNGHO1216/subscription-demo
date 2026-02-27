package com.seaotter.infra.subscription.persistence.repository

import com.seaotter.infra.subscription.persistence.entity.SubscriptionEntity
import com.seaotter.infra.subscription.persistence.entity.id.SubscriptionEntityId
import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionCommandRepository: JpaRepository<SubscriptionEntity, SubscriptionEntityId> {
}