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
import javax.inject.Inject
import models.{Case, MiscApplication, Permission}
import models.forms.v2.MiscellaneousForm
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future


class CreateMiscellaneousController @Inject()(
                                                 verify: RequestActions,
                                                 casesService: CasesService,
                                                 queueService: QueuesService,
                                                 mcc: MessagesControllerComponents,
                                                 implicit val appConfig: AppConfig
                                               ) extends FrontendController(mcc)  with I18nSupport {

  private val form: Form[MiscApplication] = MiscellaneousForm.newMiscForm

  def get(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
    implicit request: Request[AnyContent] =>
      Future.successful(Ok(views.html.v2.create_misc(form)))
  }

//  def post(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
//    implicit request =>
//      form.bindFromRequest.fold(
//        formWithErrors => Future.successful(Ok(views.html.v2.create_misc(formWithErrors))),
//        miscApp =>
//          casesService.createCase(miscApp, request.operator).map { caseCreated: Case =>
//            Redirect(routes.CreateMiscellaneousController.displayQuestion(caseCreated.reference))
//          }
//      )
//
//  }

}