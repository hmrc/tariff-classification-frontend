/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import models._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, FileStoreService, KeywordsService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._
import views.SearchTab
import views.html.advanced_search

import java.time.Instant
import scala.concurrent.Future

class SearchControllerSpec extends ControllerBaseSpec {

  private val casesService     = mock[CasesService]
  private val fileStoreService = mock[FileStoreService]
  private val keywordsService  = mock[KeywordsService]
  private val operator         = Operator(id = "id")
  private val advancedSearch   = app.injector.instanceOf[advanced_search]
  private val defaultTab       = SearchTab.DETAILS

  private def controller = new SearchController(
    new SuccessfulRequestActions(playBodyParsers, operator),
    casesService,
    keywordsService,
    fileStoreService,
    mcc,
    advancedSearch,
    realAppConfig
  )

  private def controller(permission: Set[Permission]) =
    new SearchController(
      new RequestActionsWithPermissions(playBodyParsers, permission),
      casesService,
      keywordsService,
      fileStoreService,
      mcc,
      advancedSearch,
      realAppConfig
    )

  "Search" should {

    "redirect to case if searching by reference and the case exists" in {
      given(casesService.getOne(ArgumentMatchers.eq("reference"))(any[HeaderCarrier])) willReturn Future.successful(
        Some(mock[Case])
      )
      val result = await(controller.search(defaultTab, reference = Some("reference"), page = 2)(fakeRequest))

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.get("reference").url)
    }

    "redirect to case if searching by reference with padding and the case exists" in {
      given(casesService.getOne(ArgumentMatchers.eq("reference"))(any[HeaderCarrier])) willReturn Future.successful(
        Some(mock[Case])
      )
      val result = await(controller.search(defaultTab, reference = Some(" reference "), page = 2)(fakeRequest))

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.get("reference").url)
    }

    "render the advanced search page if searching by reference and the case does not exist" in {
      given(casesService.getOne(ArgumentMatchers.eq("reference"))(any[HeaderCarrier])) willReturn Future.successful(
        None
      )
      given(keywordsService.findAll()(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[Keyword])
      val result = await(controller.search(defaultTab, reference = Some("reference"), page = 2)(fakeRequest))

      status(result)        shouldBe Status.OK
      contentType(result)   shouldBe Some("text/html")
      charset(result)       shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) shouldNot include("advanced_search_results")
    }

    "render the advanced search page if searching by reference but it is empty" in {
      given(keywordsService.findAll()(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[Keyword])
      val result = await(controller.search(defaultTab, reference = Some(" "), page = 2)(fakeRequest))

      status(result)        shouldBe Status.OK
      contentType(result)   shouldBe Some("text/html")
      charset(result)       shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) shouldNot include("advanced_search_results")
    }

    "not render results if empty" in {
      given(
        casesService.search(refEq(Search()), refEq(Sort()), refEq(SearchPagination(2)))(any[HeaderCarrier])
      ) willReturn Future
        .successful(Paged.empty[Case])
      given(fileStoreService.getAttachments(refEq(Seq.empty))(any[HeaderCarrier])) willReturn Future.successful(
        Map.empty[Case, Seq[StoredAttachment]]
      )
      given(keywordsService.findAll()(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[Keyword])

      val result = await(controller.search(defaultTab, search = Search(), page = 2)(fakeRequest))

      status(result)        shouldBe Status.OK
      contentType(result)   shouldBe Some("text/html")
      charset(result)       shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) shouldNot include("advanced_search_results")
    }

    "render results if not empty" in {

      val search = Search(caseSource = Some("trader"), commodityCode = Some("00"))
      val c      = aCase()
      val attachment =
        StoredAttachment(
          "id",
          public = true,
          None,
          None,
          Some("file"),
          Some("image/png"),
          None,
          Instant.now(),
          Some("test description"),
          shouldPublishToRulings = true
        )

      given(
        casesService.search(refEq(search), refEq(Sort()), refEq(SearchPagination(2)))(any[HeaderCarrier])
      ) willReturn Future
        .successful(Paged(Seq(c)))
      given(fileStoreService.getAttachments(refEq(Seq(c)))(any[HeaderCarrier])) willReturn Future.successful(
        Map(c -> Seq(attachment))
      )
      given(keywordsService.findAll()(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[Keyword])

      val request = fakeRequest.withFormUrlEncodedBody(
        "case_source"    -> "trader",
        "commodity_code" -> "00"
      )
      val result = await(controller.search(defaultTab, search = search, page = 2)(request))

      status(result)                                                shouldBe Status.OK
      contentType(result)                                           shouldBe Some("text/html")
      charset(result)                                               shouldBe Some("utf-8")
      contentAsString(result)                                         should include("advanced_search-heading")
      contentAsString(result)                                         should include("advanced_search_results")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe Some("search results")
      session(result).get(SessionKeys.backToSearchResultsLinkUrl) shouldBe
        Some(
          "/manage-tariff-classifications/search?addToSearch=false&case_source=trader&commodity_code=00&page=2#advanced_search_keywords"
        )
    }

    "render errors if form invalid" in {

      val search = Search(caseSource = Some("trader"))

      given(keywordsService.findAll()(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[Keyword])

      val request = fakeRequest.withFormUrlEncodedBody("commodity_code" -> "a")
      val result  = await(controller.search(defaultTab, search = search, page = 2)(request))

      status(result)        shouldBe Status.OK
      contentType(result)   shouldBe Some("text/html")
      charset(result)       shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) should include("error-summary")
      contentAsString(result) shouldNot include("advanced_search_results")
    }

    "return OK when user has right permissions" in {

      val search = Search(caseSource = Some("trader"), commodityCode = Some("00"))
      val c      = aCase()
      val attachment =
        StoredAttachment(
          "id",
          public = true,
          None,
          None,
          Some("file"),
          Some("image/png"),
          None,
          Instant.now(),
          Some("test description"),
          shouldPublishToRulings = true
        )

      given(
        casesService.search(refEq(search), refEq(Sort()), refEq(SearchPagination(2)))(any[HeaderCarrier])
      ) willReturn Future
        .successful(Paged(Seq(c)))
      given(fileStoreService.getAttachments(refEq(Seq(c)))(any[HeaderCarrier])) willReturn Future.successful(
        Map(c -> Seq(attachment))
      )
      given(keywordsService.findAll()(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[Keyword])

      val request = fakeRequest.withFormUrlEncodedBody(
        "case_source"    -> "trader",
        "commodity_code" -> "00"
      )
      val result =
        await(controller(Set(Permission.ADVANCED_SEARCH)).search(defaultTab, search = search, page = 2)(request))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val search = Search(caseSource = Some("trader"), commodityCode = Some("00"))
      val request = fakeRequest.withFormUrlEncodedBody(
        "case_source"    -> "trader",
        "commodity_code" -> "00"
      )
      val result = await(controller(Set.empty).search(defaultTab, search = search, page = 2)(request))

      status(result)             shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

}
