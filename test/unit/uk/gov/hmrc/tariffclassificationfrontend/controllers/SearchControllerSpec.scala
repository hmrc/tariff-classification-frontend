/*
 * Copyright 2019 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{refEq, _}
import org.mockito.BDDMockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, KeywordsService}

import scala.concurrent.Future

class SearchControllerSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {

  private val fakeRequest = FakeRequest()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val keywordsService = mock[KeywordsService]
  private val operator = mock[Operator]

  private val controller = new SearchController(
    new SuccessfulAuthenticatedAction(operator),
    casesService,
    keywordsService,
    messageApi,
    appConfig
  )

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "Search" should {

    "redirect to case if searching by reference" in {
      val result = await(controller.search(reference = Some("reference"), page = 2)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.CaseController.trader("reference").url)
    }

    "not render results if empty" in {
      given(casesService.search(refEq(Search()), refEq(Sort()), refEq(SearchPagination(2)))(any[HeaderCarrier])) willReturn Future.successful(Paged.empty[Case])
      given(keywordsService.autoCompleteKeywords) willReturn Future.successful(Seq.empty[String])

      val result = await(controller.search(search = Search(), page = 2)(fakeRequest))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) shouldNot include("advanced_search_results")
    }

    "render results if not empty" in {
      // Given
      val search = Search(traderName = Some("trader"), commodityCode = Some("00"))

      given(casesService.search(refEq(search), refEq(Sort()), refEq(SearchPagination(2)))(any[HeaderCarrier])) willReturn Future.successful(Paged.empty[Case])
      given(keywordsService.autoCompleteKeywords) willReturn Future.successful(Seq.empty[String])

      // When
      val request = fakeRequest.withFormUrlEncodedBody(
        "trader_name" -> "trader", "commodity_code" -> "00"
      )
      val result = await(controller.search(search = search, page = 2)(request))

      // Then
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) should include("advanced_search_results")
    }

    "render errors if form invalid" in {
      // Given
      val search = Search(traderName = Some("trader"))

      given(casesService.search(refEq(search), refEq(Sort()), refEq(SearchPagination(2)))(any[HeaderCarrier])) willReturn Future.successful(Paged.empty[Case])
      given(keywordsService.autoCompleteKeywords) willReturn Future.successful(Seq.empty[String])

      // When
      val request = fakeRequest.withFormUrlEncodedBody("commodity_code" -> "a")
      val result = await(controller.search(search = search, page = 2)(request))

      // Then
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("advanced_search-heading")
      contentAsString(result) shouldNot include("advanced_search_results")
    }

  }

}
