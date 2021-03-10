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

import controllers.{ControllerBaseSpec, RequestActionsWithPermissions}
import models._
import models.forms.KeywordForm
import models.forms.v2.EditApprovedKeywordForm
import models.viewmodels.ManagerToolsKeywordsTab
import models.forms.v2.ChangeKeywordStatusForm
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, ManageKeywordsService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.managementtools._
import utils.Cases
import views.html.managementtools.{change_keyword_status_view, confirm_keyword_created, manage_keywords_view, new_keyword_view}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class ManageKeywordsControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  val keywords = Seq(Keyword("SHOES", true), Keyword("HATS", true), Keyword("SHIRTS", true))
  val keywordForm = KeywordForm.formWithAuto(keywords.map(_.name))
  val editKeywordForm = EditApprovedKeywordForm
  val caseKeyword = CaseKeyword(
    Keyword("BOOK", false),
    List(CaseHeader("ref", None, None, Some("NOTEBOOK"), ApplicationType.ATAR, CaseStatus.REFERRED, 0, None))
  )

  private lazy val manage_keywords_view = injector.instanceOf[manage_keywords_view]
  private lazy val confirm_keyword_view = injector.instanceOf[confirm_keyword_created]
  private lazy val new_keyword_view = injector.instanceOf[new_keyword_view]
  private lazy val edit_approved_keyword_view = injector.instanceOf[edit_approved_keywords]
  private lazy val confirmation_keyword_deleted_view = injector.instanceOf[confirmation_keyword_deleted]
  private lazy val confirmation_keyword_renamed_view = injector.instanceOf[confirmation_keyword_renamed]
  private lazy val keywordService = mock[ManageKeywordsService]
  private lazy val changeKeywordStatusView = injector.instanceOf[change_keyword_status_view]
  private val casesService = mock[CasesService]
  val form: Form[String] = ChangeKeywordStatusForm.form

  override protected def beforeEach(): Unit =
    reset(
      casesService
    )

  private def controller(permission: Set[Permission]) = new ManageKeywordsController(
    new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
    casesService,
    keywordService,
    mcc,
    manage_keywords_view,
    confirm_keyword_view,
    new_keyword_view,
    changeKeywordStatusView,
    edit_approved_keyword_view,
    confirmation_keyword_deleted_view,
    confirmation_keyword_renamed_view,
    realAppConfig
  )

  "displayManageKeywords" should {

    "return 200 OK and HTML content type" in {
      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))
      given(keywordService.fetchCaseKeywords()(any[HeaderCarrier]))
        .willReturn(Future(Paged(Seq(caseKeyword))))

      val result = await(controller(Set(Permission.MANAGE_USERS)).displayManageKeywords()(newFakeGETRequestWithCSRF))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).displayManageKeywords()(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

  "newKeyword" should {

    "return 200 OK and HTML content type" in {

      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      val result = await(controller(Set(Permission.MANAGE_USERS)).newKeyword()(newFakeGETRequestWithCSRF))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).newKeyword()(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)
    }
  }

  "createKeyword" should {

    "return 303 SEE_OTHER when new keyword successfully added" in {

      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      given(keywordService.createKeyword(any[Keyword])(any[HeaderCarrier]))
        .willReturn(Future(Keyword("newkeyword", true)))

      val result = await(
        controller(Set(Permission.MANAGE_USERS))
          .createKeyword()(newFakePOSTRequestWithCSRF(Map("keyword" -> "newkeyword")))
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.v2.routes.ManageKeywordsController.displayConfirmKeyword("newkeyword").path()
      )

    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).createKeyword()(newFakePOSTRequestWithCSRF(Map("keyword" -> "newkeyword"))))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)
    }

    "render error if keyword empty" in {

      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      val result = await(
        controller(Set(Permission.MANAGE_USERS)).createKeyword()(newFakePOSTRequestWithCSRF(Map("keyword" -> "")))
      )

      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("error-summary")
      contentAsString(result) should include(messages("management.create-keyword.error.empty.keyword"))

    }

    "render error if duplicate keyword entered" in {
      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      val result = await(
        controller(Set(Permission.MANAGE_USERS))
          .createKeyword()(newFakePOSTRequestWithCSRF(Map("keyword" -> keywords.head.name)))
      )

      status(result) shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("error-summary")
      contentAsString(result) should include(messages("management.create-keyword.error.duplicate.keyword"))
    }

  }

  "displayConfirmKeyword" should {

    "return 200 OK and HTML content type" in {

      val result = await(controller(Set(Permission.MANAGE_USERS)).displayConfirmKeyword("KEYWORD")(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).displayConfirmKeyword("KEYWORD")(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }
  }


  "Change keyword status" should {
    val aCase = Cases.btiCaseExample.copy(reference = "reference")

    "return 200 OK and load the changeKeywordStatus form" in {
      when(casesService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(aCase)))

      val result =
        await(
          controller(Set(Permission.MANAGE_USERS))
            .changeKeywordStatus("keywordName", "reference")(newFakeGETRequestWithCSRF(app))
        )

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return case not found when case is not found" in {
      when(casesService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(None))

      val result =
        await(
          controller(Set(Permission.MANAGE_USERS))
            .changeKeywordStatus("keywordName", "reference")(newFakeGETRequestWithCSRF(app))
        )

      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")

    }

    "return unauthorised with no permissions" in {

      when(casesService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(aCase)))

      val result = await(controller(Set()).changeKeywordStatus("keywordName", "reference")(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }
  }

  "editApprovedKeywords" should {

    "return 200 OK and HTML content type" in {

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).editApprovedKeywords("KEYWORD")(newFakeGETRequestWithCSRF))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).displayConfirmKeyword("KEYWORD")(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }
  }

  "displayConfirmationKeywordDeleted" should {

    "return 200 OK and HTML content type" in {

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).displayConfirmationKeywordDeleted()(newFakeGETRequestWithCSRF))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }
  }

  "displayConfirmationKeywordRenamed" should {

    "return 200 OK and HTML content type" in {

      val result =
        await(
          controller(Set(Permission.MANAGE_USERS))
            .displayConfirmationKeywordRenamed("oldKeyword", "newKeyword")(newFakeGETRequestWithCSRF)
        )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }
  }

  "postEditApprovedKeywords" should {
    "delete a keyword when keyword has been selected and found" in {
      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      given(keywordService.deleteKeyword(any[Keyword])(any[HeaderCarrier])).willReturn(Future.successful((): Unit))

      val result = await(
        controller(Set(Permission.MANAGE_USERS))
          .postEditApprovedKeywords("keywordName")(
            newFakePOSTRequestWithCSRF(Map("action" -> "DELETE", "keywordName" -> "keywordName"))
          )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe
        Some(controllers.v2.routes.ManageKeywordsController.displayConfirmationKeywordDeleted().url)

    }

    "rename a keyword when keyword has been selected and found" in {
      val oldKeyword = Keyword("oldKeyword", true)
      val newKeyword = Keyword("updatedKeyword", true)

      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      given(keywordService.renameKeyword(any[Keyword], any[Keyword])(any[HeaderCarrier])).willReturn(Future(newKeyword))

      val result = await(
        controller(Set(Permission.MANAGE_USERS))
          .postEditApprovedKeywords("oldKeyword")(
            newFakePOSTRequestWithCSRF(Map("action" -> "RENAME", "keywordName" -> "updatedKeyword"))
          )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe
        Some(
          controllers.v2.routes.ManageKeywordsController
            .displayConfirmationKeywordRenamed("oldKeyword", "updatedKeyword")
            .path()
        )

    }

    "return Bad request when form has errors" in {

      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      val result = await(
        controller(Set(Permission.MANAGE_USERS))
          .postEditApprovedKeywords("oldKeyword")(newFakePOSTRequestWithCSRF(Map("action" -> "NON-Existing", "" -> "")))
      )

      status(result) shouldBe Status.BAD_REQUEST

    }
  }

  "postDisplayManageKeywords" should {
    "redirect to editApprovedKeywords if the keyword has been selected" in {

      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      given(keywordService.fetchCaseKeywords()(any[HeaderCarrier]))
        .willReturn(Future(Paged(Seq(caseKeyword))))

      val result = await(
        controller(Set(Permission.MANAGE_USERS))
          .postDisplayManageKeywords(activeSubNav = ManagerToolsKeywordsTab)(
            newFakePOSTRequestWithCSRF(Map("keyword" -> "HATS"))
          )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe
        Some(controllers.v2.routes.ManageKeywordsController.editApprovedKeywords("HATS").path())
    }

    "return Bad Request with form with errors" in {

      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      given(keywordService.fetchCaseKeywords()(any[HeaderCarrier]))
        .willReturn(Future(Paged(Seq(caseKeyword))))

      val result = await(
        controller(Set(Permission.MANAGE_USERS))
          .postDisplayManageKeywords(activeSubNav = ManagerToolsKeywordsTab)(newFakePOSTRequestWithCSRF(Map("" -> "")))
      )

      status(result) shouldBe Status.BAD_REQUEST
    }

  }

}
