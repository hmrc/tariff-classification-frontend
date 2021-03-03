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
package reporting

import cats.syntax.all._
import models.BinderUtil._
import play.api.mvc.QueryStringBindable

sealed abstract class Report extends Product with Serializable {
  def name: String
  def sortBy: ReportField[_]
  def sortOrder: SortDirection.Value
  def caseTypes: Set[ApplicationType]
  def statuses: Set[PseudoCaseStatus.Value]
  def teams: Set[String]
  def dateRange: InstantRange
}

object Report {

  val numberOfOpenCases = SummaryReport(
    name      = "Number of open cases",
    groupBy   = ReportField.Team,
    sortBy    = ReportField.Team,
    sortOrder = SortDirection.ASCENDING,
    statuses  = Set(PseudoCaseStatus.OPEN)
  )

  val completedCases = SummaryReport(
    name      = "Completed cases",
    groupBy   = ReportField.Team,
    sortBy    = ReportField.Team,
    sortOrder = SortDirection.ASCENDING,
    statuses  = Set(PseudoCaseStatus.COMPLETED)
  )

  val numberOfCasesPerUser = SummaryReport(
    name      = "Number of cases per user",
    groupBy   = ReportField.User,
    sortBy    = ReportField.User,
    sortOrder = SortDirection.ASCENDING
  )

  val cancelledCasesPerUser = SummaryReport(
    name      = "Cancelled cases by assigned user",
    groupBy   = ReportField.User,
    sortBy    = ReportField.User,
    sortOrder = SortDirection.ASCENDING,
    statuses  = Set(PseudoCaseStatus.CANCELLED)
  )

  val cancelledCasesByChapter = SummaryReport(
    name      = "Cancelled cases by chapter",
    groupBy   = ReportField.Chapter,
    sortBy    = ReportField.Chapter,
    sortOrder = SortDirection.ASCENDING,
    statuses  = Set(PseudoCaseStatus.CANCELLED)
  )

  val liabilitiesSummary = CaseReport(
    name      = "Liabilities summary",
    sortBy    = ReportField.ElapsedDays,
    sortOrder = SortDirection.DESCENDING,
    caseTypes = Set(ApplicationType.LIABILITY),
    fields = Seq(
      ReportField.Reference,
      ReportField.GoodsName,
      ReportField.TraderName,
      ReportField.Status,
      ReportField.Chapter,
      ReportField.Team,
      ReportField.User,
      ReportField.ElapsedDays
    )
  )

  val atarSummary = CaseReport(
    name      = "ATaR summary",
    sortBy    = ReportField.ElapsedDays,
    sortOrder = SortDirection.DESCENDING,
    caseTypes = Set(ApplicationType.ATAR),
    fields = Seq(
      ReportField.Reference,
      ReportField.GoodsName,
      ReportField.TraderName,
      ReportField.Status,
      ReportField.Chapter,
      ReportField.Team,
      ReportField.User,
      ReportField.ElapsedDays
    )
  )

  val liabilitiesCases = CaseReport(
    name      = "Liabilities cases",
    sortBy    = ReportField.ElapsedDays,
    sortOrder = SortDirection.DESCENDING,
    caseTypes = Set(ApplicationType.LIABILITY),
    fields = Seq(
      ReportField.Reference,
      ReportField.GoodsName,
      ReportField.TraderName,
      ReportField.Status,
      ReportField.Team,
      ReportField.User,
      ReportField.ElapsedDays
    )
  )

  val byId = Map[String, Report](
    "number-of-open-cases"             -> numberOfOpenCases,
    "completed-cases"                  -> completedCases,
    "number-of-cases-per-user"         -> numberOfCasesPerUser,
    "cancelled-cases-by-assigned-user" -> cancelledCasesPerUser,
    "cancelled-cases-by-chapter"       -> cancelledCasesByChapter,
    "liabilities-summary"              -> liabilitiesSummary,
    "atar-summary"                     -> atarSummary,
    "liabilities-cases"                -> liabilitiesCases
  )

  private val groupByKey      = "group_by"
  private val maxFieldsKey    = "max_fields"
  private val includeCasesKey = "include_cases"

  implicit val reportQueryStringBindable: QueryStringBindable[Report] =
    new QueryStringBindable[Report] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Report]] =
        if (params.contains(groupByKey) || params.contains(maxFieldsKey) || params.contains(includeCasesKey))
          SummaryReport.summaryReportQueryStringBindable.bind(key, params)
        else
          CaseReport.caseReportQueryStringBindable.bind(key, params)

      override def unbind(key: String, value: Report): String = value match {
        case cse: CaseReport =>
          CaseReport.caseReportQueryStringBindable.unbind(key, cse)
        case sum: SummaryReport =>
          SummaryReport.summaryReportQueryStringBindable.unbind(key, sum)
        case queue: QueueReport =>
          QueueReport.queueReportQueryStringBindable.unbind(key, queue)
      }
    }
}

case class SummaryReport(
  name: String,
  groupBy: ReportField[_],
  sortBy: ReportField[_],
  sortOrder: SortDirection.Value        = SortDirection.ASCENDING,
  caseTypes: Set[ApplicationType]       = Set.empty,
  statuses: Set[PseudoCaseStatus.Value] = Set.empty,
  teams: Set[String]                    = Set.empty,
  dateRange: InstantRange               = InstantRange.allTime,
  maxFields: Set[ReportField[Long]]     = Set.empty,
  includeCases: Boolean                 = false
) extends Report

object SummaryReport {
  private val nameKey         = "name"
  private val dateRangeKey    = "date"
  private val groupByKey      = "group_by"
  private val sortByKey       = "sort_by"
  private val sortOrderKey    = "sort_order"
  private val caseTypesKey    = "case_type"
  private val teamsKey        = "team"
  private val maxFieldsKey    = "max_fields"
  private val includeCasesKey = "include_cases"
  private val statusesKey     = "status"

  implicit def summaryReportQueryStringBindable(
    implicit
    stringBindable: QueryStringBindable[String],
    boolBindable: QueryStringBindable[Boolean],
    rangeBindable: QueryStringBindable[InstantRange]
  ): QueryStringBindable[SummaryReport] = new QueryStringBindable[SummaryReport] {
    override def bind(key: String, requestParams: Map[String, Seq[String]]): Option[Either[String, SummaryReport]] = {
      val reportName   = stringBindable.bind(nameKey, requestParams)
      val includeCases = boolBindable.bind(includeCasesKey, requestParams).getOrElse(Right(false))
      val dateRange    = rangeBindable.bind(dateRangeKey, requestParams).getOrElse(Right(InstantRange.allTime))
      val groupBy      = param(groupByKey)(requestParams).flatMap(ReportField.fields.get(_))
      val sortBy       = param(sortByKey)(requestParams).flatMap(ReportField.fields.get(_)).orElse(groupBy)
      val sortOrder    = param(sortOrderKey)(requestParams).flatMap(bindSortDirection).getOrElse(SortDirection.ASCENDING)
      val teams        = params(teamsKey)(requestParams).getOrElse(Set.empty)
      val caseTypes = params(caseTypesKey)(requestParams)
        .map(_.map(bindApplicationType).collect { case Some(value) => value })
        .getOrElse(Set.empty)
      val statuses = params(statusesKey)(requestParams)
        .map(_.map(bindPseudoCaseStatus).collect { case Some(status) => status })
        .getOrElse(Set.empty)
      val maxFields = params(maxFieldsKey)(requestParams)
        .map(_.flatMap(ReportField.fields.get(_).collect[ReportField[Long]] {
          case days @ DaysSinceField(_) => days
          case num @ NumberField(_)     => num
        }))
        .getOrElse(Set.empty)
      (reportName, groupBy, sortBy).mapN {
        case (reportName, groupBy, sortBy) =>
          for {
            name    <- reportName
            range   <- dateRange
            include <- includeCases
          } yield SummaryReport(
            name         = name,
            dateRange    = range,
            groupBy      = groupBy,
            sortBy       = sortBy,
            sortOrder    = sortOrder,
            caseTypes    = caseTypes,
            statuses     = statuses,
            teams        = teams,
            maxFields    = maxFields,
            includeCases = include
          )
      }
    }

    override def unbind(key: String, value: SummaryReport): String =
      Seq(
        stringBindable.unbind(nameKey, value.name),
        stringBindable.unbind(groupByKey, value.groupBy.fieldName),
        stringBindable.unbind(sortByKey, value.sortBy.fieldName),
        stringBindable.unbind(sortOrderKey, value.sortOrder.toString),
        stringBindable.unbind(caseTypesKey, value.caseTypes.map(_.name).mkString(",")),
        stringBindable.unbind(statusesKey, value.statuses.map(_.toString).mkString(",")),
        stringBindable.unbind(teamsKey, value.teams.mkString(",")),
        rangeBindable.unbind(dateRangeKey, value.dateRange),
        stringBindable.unbind(maxFieldsKey, value.maxFields.map(_.fieldName).mkString(",")),
        boolBindable.unbind(includeCasesKey, value.includeCases)
      ).filterNot(_.isEmpty).mkString("&")
  }
}

case class CaseReport(
  name: String,
  sortBy: ReportField[_]                = ReportField.Reference,
  sortOrder: SortDirection.Value        = SortDirection.ASCENDING,
  caseTypes: Set[ApplicationType]       = Set.empty,
  statuses: Set[PseudoCaseStatus.Value] = Set.empty,
  teams: Set[String]                    = Set.empty,
  dateRange: InstantRange               = InstantRange.allTime,
  fields: Seq[ReportField[_]]           = Seq.empty
) extends Report

object CaseReport {
  private val nameKey      = "name"
  private val dateRangeKey = "date"
  private val sortByKey    = "sort_by"
  private val sortOrderKey = "sort_order"
  private val caseTypesKey = "case_type"
  private val teamsKey     = "team"
  private val fieldsKey    = "fields"
  private val statusesKey  = "status"

  implicit def caseReportQueryStringBindable(
    implicit
    stringBindable: QueryStringBindable[String],
    rangeBindable: QueryStringBindable[InstantRange]
  ): QueryStringBindable[CaseReport] = new QueryStringBindable[CaseReport] {
    override def bind(key: String, requestParams: Map[String, Seq[String]]): Option[Either[String, CaseReport]] = {
      val reportName = stringBindable.bind(nameKey, requestParams)
      val sortBy     = param(sortByKey)(requestParams).flatMap(ReportField.fields.get(_)).getOrElse(ReportField.Reference)
      val sortOrder  = param(sortOrderKey)(requestParams).flatMap(bindSortDirection).getOrElse(SortDirection.ASCENDING)
      val dateRange  = rangeBindable.bind(dateRangeKey, requestParams).getOrElse(Right(InstantRange.allTime))
      val teams      = params(teamsKey)(requestParams).getOrElse(Set.empty)
      val caseTypes = params(caseTypesKey)(requestParams)
        .map(_.map(bindApplicationType).collect { case Some(value) => value })
        .getOrElse(Set.empty)
      val statuses = params(statusesKey)(requestParams)
        .map(_.map(bindPseudoCaseStatus).collect { case Some(status) => status })
        .getOrElse(Set.empty)
      val fields = orderedParams(fieldsKey)(requestParams)
        .map(_.flatMap(ReportField.fields.get(_)))

      (reportName, fields).mapN {
        case (reportName, fields) =>
          for {
            name  <- reportName
            range <- dateRange
          } yield CaseReport(
            name      = name,
            sortBy    = sortBy,
            sortOrder = sortOrder,
            caseTypes = caseTypes,
            statuses  = statuses,
            teams     = teams,
            dateRange = range,
            fields    = fields
          )
      }
    }

    override def unbind(key: String, value: CaseReport): String =
      Seq(
        stringBindable.unbind(nameKey, value.name),
        stringBindable.unbind(sortByKey, value.sortBy.fieldName),
        stringBindable.unbind(sortOrderKey, value.sortOrder.toString),
        stringBindable.unbind(caseTypesKey, value.caseTypes.map(_.name).mkString(",")),
        stringBindable.unbind(statusesKey, value.statuses.map(_.toString).mkString(",")),
        stringBindable.unbind(teamsKey, value.teams.mkString(",")),
        rangeBindable.unbind(dateRangeKey, value.dateRange),
        stringBindable.unbind(fieldsKey, value.fields.map(_.fieldName).mkString(","))
      ).filterNot(_.isEmpty).mkString("&")
  }
}

case class QueueReport(
  sortBy: ReportField[_]                = ReportField.Team,
  sortOrder: SortDirection.Value        = SortDirection.ASCENDING,
  caseTypes: Set[ApplicationType]       = Set.empty,
  statuses: Set[PseudoCaseStatus.Value] = Set.empty,
  teams: Set[String]                    = Set.empty,
  assignee: Option[String]              = Option.empty,
  dateRange: InstantRange               = InstantRange.allTime
) extends Report {
  override val name = "Cases by queue"
}

object QueueReport {
  private val nameKey      = "name"
  private val dateRangeKey = "date"
  private val sortByKey    = "sort_by"
  private val sortOrderKey = "sort_order"
  private val caseTypesKey = "case_type"
  private val teamsKey     = "team"
  private val statusesKey  = "status"
  private val assigneeKey  = "assigned_user"

  implicit def queueReportQueryStringBindable(
    implicit
    stringBindable: QueryStringBindable[String],
    rangeBindable: QueryStringBindable[InstantRange]
  ): QueryStringBindable[QueueReport] = new QueryStringBindable[QueueReport] {
    override def bind(key: String, requestParams: Map[String, Seq[String]]): Option[Either[String, QueueReport]] = {
      val sortBy    = param(sortByKey)(requestParams).flatMap(ReportField.fields.get(_)).getOrElse(ReportField.Team)
      val sortOrder = param(sortOrderKey)(requestParams).flatMap(bindSortDirection).getOrElse(SortDirection.ASCENDING)
      val dateRange = rangeBindable.bind(dateRangeKey, requestParams).getOrElse(Right(InstantRange.allTime))
      val teams     = params(teamsKey)(requestParams).getOrElse(Set.empty)
      val assignee  = param(assigneeKey)(requestParams)
      val caseTypes = params(caseTypesKey)(requestParams)
        .map(_.map(bindApplicationType).collect { case Some(value) => value })
        .getOrElse(Set.empty)
      val statuses = params(statusesKey)(requestParams)
        .map(_.map(bindPseudoCaseStatus).collect { case Some(status) => status })
        .getOrElse(Set.empty)

      Some(dateRange.map { range =>
        QueueReport(
          sortBy    = sortBy,
          sortOrder = sortOrder,
          caseTypes = caseTypes,
          statuses  = statuses,
          teams     = teams,
          dateRange = range,
          assignee  = assignee
        )
      })
    }

    override def unbind(key: String, value: QueueReport): String =
      Seq(
        stringBindable.unbind(nameKey, value.name),
        stringBindable.unbind(sortByKey, value.sortBy.fieldName),
        stringBindable.unbind(sortOrderKey, value.sortOrder.toString),
        stringBindable.unbind(caseTypesKey, value.caseTypes.map(_.name).mkString(",")),
        stringBindable.unbind(statusesKey, value.statuses.map(_.toString).mkString(",")),
        stringBindable.unbind(teamsKey, value.teams.mkString(",")),
        rangeBindable.unbind(dateRangeKey, value.dateRange),
        value.assignee.map(stringBindable.unbind(assigneeKey, _)).getOrElse("")
      ).filterNot(_.isEmpty).mkString("&")
  }
}
