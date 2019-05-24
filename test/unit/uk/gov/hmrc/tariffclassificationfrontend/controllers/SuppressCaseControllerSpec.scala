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
import play.api.http.{MimeTypes, Status}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.Result
import play.api.test.Helpers.{redirectLocation, _}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus, Operator, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.successful

class SuppressCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val caseWithStatusNEW = Cases.btiCaseExample.copy(status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
  private val caseWithStatusSUPRRESSED = Cases.btiCaseExample.copy(status = CaseStatus.SUPPRESSED)

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def controller(requestCase: Case = caseWithStatusNEW) = new SuppressCaseController(
    new SuccessfulRequestActions(operator, c = requestCase), casesService, messageApi, appConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new SuppressCaseController(
    new RequestActionsWithPermissions(permission, c = requestCase), casesService, messageApi, appConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  "Suppress Case" should {

    "return OK and HTML content type" in {
      val result: Result = await(controller(caseWithStatusNEW).getSuppressCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Contact the applicant and attach a copy of the email before you suppress this case")
    }

    "redirect to Application Details for non NEW statuses" in {
      val result: Result = await(controller(caseWithStatusOPEN).getSuppressCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return OK when user has right permissions" in {
      val result: Result = await(controller(caseWithStatusNEW, Set(Permission.SUPPRESS_CASE))
        .getSuppressCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }


    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusNEW, Set.empty).getSuppressCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "Confirm Suppress a Case" should {

    "redirect to confirmation when operator indicates that applicant has been contacted" in {
      when(casesService.suppressCase(refEq(caseWithStatusNEW), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUPRRESSED))

      val result: Result =
        await(controller(caseWithStatusNEW).postSuppressCase("reference")
             (newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/suppress/confirmation")
    }

    "redirect to contact info when operator indicates that applicant has not been contacted" in {
      when(casesService.suppressCase(refEq(caseWithStatusNEW), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUPRRESSED))

      val result: Result =
        await(controller(caseWithStatusNEW).postSuppressCase("reference")
             (newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("state" -> "false")))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/suppress/contact-info")
    }

    "redirect to Application Details for non NEW statuses" in {
      val result: Result = await(controller(caseWithStatusOPEN).postSuppressCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return OK when user has right permissions" in {
      when(casesService.suppressCase(any[Case], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUPRRESSED))

      val result: Result = await(controller(caseWithStatusNEW, Set(Permission.SUPPRESS_CASE))
        .postSuppressCase("reference")
        (newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.SEE_OTHER
    }


    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusNEW, Set.empty)
        .postSuppressCase("reference")
        (newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "View Confirm page for a suppressed case" should {

    "return OK and HTML content type" in {
      when(casesService.suppressCase(refEq(caseWithStatusSUPRRESSED), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUPRRESSED))

      val result: Result = await(controller(caseWithStatusSUPRRESSED).confirmSuppressCase("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This case has been suppressed")
    }

    "redirect to a default page if the status is not right" in {
      when(casesService.suppressCase(refEq(caseWithStatusOPEN), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUPRRESSED))

      val result: Result = await(controller(caseWithStatusOPEN).confirmSuppressCase("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/1/application")
    }
  }

  "View contact info page for a case that was not suppressed" should {

    "return OK and HTML content type" in {
      val result: Result = await(controller(caseWithStatusNEW).showContactInformation("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("state" -> "false")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("You must contact the applicant and attach a copy of the email to this case before you suppress it")
    }

    "redirect to a default page if the status is not right" in {
      val result: Result = await(controller(caseWithStatusSUPRRESSED).showContactInformation("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("state" -> "false")))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/1/application")
    }
  }

}
