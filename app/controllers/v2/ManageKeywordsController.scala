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
import models.forms.v2.EditApprovedKeywordForm
import models.viewmodels._
import models.viewmodels.managementtools.ManageKeywordsViewModel
import models.{Keyword, NoPagination, Permission}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.ManageKeywordsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ManageKeywordsController @Inject() (
  verify: RequestActions,
  mcc: MessagesControllerComponents,
  keywordService: ManageKeywordsService,
  val manageKeywordsView: views.html.managementtools.manage_keywords_view,
  val keywordCreatedConfirm: views.html.managementtools.confirm_keyword_created,
  val newKeywordView: views.html.managementtools.new_keyword_view,
  val editApprovedKeywordsView: views.html.managementtools.edit_approved_keywords,
  val confirmKeywordDeletedView: views.html.managementtools.confirmation_keyword_deleted,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {
  val keywordForm: Form[String] = KeywordForm.form
  val editKeyword: Form[String] = EditApprovedKeywordForm.form

  def displayManageKeywords(activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      for {
        caseKeywords <- keywordService.fetchCaseKeywords()
        allKeywords  <- keywordService.findAll(NoPagination())
        manageKeywordsViewModel = ManageKeywordsViewModel
          .forManagedTeams(caseKeywords.results, allKeywords.results.map(_.name))
      } yield Ok(
        manageKeywordsView(
          activeSubNav,
          manageKeywordsViewModel,
          keywordForm
        )
      )
    }

  def newKeyword(activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      for {
        keywords <- keywordService.findAll(NoPagination())
      } yield Ok(
        newKeywordView(
          activeSubNav,
          keywords.results,
          KeywordForm.formWithAuto(keywords.results.map(_.name))
        )
      )
    }

  def createKeyword(activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      keywordService.findAll(NoPagination()).flatMap { keywords =>
        val keywordNames = keywords.results.map(_.name)
        KeywordForm
          .formWithAuto(keywordNames)
          .bindFromRequest
          .fold(
            formWithErrors =>
              Future.successful(
                BadRequest(
                  newKeywordView(
                    activeSubNav,
                    keywords.results,
                    formWithErrors
                  )
                )
              ),
            keyword =>
              keywordService.createKeyword(Keyword(keyword, true)).map { saveKeyword: Keyword =>
                Redirect(controllers.v2.routes.ManageKeywordsController.displayConfirmKeyword(saveKeyword.name))
              }
          )
      }
    }

  def displayConfirmKeyword(
    saveKeyword: String,
    activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab
  ): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS))(implicit request =>
      Ok(
        keywordCreatedConfirm(activeSubNav, saveKeyword)
      )
    )

  def editApprovedKeywords(keywordName: String): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async(implicit request =>
      for {
        count       <- keywordService.fetchCaseKeywords().map(_.results.count(keyword => keyword.keyword.approved))
        allKeywords <- keywordService.findAll(NoPagination())
      } yield Ok(
        editApprovedKeywordsView(count, keywordName, allKeywords, editKeyword, keywordForm)
      )
    )

  def displayConfirmationKeywordDeleted(activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS))(implicit request =>
      Ok(
        confirmKeywordDeletedView(activeSubNav)
      )
    )
}
