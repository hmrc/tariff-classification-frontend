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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import java.time.Instant

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService, KeywordsService}
import uk.gov.hmrc.tariffclassificationfrontend.views.SearchTab
import uk.gov.tariffclassificationfrontend.utils.Cases._

import scala.concurrent.Future

class SearchControllerSpec extends UnitSpec with Matchers with WithFakeApplication with MockitoSugar with ControllerCommons {

  private val fakeRequest = FakeRequest()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val fileStoreService = mock[FileStoreService]
  private val keywordsService = mock[KeywordsService]
  private val operator = mock[Operator]

  private val defaultTab = SearchTab.DETAILS

  private def controller = new SearchController(
    new SuccessfulRequestActions(operator), casesService, keywordsService, fileStoreService, messageApi, appConfig
  )

  private def controller(permission: Set[Permission]) = new SearchController(
    new RequestActionsWithPermissions(permission), casesService, keywordsService, fileStoreService, messageApi, appConfig)


  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "Search" should {

    "redirect to case if searching by reference" in {
      val result = await(controller.search(defaultTab, reference = Some("reference"), page = 2)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.get("reference").url)
    }

    "redirect to case if searching by reference with padding" in {
      val result = await(controller.search(defaultTab, reference = Some(" reference "), page = 2)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.get("reference").url)
    }

    "redirect to default page if reference is empty" in {
      val result = await(controller.search(defaultTab, reference = Some(" "), page = 2)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.IndexController.get().url)
    }

    "not render results if empty" in {
      given(casesService.search(refEq(Search()), refEq(Sort()), refEq(SearchPagination(2)))(any[HeaderCarrier])) willReturn Future.successful(Paged.empty[Case])
      given(fileStoreService.getAttachments(refEq(Seq.empty))(any[HeaderCarrier])) willReturn Future.successful(Map.empty[Case, Seq[StoredAttachment]])
      given(keywordsService.autoCompleteKeywords) willReturn Future.successful(Seq.empty[String])

      val result = await(controller.search(defaultTab, search = Search(), page = 2)(fakeRequest))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) shouldNot include("advanced_search_results")
    }

    "render results if not empty" in {
      // Given
      val search = Search(traderName = Some("trader"), commodityCode = Some("00"))
      val c = aCase()
      val attachment = StoredAttachment("id", public = true, None, None, "file", "image/png", None, Instant.now())

      given(casesService.search(refEq(search), refEq(Sort()), refEq(SearchPagination(2)))(any[HeaderCarrier])) willReturn Future.successful(Paged(Seq(c)))
      given(fileStoreService.getAttachments(refEq(Seq(c)))(any[HeaderCarrier])) willReturn Future.successful(Map(c -> Seq(attachment)))
      given(keywordsService.autoCompleteKeywords) willReturn Future.successful(Seq.empty[String])

      // When
      val request = fakeRequest.withFormUrlEncodedBody(
        "trader_name" -> "trader", "commodity_code" -> "00"
      )
      val result = await(controller.search(defaultTab, search = search, page = 2)(request))

      // Then
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) should include("advanced_search_results")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe Some("search results")
      session(result).get(SessionKeys.backToSearchResultsLinkUrl) shouldBe
        Some("/manage-tariff-classifications/search?addToSearch=false&trader_name=trader&commodity_code=00&page=2#advanced_search_keywords")
    }

    "render errors if form invalid" in {
      // Given
      val search = Search(traderName = Some("trader"))

      given(keywordsService.autoCompleteKeywords) willReturn Future.successful(Seq.empty[String])

      // When
      val request = fakeRequest.withFormUrlEncodedBody("commodity_code" -> "a")
      val result = await(controller.search(defaultTab, search = search, page = 2)(request))

      // Then
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) should include("error-summary")
      contentAsString(result) shouldNot include("advanced_search_results")
    }

    "return OK when user has right permissions" in {
      // Given
      val search = Search(traderName = Some("trader"), commodityCode = Some("00"))
      val c = aCase()
      val attachment = StoredAttachment("id", public = true, None, None, "file", "image/png", None, Instant.now())

      given(casesService.search(refEq(search), refEq(Sort()), refEq(SearchPagination(2)))(any[HeaderCarrier])) willReturn Future.successful(Paged(Seq(c)))
      given(fileStoreService.getAttachments(refEq(Seq(c)))(any[HeaderCarrier])) willReturn Future.successful(Map(c -> Seq(attachment)))
      given(keywordsService.autoCompleteKeywords) willReturn Future.successful(Seq.empty[String])

      // When
      val request = fakeRequest.withFormUrlEncodedBody(
        "trader_name" -> "trader", "commodity_code" -> "00"
      )
      val result = await(controller(Set(Permission.ADVANCED_SEARCH)).search(defaultTab, search = search, page = 2)(request))

      // Then
      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val search = Search(traderName = Some("trader"), commodityCode = Some("00"))
      val request = fakeRequest.withFormUrlEncodedBody(
        "trader_name" -> "trader", "commodity_code" -> "00"
      )
      val result = await(controller(Set.empty).search(defaultTab, search = search, page = 2)(request))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

}
