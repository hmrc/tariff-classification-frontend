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
import models.viewmodels._
import models.{ApplicationType, NoPagination, Permission}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.v2.open_cases_view

import scala.concurrent.ExecutionContext

class AllOpenCasesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  queueService: QueuesService,
  mcc: MessagesControllerComponents,
  val openCasesView: open_cases_view,
  implicit val appConfig: AppConfig
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def displayAllOpenCases(activeSubNav: SubNavigationTab = ATaRTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_QUEUE_CASES)).async { implicit request =>
      val applicationType = activeSubNav match {
        case ATaRTab           => ApplicationType.ATAR
        case LiabilitiesTab    => ApplicationType.LIABILITY
        case CorrespondenceTab => ApplicationType.CORRESPONDENCE
        case MiscellaneousTab  => ApplicationType.MISCELLANEOUS
      }

      for {
        queuesForType <- queueService.getAllForCaseType(applicationType)
        casesForQueues <- casesService.getCasesByAllQueues(
                            queue = queuesForType,
                            pagination = NoPagination(),
                            forTypes = Set(applicationType),
                            assignee = "none"
                          )
        openCases = CasesTabViewModel
                      .forApplicationType(
                        applicationType,
                        queuesForType,
                        casesForQueues.results
                      )

      } yield Ok(openCasesView(openCases, activeSubNav))
    }
}
