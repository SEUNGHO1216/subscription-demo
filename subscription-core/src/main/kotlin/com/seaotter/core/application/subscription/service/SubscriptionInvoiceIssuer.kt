package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.subscription.port.out.SubscriptionCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionQueryPersistencePort
import com.seaotter.core.domain.subscription.InvoiceState
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import com.seaotter.core.domain.subscription.SubscriptionStatus
import com.seaotter.core.domain.subscription.service.InvoicePricing
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * 구독 하나에 대한 송장 발행을 독립된 트랜잭션으로 처리한다.
 *
 * 같은 회차 송장이 두 번 생기지 않도록 두 겹으로 막는다.
 *  1. 구독의 낙관적 락(@Version): 두 인스턴스가 같은 구독을 동시에 읽고 송장 잠금을 걸면, 늦게 커밋한 쪽이 실패한다.
 *  2. 송장 테이블의 유니크 제약 (enterprise_id, subscription_id, target_billing_at): 1이 뚫려도 DB가 마지막에 막는다.
 *
 * 한 건이 실패해도 다른 구독의 송장 발행에 영향을 주지 않도록 REQUIRES_NEW 로 건마다 커밋한다.
 */
@Component
class SubscriptionInvoiceIssuer(
  private val subscriptionQueryPersistencePort: SubscriptionQueryPersistencePort,
  private val subscriptionCommandPersistencePort: SubscriptionCommandPersistencePort,
  private val subscriptionInvoiceCommandPersistencePort: SubscriptionInvoiceCommandPersistencePort,
) {

  /** @return 송장을 발행했으면 true, 이미 발행됐거나 발행 대상이 아니면 false */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun issue(enterpriseId: String, subscriptionId: String): Boolean {
    // 목록을 조회한 뒤 다른 인스턴스가 먼저 처리했을 수 있으므로, 트랜잭션 안에서 다시 읽어 판단한다.
    val subscription = subscriptionQueryPersistencePort.findById(enterpriseId, subscriptionId) ?: return false
    val nextBillingAt = subscription.nextBillingAt
    if (subscription.status != SubscriptionStatus.ACTIVE || subscription.isLockGeneratingInvoice || nextBillingAt == null) {
      return false
    }

    subscriptionInvoiceCommandPersistencePort.save(
      SubscriptionInvoice(
        enterpriseId = subscription.enterpriseId,
        subscriptionId = subscription.id,
        memberId = subscription.memberId,
        storeId = subscription.storeId,
        payVersion = subscription.payVersion,
        planSnapshot = subscription.planSnapshot,
        state = InvoiceState(price = InvoicePricing.priceOf(subscription)),
        targetBillingAt = nextBillingAt,
        billKey = subscription.billingKey,
        lockGeneratingInvoice = subscription.isLockGeneratingInvoice,
      )
    )
    subscriptionCommandPersistencePort.save(subscription.lockGeneratingInvoice())
    return true
  }
}
