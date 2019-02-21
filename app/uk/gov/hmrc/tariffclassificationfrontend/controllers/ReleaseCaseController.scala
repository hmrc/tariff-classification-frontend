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
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.ReleaseCaseForm
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus, Queue}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class ReleaseCaseController @Inject()(authenticatedAction: AuthenticatedAction,
                                      casesService: CasesService,
                                      queueService: QueuesService,
                                      val messagesApi: MessagesApi,
                                      implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private lazy val releaseCaseForm: Form[String] = ReleaseCaseForm.form

  def releaseCase(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, c => successful(views.html.release_case(c, releaseCaseForm, queueService.getNonGateway)))
  }

  def releaseCaseToQueue(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>

    def onInvalidForm(formWithErrors: Form[String]): Future[Result] = {
      getCaseAndRenderView(reference, c => successful(views.html.release_case(c, formWithErrors, queueService.getNonGateway)))
    }

    def onValidForm(queueSlug: String): Future[Result] = {
      queueService.getOneBySlug(queueSlug) match {
        case None => successful(Ok(views.html.resource_not_found(s"Queue $queueSlug")))
        case Some(q: Queue) =>
          getCaseAndRenderView(reference, casesService.releaseCase(_, q, request.operator).map { c: Case =>
            views.html.confirm_release_case(c, q)
          })
      }
    }

    releaseCaseForm.bindFromRequest.fold(onInvalidForm, onValidForm)
  }

  private def getCaseAndRenderView(reference: String, toHtml: Case => Future[HtmlFormat.Appendable])
                                  (implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).flatMap {
      case Some(c: Case) if c.status == CaseStatus.NEW => toHtml(c).map(Ok(_))
      case Some(_) => successful(Redirect(routes.CaseController.applicationDetails(reference)))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }

}
