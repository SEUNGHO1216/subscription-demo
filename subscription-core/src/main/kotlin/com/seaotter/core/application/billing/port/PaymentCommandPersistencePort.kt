package com.seaotter.core.application.billing.port

import com.seaotter.core.domain.billing.Payment

interface PaymentCommandPersistencePort {

  fun save(payment: Payment): Payment
}