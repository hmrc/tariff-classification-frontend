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

package controllers.v2


import config.AppConfig
import controllers.RequestActions
import javax.inject.{Inject, Singleton}
import models.Permission
import models.viewmodels.LiabilityViewModel
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

@Singleton
class LiabilityController @Inject()(
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  val liability_view: views.html.v2.liability_view,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc) with I18nSupport {

  def displayLiability(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async {
    implicit request =>
      Future.successful(Ok(liability_view(LiabilityViewModel.fromCase(request.`case`))))
  }
}
