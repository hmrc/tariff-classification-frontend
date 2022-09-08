/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import akka.stream.alpakka.csv.scaladsl.CsvFormatting
import akka.stream.scaladsl.Source
import cats.syntax.all._
import config.AppConfig
import models._
import models.forms._
import models.reporting._
import models.viewmodels.managementtools.ReportingTabViewModel
import models.viewmodels.{ManagerToolsReportsTab, SubNavigationTab}
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{QueuesService, ReportingService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.managementtools._
import views.html.report_not_found
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding

import scala.concurrent.ExecutionContext

@Singleton
class ReportingController @Inject() (
  verify: RequestActions,
  reportingService: ReportingService,
  queuesService: QueuesService,
  usersService: UserService,
  mcc: MessagesControllerComponents,
  val manageReportsView: manage_reports_view,
  val summaryReportView: summaryReportView,
  val queueReportView: queueReportView,
  val caseReportView: caseReportView,
  val reportChooseDates: reportChooseDates,
  val reportChooseTeams: reportChooseTeams,
  val report_not_found: report_not_found,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with WithUnsafeDefaultFormBinding {

  lazy val chooseDatesForm = ReportDateForm.form
  lazy val chooseTeamsForm = ReportTeamForm.form

  val DownloadPageSize               = 1000
  val DownloadPagination: Pagination = SearchPagination(pageSize = DownloadPageSize)
  val LocalDateFormatter             = DateTimeFormatter.ISO_LOCAL_DATE

  def downloadCaseReport(report: CaseReport) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)).async { implicit request =>
      val dateTime   = appConfig.clock.instant().atZone(ZoneOffset.UTC)
      val dateString = LocalDateFormatter.format(dateTime)
      val getUsers   = usersService.getAllUsers(Seq.empty, "", NoPagination())
      val getTeams   = queuesService.getAllById

      (getUsers, getTeams).mapN {
        case (users, teamsById) =>
          val usersByPid = users.results.map(user => user.id -> user).toMap

          val fileData = Paged
            .stream(DownloadPagination)(reportingService.caseReport(report, _))
            .map(Reports.formatCaseReport(report, usersByPid, teamsById))
            .prepend(Source.single(Reports.formatHeaders(report)))
            .via(CsvFormatting.format[List[String]]())

          Ok.streamed(fileData, contentLength = None, contentType = Some("text/csv"))
            .withHeaders(
              "Content-Disposition" -> s"attachment; filename=${report.name.replaceAll("\\s+", "-")}-$dateString.csv"
            )
      }
    }

  def downloadSummaryReport(report: SummaryReport) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)).async { implicit request =>
      val dateTime   = appConfig.clock.instant().atZone(ZoneOffset.UTC)
      val dateString = LocalDateFormatter.format(dateTime)
      val getUsers   = usersService.getAllUsers(Seq.empty, "", NoPagination())
      val getTeams   = queuesService.getAllById

      (getUsers, getTeams).mapN {
        case (users, teamsById) =>
          val usersByPid = users.results.map(user => user.id -> user).toMap

          val fileData = Paged
            .stream(DownloadPagination)(reportingService.summaryReport(report, _))
            .map(Reports.formatSummaryReport(report, usersByPid, teamsById))
            .prepend(Source.single(Reports.formatHeaders(report)))
            .via(CsvFormatting.format[List[String]]())

          Ok.streamed(fileData, contentLength = None, contentType = Some("text/csv"))
            .withHeaders(
              "Content-Disposition" -> s"attachment; filename=${report.name.replaceAll("\\s+", "-")}-$dateString.csv"
            )
      }
    }

  def downloadQueueReport(report: QueueReport) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)).async { implicit request =>
      val dateTime   = appConfig.clock.instant().atZone(ZoneOffset.UTC)
      val dateString = LocalDateFormatter.format(dateTime)
      val getTeams   = queuesService.getAllById

      getTeams.map { teamsById =>
        val fileData = Paged
          .stream(DownloadPagination)(reportingService.queueReport(report, _))
          .map(Reports.formatQueueReport(teamsById))
          .prepend(Source.single(Reports.formatHeaders(report)))
          .via(CsvFormatting.format[List[String]]())

        Ok.streamed(fileData, contentLength = None, contentType = Some("text/csv"))
          .withHeaders(
            "Content-Disposition" -> s"attachment; filename=${report.name.replaceAll("\\s+", "-")}-$dateString.csv"
          )
      }
    }

  def showChangeDateFilter(report: Report, pagination: Pagination) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)) { implicit request =>
      val specificDates = report.dateRange != InstantRange.allTime
      val currentData   = ReportDateFormData(specificDates, report.dateRange)
      Ok(reportChooseDates(chooseDatesForm.fill(currentData), report, pagination))
    }

  def postChangeDateFilter(report: Report, pagination: Pagination) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)) { implicit request =>
      chooseDatesForm
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(reportChooseDates(formWithErrors, report, pagination)),
          form =>
            report match {
              case cses: CaseReport =>
                Redirect(
                  controllers.routes.ReportingController
                    .caseReport(cses.copy(dateRange = form.dateRange), pagination)
                )
              case summary: SummaryReport =>
                Redirect(
                  controllers.routes.ReportingController
                    .summaryReport(summary.copy(dateRange = form.dateRange), pagination)
                )
              case queue: QueueReport =>
                Redirect(
                  controllers.routes.ReportingController
                    .queueReport(queue.copy(dateRange = form.dateRange), pagination)
                )

            }
        )
    }

  def showChangeTeamsFilter(report: Report, pagination: Pagination) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)) { implicit request =>
      Ok(reportChooseTeams(chooseTeamsForm.fill(report.teams.isEmpty), report, pagination))
    }

  def postChangeTeamsFilter(report: Report, pagination: Pagination) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)) { implicit request =>
      chooseTeamsForm
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(reportChooseTeams(formWithErrors, report, pagination)),
          allTeams => {
            val teams = if (allTeams) Set.empty[String] else request.operator.memberOfTeams.toSet
            report match {
              case cses: CaseReport =>
                Redirect(
                  controllers.routes.ReportingController
                    .caseReport(cses.copy(teams = teams), pagination)
                )
              case summary: SummaryReport =>
                Redirect(
                  controllers.routes.ReportingController
                    .summaryReport(summary.copy(teams = teams), pagination)
                )
              case queue: QueueReport =>
                Redirect(
                  controllers.routes.ReportingController
                    .queueReport(queue.copy(teams = teams), pagination)
                )
            }
          }
        )
    }

  def caseReport(report: CaseReport, pagination: Pagination) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)).async { implicit request =>
      val getReport = reportingService.caseReport(report, pagination)
      val getUsers  = usersService.getAllUsers(Seq.empty, "", NoPagination())
      val getTeams  = queuesService.getAllById

      for {
        results <- getReport
        users   <- getUsers
        usersByPid = users.results.map(user => user.id -> user).toMap
        teamsById <- getTeams
      } yield Ok(caseReportView(report, pagination, results, usersByPid, teamsById))
    }

  def summaryReport(report: SummaryReport, pagination: Pagination) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)).async { implicit request =>
      val getReport = reportingService.summaryReport(report, pagination)
      val getUsers  = usersService.getAllUsers(Seq.empty, "", NoPagination())
      val getTeams  = queuesService.getAllById

      for {
        results <- getReport
        users   <- getUsers
        usersByPid = users.results.map(user => user.id -> user).toMap
        teamsById <- getTeams
      } yield Ok(summaryReportView(report, pagination, results, usersByPid, teamsById))
    }

  def queueReport(report: QueueReport, pagination: Pagination) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)).async { implicit request =>
      val getReport = reportingService.queueReport(report, pagination)
      val getTeams  = queuesService.getAllById

      for {
        results   <- getReport
        teamsById <- getTeams
      } yield Ok(queueReportView(report, pagination, results, teamsById))
    }

  def getReportByName(reportName: String) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)) { implicit request =>
      Report.byId
        .get(reportName)
        .map {
          case summary: SummaryReport =>
            Redirect(routes.ReportingController.summaryReport(summary, SearchPagination()))
          case cses: CaseReport =>
            Redirect(routes.ReportingController.caseReport(cses, SearchPagination()))
          case queue: QueueReport =>
            Redirect(routes.ReportingController.queueReport(queue, SearchPagination()))
        }
        .getOrElse {
          NotFound(report_not_found(reportName))
        }
    }

  def displayManageReporting(activeSubNav: SubNavigationTab = ManagerToolsReportsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS))(implicit request =>
      Ok(
        manageReportsView(
          activeSubNav,
          ReportingTabViewModel.reportingTabs()
        )
      )
    )
}
