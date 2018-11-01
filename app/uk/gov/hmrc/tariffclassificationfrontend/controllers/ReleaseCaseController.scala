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
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.ReleaseCaseForm
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReleaseCaseController @Inject()(casesService: CasesService,
                                      queueService: QueuesService,
                                      val messagesApi: MessagesApi,
                                      implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val releaseCaseForm: Form[ReleaseCaseForm] = ReleaseCaseForm.form

  def releaseCase(reference: String): Action[AnyContent] = AuthenticatedAction.async { implicit request =>
    getCaseAndRender(reference, views.html.release_case(_, releaseCaseForm, queueService.getNonGateway))
  }

  def releaseCaseToQueue(reference: String): Action[AnyContent] = AuthenticatedAction.async { implicit request =>
    def onInvalidForm(formWithErrors: Form[ReleaseCaseForm]): Future[Result] = {
      getCaseAndRender(reference, views.html.release_case(_, formWithErrors, queueService.getNonGateway))
    }

    def onValidForm(validForm: ReleaseCaseForm): Future[Result] = {
      queueService.getOneBySlug(validForm.queue)
        .map(queue => {
          casesService.getOne(reference).flatMap {
            case Some(c: Case) => casesService.releaseCase(c, queue).map(updatedCase => Ok(views.html.confirm_release_case(updatedCase, queue)))
            case _ => Future.successful(Ok(views.html.case_not_found(reference)))
          }
        })
        .getOrElse(Future.successful(Ok(views.html.resource_not_found(Some("Queue")))))
    }

    releaseCaseForm.bindFromRequest.fold(onInvalidForm, onValidForm)
  }

  private def getCaseAndRender(reference: String, toHtml: Case => Html)(implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).map {
      case Some(c: Case) => Ok(toHtml(c))
      case _ => Ok(views.html.case_not_found(reference))
    }
  }

}
