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
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{DecisionForm, DecisionFormData, FormMapper}
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.Future


@Singleton
class RulingController @Inject()(casesService: CasesService,
                                 mapper: FormMapper,
                                 val messagesApi: MessagesApi,
                                 implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val decisionForm: Form[DecisionFormData] = DecisionForm.form

  val menuTitle = "ruling"

  def editRulingDetails(reference: String): Action[AnyContent] = AuthenticatedAction.async { implicit request =>
    casesService.getOne(reference).map {
      case Some(c: Case) if c.status == "OPEN" =>
        val formData = mapper.caseToDecisionFormData(c)
        val df = decisionForm.fill(formData)

        Ok(views.html.case_details(c, "ruling", views.html.partials.ruling_details_edit(c, df)))
      case Some(_) => Redirect(routes.CaseController.rulingDetails(reference))
      case _ => Ok(views.html.case_not_found(reference))
    }
  }

  def updateRulingDetails(reference: String): Action[AnyContent] = AuthenticatedAction.async { implicit request =>
    decisionForm.bindFromRequest.fold(
      errorForm =>
        getCaseAndRenderView(reference, "ruling", views.html.partials.ruling_details_edit(_, errorForm)),

      validForm => {
        casesService.getOne(reference).flatMap {
          case Some(c: Case) if c.status == "OPEN" =>
            casesService.updateCase(mapper.formToCase(c, validForm))
              .map(update => Ok(views.html.case_details(update, "ruling", views.html.partials.ruling_details(update))))
          case Some(_) => Future.successful(Redirect(routes.CaseController.rulingDetails(reference)))
          case _ => Future.successful(Ok(views.html.case_not_found(reference)))
        }
      }
    )
  }

  private def getCaseAndRenderView(reference: String, page: String, toHtml: Case => Html)(implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).map {
      case Some(c: Case) if c.status == "OPEN" => Ok(views.html.case_details(c, page, toHtml(c)))
      case Some(_) => Redirect(routes.CaseController.rulingDetails(reference))
      case _ => Ok(views.html.case_not_found(reference))
    }
  }
}
