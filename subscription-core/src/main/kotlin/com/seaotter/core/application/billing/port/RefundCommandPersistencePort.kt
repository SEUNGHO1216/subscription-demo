package com.seaotter.core.application.billing.port

import com.seaotter.core.domain.billing.Refund

interface RefundCommandPersistencePort {

  fun save(refund: Refund): Refund
}