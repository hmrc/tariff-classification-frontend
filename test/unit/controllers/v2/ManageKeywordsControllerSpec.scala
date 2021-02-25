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
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.`given`
import play.api.http.Status
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import service.ManageKeywordsService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.managementtools.{confirm_keyword_created, manage_keywords_view, new_keyword_view}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ManageKeywordsControllerSpec extends ControllerBaseSpec {

  val keywords    = Seq(Keyword("shoes", true), Keyword("hats", true), Keyword("shirts", true))
  val keywordForm = KeywordForm.formWithAuto(keywords.map(_.name))
  val caseKeyword = CaseKeyword(Keyword("BOOK", false), List(CaseHeader("ref", None, None, Some("NOTEBOOK"), AppType.BTI, CaseStatus.REFERRED)))

  private lazy val manage_keywords_view = injector.instanceOf[manage_keywords_view]
  private lazy val confirm_keyword_view = injector.instanceOf[confirm_keyword_created]
  private lazy val new_keyword_view = injector.instanceOf[new_keyword_view]
  private lazy val keywordService = mock[ManageKeywordsService]

  private def controller(permission: Set[Permission]) = new ManageKeywordsController(
    new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
    mcc,
    keywordService,
    manage_keywords_view,
    confirm_keyword_view,
    new_keyword_view,
    realAppConfig
  )

  "displayManageKeywords" should {

    "return 200 OK and HTML content type" in {
      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))
      given(keywordService.fetchCaseKeywords()(any[HeaderCarrier]))
        .willReturn(Future(Paged(Seq(caseKeyword))))

      val result = await(controller(Set(Permission.MANAGE_USERS)).displayManageKeywords()(fakeRequest))
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

      val result = await(controller(Set(Permission.MANAGE_USERS)).createKeyword()(newFakePOSTRequestWithCSRF(Map("keyword" -> "newkeyword"))))

      status(result)      shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.v2.routes.ManageKeywordsController.displayConfirmKeyword("newkeyword").path())

    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).createKeyword()(newFakePOSTRequestWithCSRF(Map("keyword" -> "newkeyword"))))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)
    }

    "render error if keyword empty" in {

      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      val result = await(controller(Set(Permission.MANAGE_USERS)).createKeyword()(newFakePOSTRequestWithCSRF(Map("keyword" -> ""))))

      status(result)      shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      contentAsString(result) should include("error-summary")
      contentAsString(result) should include(messages("management.create-keyword.error.empty.keyword"))

    }

    "render error if duplicate keyword entered" in {
      given(keywordService.findAll(refEq(NoPagination()))(any[HeaderCarrier]))
        .willReturn(Future(Paged(keywords)))

      val result = await(controller(Set(Permission.MANAGE_USERS)).createKeyword()(newFakePOSTRequestWithCSRF(Map("keyword" -> keywords.head.name))))

      status(result)      shouldBe Status.BAD_REQUEST
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      contentAsString(result) should include("error-summary")
      contentAsString(result) should include(messages("management.create-keyword.error.duplicate.keyword"))
    }

  }

}