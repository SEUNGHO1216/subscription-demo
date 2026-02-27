package com.seaotter.infra.common

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseInfoEntity(
  @CreatedDate
  var createdAt: LocalDateTime = LocalDateTime.now(),
  @LastModifiedDate
  var updatedAt: LocalDateTime = createdAt,
  var deletedAt: LocalDateTime? = null,
  @CreatedBy
  var createdBy: String = "system",
  @LastModifiedBy
  var updatedBy: String = createdBy,
  var deletedBy: String? = null,
) {

}