package com.seaotter.core.application.subscription.service

import com.seaotter.core.application.subscription.port.out.SubscriptionCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceCommandPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionInvoiceQueryPersistencePort
import com.seaotter.core.application.subscription.port.out.SubscriptionQueryPersistencePort
import com.seaotter.core.domain.subscription.InvoiceStatus
import com.seaotter.core.domain.subscription.SubscriptionInvoice
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * 정기결제 한 건을 '선점 → PG 호출 → 결과 반영' 세 단계로 나눌 때, DB를 다루는 앞뒤 단계를 맡는다.
 *
 * PG 호출은 이 클래스 밖, 트랜잭션 밖에서 한다.
 *  - DB 커넥션을 PG 응답 시간 동안 붙잡지 않는다.
 *  - 선점(claim)을 먼저 커밋해 두면, 다른 인스턴스는 PROCESSING 송장을 보고 결제하지 않는다. 중복 결제를 막는 핵심이다.
 */
@Component
class InvoiceBillingStep(
  private val subscriptionInvoiceQueryPersistencePort: SubscriptionInvoiceQueryPersistencePort,
  private val subscriptionInvoiceCommandPersistencePort: SubscriptionInvoiceCommandPersistencePort,
  private val subscriptionQueryPersistencePort: SubscriptionQueryPersistencePort,
  private val subscriptionCommandPersistencePort: SubscriptionCommandPersistencePort,
) {

  /**
   * 결제할 송장을 PROCESSING 으로 바꿔 선점한다.
   * @return 선점한 송장. 이미 다른 곳에서 처리 중이거나 끝난 송장이면 null
   * 동시에 두 곳이 선점하면 늦게 커밋한 쪽에서 낙관적 락 예외가 난다(호출자가 '건너뜀'으로 처리).
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun claim(enterpriseId: String, invoiceId: String): SubscriptionInvoice? {
    val invoice = subscriptionInvoiceQueryPersistencePort.findById(enterpriseId, invoiceId) ?: return null
    if (!invoice.isBillable()) return null
    subscriptionInvoiceCommandPersistencePort.save(invoice.startProcessing())
    return invoice
  }

  /** PG 결과를 송장과 구독에 함께 반영한다. 선점한 송장(PROCESSING)에만 반영한다. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun applyResult(enterpriseId: String, invoiceId: String, paySucceeded: Boolean) {
    val invoice = requireNotNull(subscriptionInvoiceQueryPersistencePort.findById(enterpriseId, invoiceId)) {
      "결과를 반영할 송장이 없습니다. enterpriseId=$enterpriseId, invoiceId=$invoiceId"
    }
    check(invoice.state.status == InvoiceStatus.PROCESSING) {
      "선점하지 않은 송장에는 결제 결과를 반영하지 않습니다. status=${invoice.state.status}"
    }
    val subscription = requireNotNull(subscriptionQueryPersistencePort.findById(enterpriseId, invoice.subscriptionId)) {
      "송장의 구독이 없습니다. enterpriseId=$enterpriseId, subscriptionId=${invoice.subscriptionId}"
    }

    if (paySucceeded) {
      invoice.successPay()
      subscription.paySuccess(invoice.payVersion)
    } else {
      invoice.failPay()
      subscription.payFail(invoice.state.retryCount)
    }
    subscriptionInvoiceCommandPersistencePort.save(invoice)
    subscriptionCommandPersistencePort.save(subscription)
  }
}
