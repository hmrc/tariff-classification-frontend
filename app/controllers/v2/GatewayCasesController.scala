/*
 * Copyright 2025 HM Revenue & Customs
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

import com.google.inject.Inject
import config.AppConfig
import controllers.RequestActions
import models.request.AuthenticatedRequest
import models.viewmodels._
import models.{ApplicationType, NoPagination, Permission, Queues}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CasesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.v2.gateway_cases_view

import scala.concurrent.ExecutionContext

class GatewayCasesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  val gatewayCasesView: gateway_cases_view,
  implicit val appConfig: AppConfig
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def displayGatewayCases: Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_QUEUE_CASES)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        val types: Set[ApplicationType] = ApplicationType.values

        for {
          cases <- casesService.getCasesByQueue(Queues.gateway, NoPagination(), types)
          gatewayCases = ApplicationsTab.gateway(cases.results)
        } yield Ok(gatewayCasesView(gatewayCases))
    }

}
