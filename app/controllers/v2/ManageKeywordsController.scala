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
import models.viewmodels._
import models.viewmodels.managementtools.ManageKeywordsViewModel
import models.{Keyword, NoPagination, Operator, Permission}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, ManageKeywordsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ManageKeywordsController @Inject()(
  verify: RequestActions,
  mcc: MessagesControllerComponents,
  keywordService: ManageKeywordsService,
  casesService: CasesService,
  val manageKeywordsView: views.html.managementtools.manage_keywords_view,
  val keywordCreatedConfirm: views.html.managementtools.confirm_keyword_created,
  val keywordUpdatedConfirmation: views.html.managementtools.confirm_keyword_status,
  val newKeywordView: views.html.managementtools.new_keyword_view,
  val changeKeywordStatusView: views.html.managementtools.change_keyword_status_view,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {
  val keywordForm: Form[String] = KeywordForm.form
  val ChangeKeywordStatusForm: Form[String] = ChangeKeywordStatusForm.form

  def displayManageKeywords(activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>

      for {
        caseKeywords           <- keywordService.fetchCaseKeywords()
        allKeywords            <- keywordService.findAll(NoPagination())
        manageKeywordsViewModel = ManageKeywordsViewModel
          .forManagedTeams(caseKeywords.results, allKeywords.results.map(_.name))
      } yield
        Ok(
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
      } yield
        Ok(
          newKeywordView(
            activeSubNav,
            keywords.results,
            KeywordForm.formWithAuto(keywords.results.map(_.name))
          )
        )
    }

  def createKeyword(activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>

      keywordService.findAll(NoPagination()).flatMap {
        keywords =>
          val keywordNames = keywords.results.map(_.name)
          KeywordForm.formWithAuto(keywordNames).bindFromRequest.fold(
            formWithErrors =>
              Future.successful(BadRequest(
                newKeywordView(
                  activeSubNav,
                  keywords.results,
                  formWithErrors
                )
              )),
            keyword =>
              keywordService.createKeyword(Keyword(keyword, true)).map { saveKeyword: Keyword =>
                Redirect(controllers.v2.routes.ManageKeywordsController.displayConfirmKeyword(saveKeyword.name))
              }
          )
      }
    }

  def changeKeywordStatus(originalKeywordName: String, caseReference: String, newKeywordName: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>

    val operator: Future[Option[Operator]] = casesService.getOne(caseReference).map(c => c.get.assignee)

      keywordService.findAll(NoPagination()).flatMap {
        keywords =>
          val originalKeywordOpt: Option[Keyword] = keywords.results.find(keyword => keyword.name == originalKeywordName)
          if(newKeywordName.isDefined) {
            val keywordExists: Boolean = keywords.results.exists(keyword => keyword.name == newKeywordName.get)
          }

          ChangeKeywordStatusForm.bindFromRequest.fold(
            formWithErrors =>
              Future.successful(BadRequest(
                changeKeywordStatusView(
                  originalKeywordName,
                  caseReference,
                  formWithErrors
                )
              )),
            keywordChangeStatusForm =>
              keywordService.updateKeywordStatus(originalKeywordOpt.get, keywordChangeStatusForm).map { savedKeyword: Keyword =>
                Redirect(
                  controllers.v2.routes.ManageKeywordsController.displayConfirmKeywordChange(
                    savedKeyword, keywordChangeStatusForm.action, originalKeywordOpt.get.name, operator.map(f => f.get))
                )
              }
          )
      }
    }

  def displayConfirmKeyword(
    saveKeyword: String,
    activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS))(
      implicit request =>
        Ok(
          keywordCreatedConfirm(activeSubNav, saveKeyword)
      ))

  def displayConfirmKeywordChange(
    savedKeyword: Keyword,
    status: String,
    oldKeywordName: Option[String],
    operator: Operator,
    activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS))(
      implicit request =>
        Ok(
          keywordUpdatedConfirmation(activeSubNav, savedKeyword, status, oldKeywordName, operator)
        ))
}
