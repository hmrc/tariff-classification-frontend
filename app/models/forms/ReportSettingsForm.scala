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

package models.forms

import java.time.Instant

import models.PseudoReportColumns.PseudoReportColumns
import models.{ReportOptions, _}
import models.forms.FormUtils._
import models.forms.mappings.FormMappings.oneOf
import play.api.data.{Form, Mapping}
import play.api.data.Forms._

object ReportSettingsForm {

  val form: Form[ReportOptions] = Form(
    mapping(
      "selectedDateRange" -> reportDatesMapping,
      "grouping"          -> oneOf("error", PseudoGroupingType),
      "columns" -> optional[Set[PseudoReportColumns]](
        set(textTransformingTo(PseudoReportColumns.withName, _.toString))
      )
    )(form2Settings)(settings2Form)
  )

  private val form2Settings: (ReportDates, String, Option[Set[PseudoReportColumns]]) => ReportOptions = {
    case (selectedDateRange, grouping, columns) =>
      PseudoGroupingType.withName(grouping) match {
        case PseudoGroupingType.NONE => ReportColumnsOptions(selectedDateRange, columns)
        case _                       => ReportGroupingOptions(selectedDateRange, PseudoGroupingType.withName(grouping))
      }

  }

  private val settings2Form: ReportOptions => Option[(ReportDates, String, Option[Set[PseudoReportColumns]])] =
    reportSettings => {
      reportSettings.`type` match {
        case ReportOptionsType.GROUPING_OPTIONS =>
          Some(
            (
              reportSettings.selectedDateRange,
              reportSettings.asInstanceOf[ReportGroupingOptions].grouping.toString,
              None
            )
          )
        case ReportOptionsType.COLUMNS_OPTIONS =>
          Some(
            (
              reportSettings.selectedDateRange,
              PseudoGroupingType.NONE.toString,
              reportSettings.asInstanceOf[ReportColumnsOptions].columns
            )
          )
      }
    }
  private def reportDatesMapping: Mapping[ReportDates] =
    mapping(
      "chosenDateRange" -> oneOf("error", PseudoDateRange),
      "from" -> optional(
        FormDate
          .date("case.liability.error.date-of-repayment")
      ),
      "to" -> optional(
        FormDate
          .date("case.liability.error.date-of-repayment")
      ),
      "numberOfDays" -> optional(text)
    )(form2Dates)(dates2Form)

  private val form2Dates: (String, Option[Instant], Option[Instant], Option[String]) => ReportDates = {
    case (chosenDateRange, from, to, numberOfDays) =>
      PseudoDateRange.withName(chosenDateRange) match {
        case PseudoDateRange.LAST_CUSTOM_DAYS  => CustomRelativeDateRange(numberOfDays.getOrElse("0").toInt)
        case PseudoDateRange.CUSTOM_DATE_RANGE => CustomDateRange(from.get, to.get)
        case PseudoDateRange.CUSTOM_DATE       => CustomDate(from.get)
        case _                                 => RelativeDateRange(PseudoDateRange.withName(chosenDateRange))
      }
  }

  private val dates2Form: ReportDates => Option[(String, Option[Instant], Option[Instant], Option[String])] =
    reportDates => {
      reportDates.`type` match {
        case ReportDatesType.CUSTOM_DATE => Some(("", Some(reportDates.asInstanceOf[CustomDate].from), None, None))
        case ReportDatesType.CUSTOM_DATE_RANGE =>
          Some(
            (
              "",
              Some(reportDates.asInstanceOf[CustomDateRange].from),
              Some(reportDates.asInstanceOf[CustomDateRange].to),
              None
            )
          )
        case ReportDatesType.RELATIVE_DATE_RANGE =>
          Some((reportDates.asInstanceOf[RelativeDateRange].relativeDateRange.toString, None, None, None))
        case ReportDatesType.CUSTOM_RELATIVE_DATE_RANGE =>
          Some(
            (
              PseudoDateRange.LAST_CUSTOM_DAYS.toString,
              None,
              None,
              Some(reportDates.asInstanceOf[CustomRelativeDateRange].numberOfDays.toString)
            )
          )
      }
    }

}
