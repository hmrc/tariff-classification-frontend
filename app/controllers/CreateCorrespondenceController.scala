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
import models.forms.CorrespondenceForm
import javax.inject.{Inject, Singleton}
import models.{CorrespondenceApplication, Permission, Case}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class CreateCorrespondenceController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  val releaseCaseView: views.html.release_case,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  private val form: Form[CorrespondenceApplication] = CorrespondenceForm.newCorrespondenceForm

  def get(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
    implicit request: Request[AnyContent] => Future.successful(Ok(views.html.v2.create_correspondence(form)))
  }

  def post(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
    implicit request =>
      form.bindFromRequest.fold(
        formWithErrors => Future.successful(Ok(views.html.v2.create_correspondence(formWithErrors))),
        correspondenceApp =>
          casesService.createCase(correspondenceApp, request.operator).map { caseCreated:Case =>
            Redirect(routes.ReleaseCaseController.releaseCase(caseCreated.reference, None))
          }
      )

  }

  def displayQuestion():Unit = ???

  def chooseQueuePost():Unit = ???

  def displayConfirmation() = (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
    implicit request: Request[AnyContent] => Future.successful(Ok(views.html.v2.confirmation_case_creation()))
  }

}
