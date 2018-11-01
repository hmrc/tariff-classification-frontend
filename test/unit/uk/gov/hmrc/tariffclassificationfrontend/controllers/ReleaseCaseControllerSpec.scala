/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.{Configuration, Environment}
import play.filters.csrf.CSRF.{Token, TokenProvider}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.hmrc.tariffclassificationfrontend.utils.CaseExamples

import scala.concurrent.Future

class ReleaseCaseControllerSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val queueService = mock[QueuesService]
  private implicit val hc = HeaderCarrier()

  private val controller = new ReleaseCaseController(casesService, queueService, messageApi, appConfig)

  "Release Case" should {
    val caseWithStatusNEW = CaseExamples.btiCaseExample.copy(status = "NEW")
    val caseWithStatusOPEN = CaseExamples.btiCaseExample.copy(status = "OPEN")

    "return OK and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(caseWithStatusNEW)))
      given(queueService.getNonGateway).willReturn(Seq.empty)

      val result = controller.releaseCase("reference")(newFakeRequestWithCSRF())
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("Release this Case for Classification")
    }

    "redirect to Application Details for non NEW Statuses" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(caseWithStatusOPEN)))
      given(queueService.getNonGateway).willReturn(Seq.empty)

      val result = controller.releaseCase("reference")(newFakeRequestWithCSRF())
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("Application Details")
    }

    "return Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))
      given(queueService.getNonGateway).willReturn(Seq.empty)

      val result = controller.releaseCase("reference")(newFakeRequestWithCSRF())
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }

  private def newFakeRequestWithCSRF(method: String = "GET"): FakeRequest[AnyContentAsEmpty.type] = {
    val tokenProvider: TokenProvider = app.injector.instanceOf[TokenProvider]
    val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)
    FakeRequest(method, "/", FakeHeaders(), AnyContentAsEmpty, tags = csrfTags)
  }
}
