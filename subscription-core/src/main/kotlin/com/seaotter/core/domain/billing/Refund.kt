package com.seaotter.core.domain.billing

import com.github.f4b6a3.ulid.UlidCreator
import java.time.LocalDateTime
import java.util.UUID

class Refund(
  val enterpriseId: String,
  val id: String = UlidCreator.getUlid().toString() + UUID.randomUUID().toString().split("-")[0],
  val storeId: String,
  val paymentId: String,
  val memberId: String,
  val resultCode: String,
  val resultMsg: String,
  val tid: String,
  val cancelDate: String,
  val cancelTime: String,
  val cancelAmount: Long,
  val remainAmount: Long,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)