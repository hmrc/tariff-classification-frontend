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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class ReferCaseController @Inject()(authenticatedAction: AuthenticatedAction,
                                    casesService: CasesService,
                                    val messagesApi: MessagesApi,
                                    implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {


  def referCase(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, c => successful(views.html.refer_case(c)))
  }

  def confirmReferCase(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, casesService.referCase(_, request.operator).map {
      views.html.confirm_refer_case(_)
    })
  }

  private def getCaseAndRenderView(reference: String, toHtml: Case => Future[HtmlFormat.Appendable])
                                  (implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).flatMap {
      case Some(c: Case) if c.status == CaseStatus.OPEN => toHtml(c).map(Ok(_))
      case Some(_) => successful(Redirect(routes.CaseController.applicationDetails(reference)))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }

}
