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

import com.google.inject.Inject
import config.AppConfig
import controllers.{RenderCaseAction, RequestActions}
import models.{NoPagination, Permission}
import models.viewmodels.{ATaRTab, CasesTabViewModel, CorrespondenceTab, LiabilitiesTab, MiscellaneousTab, SubNavigationTab}
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global

class AllOpenCasesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  queueService: QueuesService,
  mcc: MessagesControllerComponents,
  val openCasesView: views.html.v2.open_cases_view,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  def displayAllOpenCases(activeSubNav: SubNavigationTab = ATaRTab): Action[AnyContent] = (verify.authenticated
    andThen verify.mustHave(Permission.VIEW_CASES)).async {
    implicit request =>

      for {
        nonGatewayQueues <- queueService.getNonGateway
        nonGatewayCases  <- casesService.getCasesByAllQueues(nonGatewayQueues, NoPagination())
        atarCases: CasesTabViewModel = activeSubNav match {
          case ATaRTab => CasesTabViewModel.atarCases(nonGatewayCases.results)
          case LiabilitiesTab => CasesTabViewModel.liability
          case CorrespondenceTab  => CasesTabViewModel.correspondence
          case MiscellaneousTab => CasesTabViewModel.miscellaneous
        }
      } yield Ok(openCasesView(atarCases))
  }
}
