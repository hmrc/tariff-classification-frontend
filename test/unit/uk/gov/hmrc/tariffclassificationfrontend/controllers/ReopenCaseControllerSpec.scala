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

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{MimeTypes, Status}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Result}
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.{Configuration, Environment}
import play.filters.csrf.CSRF.{Token, TokenProvider}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus, Operator}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.{failed, successful}

class ReopenCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach with ControllerAssertions {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val caseWithStatusNEW = Cases.btiCaseExample.copy(status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
  private val caseWithStatusREFERRED = Cases.btiCaseExample.copy(status = CaseStatus.REFERRED)
  private val caseWithStatusSUSPENDED = Cases.btiCaseExample.copy(status = CaseStatus.SUSPENDED)

  private implicit val mat: Materializer = app.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val controller = new ReopenCaseController(new SuccessfulAuthenticatedAction(operator), casesService, messageApi, appConfig)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  "Reopen Case" should {

    "return OK and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusREFERRED)))

      val result: Result = await(controller.reopenCase("reference")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Reopen this case")
    }

    "redirect to Application Details for non REFERRED or SUSPENDED statuses" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusNEW)))

      val result: Result = await(controller.reopenCase("reference")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return Not Found and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(None))

      val result: Result = await(controller.reopenCase("reference")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("We could not find a Case with reference")
    }

  }

  "Confirm Reopen a Case" should {

    "return OK and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusREFERRED)))
      when(casesService.reopenCase(refEq(caseWithStatusREFERRED), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(controller.confirmReopenCase("reference")(newFakePOSTRequestWithCSRF()))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This case has been reopen")
    }


    def doNotRedirectWith(allowedStatus: CaseStatus*): Unit = {

      for (s <- CaseStatus.values) {
        val statusCase = Cases.btiCaseExample.copy(status = s)
        when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(statusCase)))
        when(casesService.reopenCase(any[Case], refEq(operator))(any[HeaderCarrier])).thenReturn(successful(statusCase))
        val result: Result = await(controller.confirmReopenCase("reference")(newFakePOSTRequestWithCSRF()))
        if (allowedStatus.contains(s)) {
          withClue(s"Status $s must be redirected") {
            status(result) shouldBe Status.OK
          }
        } else {
          withClue(s"Status $s has not been redirected") {
            status(result) shouldBe Status.SEE_OTHER
          }
          contentTypeOf(result) shouldBe None
          charsetOf(result) shouldBe None
          locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
        }
      }
    }


    "redirect to Application Details for non REFERRED or SUSPENDED statuses" in {
      doNotRedirectWith(CaseStatus.REFERRED, CaseStatus.SUSPENDED)
    }

    "return Not Found and HTML content type on missing Case" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(None))
      val result: Result = await(controller.confirmReopenCase("reference")(newFakePOSTRequestWithCSRF()))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("We could not find a Case with reference")
    }

    "propagate the error in case the CaseService fails to release the case" in {
      val error = new IllegalStateException("expected error")

      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(failed(error))

      val caught = intercept[error.type] {
        await(controller.confirmReopenCase("reference")(newFakePOSTRequestWithCSRF()))
      }
      caught shouldBe error
    }
  }

  private def newFakeGETRequestWithCSRF(): FakeRequest[AnyContentAsEmpty.type] = {
    val tokenProvider: TokenProvider = app.injector.instanceOf[TokenProvider]
    val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)
    FakeRequest("GET", "/", FakeHeaders(), AnyContentAsEmpty, tags = csrfTags)
  }

  private def newFakePOSTRequestWithCSRF(): FakeRequest[AnyContentAsFormUrlEncoded] = {
    val tokenProvider: TokenProvider = app.injector.instanceOf[TokenProvider]
    val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)
    FakeRequest("POST", "/", FakeHeaders(), AnyContentAsFormUrlEncoded, tags = csrfTags).withFormUrlEncodedBody()
  }

}
