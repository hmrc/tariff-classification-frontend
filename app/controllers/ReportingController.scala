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

package controllers

import akka.stream.scaladsl.Source
import akka.stream.alpakka.csv.scaladsl.CsvFormatting
import config.AppConfig
import javax.inject.{Inject, Singleton}
import models._
import models.forms._
import models.reporting._
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, QueuesService, ReportingService, UserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.managementtools.{caseReportView, reportChooseDates, reportChooseTeams, summaryReportView}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ReportingController @Inject() (
  verify: RequestActions,
  reportingService: ReportingService,
  queuesService: QueuesService,
  usersService: UserService,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  lazy val chooseDatesForm = ReportDateForm.form
  lazy val chooseTeamsForm = ReportTeamForm.form

  def downloadCaseReport(report: CaseReport) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS))(implicit request => NotFound)

  def downloadSummaryReport(report: SummaryReport) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS))(implicit request => NotFound)

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

  def getReportByName(reportName: String) =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS)) { implicit request =>
      Report.byId
        .get(reportName)
        .map {
          case summary: SummaryReport =>
            Redirect(routes.ReportingController.summaryReport(summary, SearchPagination()))
          case cses: CaseReport =>
            Redirect(routes.ReportingController.caseReport(cses, SearchPagination()))
          case _: QueueReport =>
            NotFound(views.html.report_not_found(reportName))
        }
        .getOrElse {
          NotFound(views.html.report_not_found(reportName))
        }
    }
}
