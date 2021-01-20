/*
 * Copyright 2021 HM Revenue & Customs
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

package models

import java.time.Instant

import models.PseudoGroupingType.PseudoGroupingType
import models.PseudoReportColumns.PseudoReportColumns

case class ReportSettings(
  selectedDateRange: Option[ReportDates]    = None,
  grouping: PseudoGroupingType              = PseudoGroupingType.NONE,
  columns: Option[Set[PseudoReportColumns]] = None
)

case class ReportDates(chosenDateRange: String, from: Option[Instant], to: Option[Instant])

object PseudoDateRange extends Enumeration {
  type PseudoDateRange = Value
  val ALL_TIME, TODAY, YESTERDAY, LAST_SEVEN_DAYS, LAST_THIRTY_DAYS, LAST_CUSTOM_DAYS, THIS_MONTH, LAST_MONTH,
    CUSTOM_DATE, CUSTOM_DATE_RANGE = Value
}
