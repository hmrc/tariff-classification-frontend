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
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.DecisionFormMapper
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, EventsService, FileStoreService}
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class CaseController @Inject()(authenticatedAction: AuthenticatedAction,
                               casesService: CasesService,
                               fileStoreService: FileStoreService,
                               eventsService: EventsService,
                               mapper: DecisionFormMapper,
                               val messagesApi: MessagesApi,
                               implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def summary(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, CaseDetailPage.SUMMARY, c => successful(views.html.partials.case_summary(c)))
  }

  def applicationDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      reference,
      CaseDetailPage.APPLICATION_DETAILS,
      c => {
        for {
          attachments <- fileStoreService.getAttachments(c)
          letter <- fileStoreService.getLetterOfAuthority(c)
          response = views.html.partials.application_details(c, attachments, letter)
        } yield response
      }
    )
  }

  def rulingDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, CaseDetailPage.RULING, c => {
      fileStoreService.getAttachments(c).map(views.html.partials.ruling_details(c, _))
    })
  }

  def activityDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, CaseDetailPage.ACTIVITY, c => {
      eventsService.getEvents(c.reference).map(views.html.partials.activity_details(c, _))
    })
  }

  private def getCaseAndRenderView(reference: String, page: CaseDetailPage, toHtml: Case => Future[Html])
                                  (implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).flatMap {
      case Some(c: Case) => toHtml(c).map(html => Ok(views.html.case_details(c, page, html)))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }

}
