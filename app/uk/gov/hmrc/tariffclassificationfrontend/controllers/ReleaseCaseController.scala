/*
 * Copyright 2018 HM Revenue & Customs
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
import play.mvc.Security.AuthenticatedAction
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditService
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.ReleaseCaseForm
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus, Queue}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReleaseCaseController @Inject()(authenticatedAction: AuthenticatedAction,
                                      casesService: CasesService,
                                      auditService: AuditService,
                                      queueService: QueuesService,
                                      val messagesApi: MessagesApi,
                                      implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private val releaseCaseForm: Form[ReleaseCaseForm] = ReleaseCaseForm.form

  def releaseCase(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, c => Future.successful(views.html.release_case(c, releaseCaseForm, queueService.getNonGateway)))
  }

  def releaseCaseToQueue(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    def onInvalidForm(formWithErrors: Form[ReleaseCaseForm]): Future[Result] = {
      getCaseAndRenderView(reference, c => Future.successful(views.html.release_case(c, formWithErrors, queueService.getNonGateway)))
    }

    def onValidForm(validForm: ReleaseCaseForm): Future[Result] = {
      queueService.getOneBySlug(validForm.queue) match {
        case None => Future.successful(Ok(views.html.resource_not_found(s"Queue ${validForm.queue}")))
        case Some(q: Queue) =>
          getCaseAndRenderView(reference, casesService.releaseCase(_, q).map { c: Case =>
            auditService.auditCaseReleased(c)
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
      case Some(_) => Future.successful(Redirect(routes.CaseController.applicationDetails(reference)))
      case _ => Future.successful(Ok(views.html.case_not_found(reference)))
    }
  }

}
