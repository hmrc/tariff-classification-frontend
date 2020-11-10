/*
 * Copyright 2020 HM Revenue & Customs
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

import config.AppConfig
import models.forms.InstantRangeForm
import javax.inject.{Inject, Singleton}
import models.Permission
import models.request.AuthenticatedRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, QueuesService, ReportingService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.Report.Report
import views.{Report, SelectedReport}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReportingController @Inject() (
  verify: RequestActions,
  reportingService: ReportingService,
  queuesService: QueuesService,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  def getReports: Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS))
      .async { implicit request =>
        for {
          queues           <- queuesService.getAll
          caseCountByQueue <- casesService.countCasesByQueue(request.operator)
        } yield Ok(views.html.reports(queues, None, caseCountByQueue))
      }

  def getReportCriteria(name: String, startAtTabIndex: Int): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS))
      .async { implicit request =>
        handleNotFound(name) {
          case Report.SLA      => getSLAReportCriteria(startAtTabIndex)
          case Report.REFERRAL => getReferralReportCriteria(startAtTabIndex)
        }
      }

  private def getSLAReportCriteria(startAtTabIndex: Int)(implicit request: AuthenticatedRequest[_]): Future[Result] =
    for {
      queues           <- queuesService.getAll
      caseCountByQueue <- casesService.countCasesByQueue(request.operator)
    } yield Ok(
      views.html.reports(
        queues,
        Some(
          SelectedReport(
            Report.SLA,
            views.html.partials.reports.sla_report_criteria(InstantRangeForm.form, startAtTabIndex)
          )
        ),
        caseCountByQueue
      )
    )

  private def getReferralReportCriteria(
    startAtTabIndex: Int
  )(implicit request: AuthenticatedRequest[_]): Future[Result] =
    for {
      queues           <- queuesService.getAll
      caseCountByQueue <- casesService.countCasesByQueue(request.operator)
    } yield Ok(
      views.html.reports(
        queues,
        Some(
          SelectedReport(
            Report.REFERRAL,
            views.html.partials.reports.referral_report_criteria(InstantRangeForm.form, startAtTabIndex)
          )
        ),
        caseCountByQueue
      )
    )

  private def handleNotFound(name: String)(onFound: Report => Future[Result]): Future[Result] =
    Report.values.find(_.toString == name) match {
      case Some(report) => onFound(report)
      case None         => Future.successful(Redirect(routes.ReportingController.getReports()))
    }

  def getReport(name: String): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_REPORTS))
      .async { implicit request =>
        handleNotFound(name) {
          case Report.SLA      => getSLAReport
          case Report.REFERRAL => getReferralReport
        }
      }

  private def getSLAReport(implicit request: AuthenticatedRequest[_]): Future[Result] =
    InstantRangeForm.form.bindFromRequest.fold(
      formWithErrors =>
        for {
          queues           <- queuesService.getAll
          caseCountByQueue <- casesService.countCasesByQueue(request.operator)
        } yield Ok(
          views.html.reports(
            queues,
            Some(SelectedReport(Report.SLA, views.html.partials.reports.sla_report_criteria(formWithErrors))),
            caseCountByQueue
          )
        ),
      filter =>
        for {
          queues  <- queuesService.getNonGateway
          results <- reportingService.getSLAReport(filter)
        } yield Ok(views.html.report_sla(filter, results, queues))
    )

  private def getReferralReport(implicit request: AuthenticatedRequest[_]): Future[Result] =
    InstantRangeForm.form.bindFromRequest.fold(
      formWithErrors =>
        for {
          queues           <- queuesService.getAll
          caseCountByQueue <- casesService.countCasesByQueue(request.operator)
        } yield Ok(
          views.html.reports(
            queues,
            Some(SelectedReport(Report.REFERRAL, views.html.partials.reports.referral_report_criteria(formWithErrors))),
            caseCountByQueue
          )
        ),
      filter =>
        for {
          queues  <- queuesService.getNonGateway
          results <- reportingService.getReferralReport(filter)
        } yield Ok(views.html.report_referral(filter, results, queues))
    )

}
