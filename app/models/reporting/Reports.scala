/*
 * Copyright 2024 HM Revenue & Customs
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
package reporting

import play.api.i18n.Messages
import utils.Dates

object Reports {
  private def formatUserForPid(pid: String, users: Map[String, Operator])(implicit messages: Messages): String =
    users
      .get(pid)
      .flatMap(_.name)
      .filterNot(_.isEmpty)
      .getOrElse(messages("reporting.user.unknown", pid))

  private def formatTeamForId(teamId: String, teams: Map[String, Queue])(implicit messages: Messages): String =
    teams
      .get(teamId)
      .map(_.name)
      .getOrElse(messages("reporting.team.unknown"))

  def formatField(
    field: ReportField[_],
    result: ReportResultField[_],
    usersByPid: Map[String, Operator],
    teamsById: Map[String, Queue]
  )(implicit messages: Messages): String = result match {
    case CaseTypeResultField(_, data) =>
      data.map(_.prettyName).getOrElse(messages("reporting.result.unknown"))
    case DateResultField(_, data) =>
      Dates.format(data)
    case NumberResultField(_, data) =>
      data.map(_.toString).getOrElse("0")
    case StatusResultField(_, data) =>
      data.map(_.toString).getOrElse(messages("reporting.result.unknown"))
    case LiabilityStatusResultField(_, data) =>
      data.map(_.toString).getOrElse(messages("reporting.result.unknown"))
    case StringResultField(_, data) =>
      field match {
        case ReportField.User =>
          data.map(formatUserForPid(_, usersByPid)).getOrElse(messages("reporting.user.unassigned"))
        case ReportField.Team =>
          data.map(formatTeamForId(_, teamsById)).getOrElse(messages("reporting.team.unassigned"))
        case _ =>
          data.getOrElse(messages("reporting.result.unknown"))
      }
  }

  def formatCaseReport(report: CaseReport, usersByPid: Map[String, Operator], teamsById: Map[String, Queue])(
    row: Map[String, ReportResultField[_]]
  )(implicit messages: Messages): List[String] =
    report.fields.toSeq.map { field =>
      row
        .get(field.fieldName)
        .map(formatField(field, _, usersByPid, teamsById))
        .getOrElse {
          messages("reporting.result.unknown")
        }
    }.toList

  def formatSummaryReport(
    report: SummaryReport,
    usersByPid: Map[String, Operator],
    teamsById: Map[String, Queue]
  )(
    row: ResultGroup
  )(implicit messages: Messages): List[String] = {
    val group = report.groupBy.zipWith(row.groupKey) {
      case (groupBy, groupKey) =>
        formatField(groupBy, groupKey, usersByPid, teamsById)
    }

    val count = row.count.toString

    val maxFields = report.maxFields.zip(row.maxFields).map {
      case (field, result) =>
        formatField(field, result, usersByPid, teamsById)
    }

    (group.toSeq ++ Seq(count) ++ maxFields).toList
  }

  def formatQueueReport(teamsById: Map[String, Queue])(
    row: QueueResultGroup
  )(implicit messages: Messages): List[String] =
    List(
      row.team.map(formatTeamForId(_, teamsById)).getOrElse(messages("reporting.team.unassigned")),
      row.caseType.prettyName,
      row.count.toString
    )

  def formatHeaders(report: Report)(implicit messages: Messages): List[String] =
    report.fields.toSeq.map(field => messages(s"reporting.field.${field.fieldName}")).toList
}
