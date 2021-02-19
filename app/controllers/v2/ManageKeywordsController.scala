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
import models.Permission
import models.forms.KeywordForm
import models.viewmodels._
import models.viewmodels.managementtools.ManageKeywordsViewModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.managementtools.new_keyword_view

class ManageKeywordsController @Inject()(
  verify: RequestActions,
  mcc: MessagesControllerComponents,
  val manageKeywordsView: views.html.managementtools.manage_keywords_view,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {
  val keywordForm: Form[String] = KeywordForm.form

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

  def newKeyword(activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS))(implicit request =>
      Ok(
        new_keyword_view(
          ManageKeywordsViewModel.forManagedTeams().allKeywordsTab,
          keywordForm
        )
      )
    )
}
