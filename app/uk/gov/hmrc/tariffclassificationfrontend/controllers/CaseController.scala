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


import cats.data.OptionT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{DecisionForm, FormMapper}
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CaseController @Inject()(casesService: CasesService,
                               mapper: FormMapper,
                               val messagesApi: MessagesApi,
                               implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val decisionForm: Form[DecisionForm] = DecisionForm.form

  def summary(reference: String): Action[AnyContent] = AuthenticatedAction.async { implicit request =>
    getCaseAndRender(reference, views.html.case_summary(_))
  }

  def applicationDetails(reference: String): Action[AnyContent] = AuthenticatedAction.async { implicit request =>
    getCaseAndRender(reference, c => views.html.application_details(c))
  }

  def rulingDetails(reference: String): Action[AnyContent] = AuthenticatedAction.async { implicit request =>
    getCaseAndRender(reference, views.html.ruling_details(_))
  }

  def editRulingDetails(reference: String): Action[AnyContent] = AuthenticatedAction.async { implicit request =>
    getCaseAndRender(reference, views.html.ruling_details_edit(_))
  }

  def updateRulingDetails(reference: String): Action[AnyContent] = AuthenticatedAction.async { implicit request =>

    // TODO: optional subfields of the decision should be promoted to Some(_) only if non-empty

    decisionForm.bindFromRequest.fold(
      formWithErrors =>
        // TODO: Handle errors on form
        getCaseAndRender(reference, views.html.ruling_details_edit(_))
      ,
      formData => {
        val ot: OptionT[Future, Case] = for {
          selectCase <- OptionT(casesService.getOne(reference))
          updatedCase <- OptionT.liftF(casesService.updateCase(mapper.mergeForm(selectCase, formData)))
        } yield updatedCase

        ot.value.flatMap {
          case Some(c: Case) => Future.successful(Ok(views.html.ruling_details(c)))
          case _ => Future.successful(Ok(views.html.case_not_found(reference)))
        }
      }
    )
  }


  private def getCaseAndRender(reference: String, toHtml: Case => Html)(implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).map {
      case Some(c: Case) => Ok(toHtml(c))
      case _ => Ok(views.html.case_not_found(reference))
    }
  }

}
