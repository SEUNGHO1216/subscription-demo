package com.seaotter.core.domain.common

import java.time.LocalDateTime

data class BaseInfo(
  val createdAt: LocalDateTime,
  var updatedAt: LocalDateTime = createdAt,
  var deletedAt: LocalDateTime? = null,
  val createdBy: String,
  var updatedBy: String = createdBy,
  var deletedBy: String? = null,
)