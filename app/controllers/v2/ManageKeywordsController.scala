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
import models._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, ManageKeywordsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class ManageKeywordsController @Inject()(
  verify: RequestActions,
  casesService: CasesService,
  keywordService: ManageKeywordsService,
  mcc: MessagesControllerComponents,
  val manageKeywordsView: views.html.managementtools.manage_keywords_view,
  val keywordCreatedConfirm: views.html.managementtools.confirm_keyword_created,
  val newKeywordView: views.html.managementtools.new_keyword_view,
  val keywordChangeConfirm: views.html.managementtools.confirm_keyword_status,
  val changeKeywordStatusView: views.html.managementtools.change_keyword_status_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {
  val keywordForm: Form[String]             = KeywordForm.form
  val changeKeywordStatusForm: Form[String] = ChangeKeywordStatusForm.form

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

  def approveOrRejectKeyword(keywordName: String, `case`: Case): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async { implicit request =>
      changeKeywordStatusForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(
            changeKeywordStatusView(
              keywordName,
              `case`,
              formWithErrors
            )
          )),
        action =>
          action.toUpperCase match {
            case "APPROVED" => keywordService.createKeyword(Keyword(keywordName, approved = true)).map { savedKeyword: Keyword =>
              Redirect(
                controllers.v2.routes.ManageKeywordsController.displayKeywordChangeConfirmation(savedKeyword, `case`)
              )
            }
            case "REJECTED" => keywordService.createKeyword(Keyword(keywordName)).map { savedKeyword: Keyword =>
              Redirect(
                controllers.v2.routes.ManageKeywordsController.displayKeywordChangeConfirmation(savedKeyword, `case`)
              )
            }
          }
        )
    }

  def displayConfirmKeyword(
    saveKeyword: String,
    activeSubNav: SubNavigationTab = ManagerToolsKeywordsTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS))(
      implicit request =>
        Ok(
          keywordCreatedConfirm(activeSubNav, saveKeyword)
      )
    )

  def changeKeywordStatus(keywordName: String, reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS)).async(implicit request =>
      casesService.getOne(reference).flatMap {
        case Some(c: Case) => Future.successful(Ok(changeKeywordStatusView(keywordName, c, changeKeywordStatusForm)))
        case _ => Future.successful(Ok(views.html.case_not_found(reference)))
      }
    )

  def displayKeywordChangeConfirmation(keyword: Keyword, `case`: Case): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.MANAGE_USERS))(
      implicit request =>
        Ok(
          keywordChangeConfirm(keyword, `case`)
        ))
}
