/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.InstantRangeForm
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.{QueuesService, ReportingService}
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.Report.Report
import uk.gov.hmrc.tariffclassificationfrontend.views.{Report, SelectedReport}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReportingController @Inject()(authenticated: AuthenticatedAction,
                                    verifiedManager: AuthenticatedManagerAction,
                                    reportingService: ReportingService,
                                    queuesService: QueuesService,
                                    val messagesApi: MessagesApi,
                                    implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def getReports: Action[AnyContent] = (authenticated andThen verifiedManager).async { implicit request =>
    for {
      queues <- queuesService.getAll
    } yield Ok(views.html.reports(queues, None))
  }

  def getReportCriteria(name: String): Action[AnyContent] = (authenticated andThen verifiedManager).async { implicit request =>
    handleNotFound(name) {
      case Report.SLA => getSLAReportCriteria
    }
  }

  def getReport(name: String): Action[AnyContent] = (authenticated andThen verifiedManager).async { implicit request =>
    handleNotFound(name) {
      case Report.SLA => getSLAReport
    }
  }

  private def getSLAReportCriteria(implicit request: AuthenticatedRequest[_]): Future[Result] = {
    for {
      queues <- queuesService.getAll
    } yield Ok(views.html.reports(
      queues,
      Some(SelectedReport(
        Report.SLA,
        views.html.partials.reports.sla_criteria(InstantRangeForm.form))
      ))
    )
  }

  private def getSLAReport(implicit request: AuthenticatedRequest[_]): Future[Result] = {
    InstantRangeForm.form.bindFromRequest.fold(
      formWithErrors =>
        for {
          queues <- queuesService.getAll
        } yield Ok(views.html.reports(queues, Some(SelectedReport(Report.SLA, views.html.partials.reports.sla_criteria(formWithErrors))))),
      filter =>
        for {
          queues <- queuesService.getNonGateway
          results <- reportingService.getSLAReport(filter)
        } yield Ok(views.html.report_sla(filter, results, queues))
    )
  }

  private def handleNotFound(name: String)(onFound: Report => Future[Result]): Future[Result] = Report.values.find(_.toString == name) match {
    case Some(report) => onFound(report)
    case None => Future.successful(Redirect(routes.ReportingController.getReports()))
  }


}
