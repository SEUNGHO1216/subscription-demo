package com.seaotter.infra.billing.pg.util

import java.text.SimpleDateFormat
import java.util.Date

class DateUtil {

  companion object {
    fun getNow_YYYYMMDDHHMMSS(): String {
      return convertTimeToTargetPattern("yyyyMMddHHmmss")
    }

    private fun convertTimeToTargetPattern(pattern: String): String {
      return SimpleDateFormat(pattern).run { this.format(Date()) }
    }
  }
}