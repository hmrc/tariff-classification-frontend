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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CaseController @Inject()(casesService: CasesService,
                               val messagesApi: MessagesApi,
                               implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def summary(reference: String): Action[AnyContent] = Action.async { implicit request =>
    casesService.getOne(reference)
      .map(response => {
        if (response.isEmpty) Ok(views.html.case_not_found(reference))
        else Ok(views.html.case_summary(response.get))
      })
  }

  def applicationDetails(reference: String): Action[AnyContent] = Action.async { implicit request =>
    casesService.getOne(reference)
      .map(response => {
        if (response.isEmpty) {
          Ok(views.html.case_not_found(reference))
        } else {
          Ok(views.html.application_details(response.get))
        }
      })
  }

  def summary(reference: String): Action[AnyContent] = Action.async { implicit request =>
    casesService.getOne(reference)
      .map(response => {
        if (response.isEmpty) {
          Ok(views.html.case_not_found(reference))
        } else {
          Ok(views.html.case_summary(response.get))
        }
      })
  }

  def getCaseAndRender(reference: String, html: Case => Html)(implicit request: Request[_]) = {
    casesService.getOne(reference)
      .map(response => {
        if (response.isEmpty) {
          Ok(views.html.case_not_found(reference))
        } else {
          Ok(views.html.case_summary(response.get))
        }
      })
  }

}
