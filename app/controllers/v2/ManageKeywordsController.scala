/*
 * Copyright 2021 HM Revenue & Customs
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
import models.forms.KeywordForm
import models.forms.v2.ChangeKeywordStatusForm
import models.viewmodels._
import models.viewmodels.managementtools.ManageKeywordsViewModel
import models.{Case, Permission}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future.successful

class ManageKeywordsController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  val manageKeywordsView: views.html.managementtools.manage_keywords_view,
  val changeKeywordStatusView: views.html.managementtools.change_keyword_status_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {
  val keywordForm: Form[String]             = KeywordForm.form
  val changeKeywordStatusForm: Form[String] = ChangeKeywordStatusForm.form

  def displayManageKeywords(activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS))(implicit request =>
      Ok(
        manageKeywordsView(
          activeSubNav,
          ManageKeywordsViewModel.forManagedTeams(),
          keywordForm
        )
      )
    )

  def changeKeywordStatus(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async(implicit request =>
      casesService.getOne(reference).flatMap {
        case Some(c: Case) =>
          successful(Ok(changeKeywordStatusView(c, changeKeywordStatusForm)))
        case _ => successful(Ok(views.html.case_not_found(reference)))
      }
    )

}
