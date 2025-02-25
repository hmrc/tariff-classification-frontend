/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import java.time.format.DateTimeFormatter
import java.time.{Duration, Instant, LocalDateTime, ZoneOffset}

object Dates {

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

  def format(instant: Instant): String =
    formatter.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC))

  def format(instant: Option[Instant]): String =
    instant.map(format).getOrElse("None")

  def daysCount(instant: Instant): Long = Duration.between(instant, Instant.now()).toDays
}
