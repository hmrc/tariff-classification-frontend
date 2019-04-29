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

class SuspendCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val caseWithStatusNEW = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusSUSPENDED = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.SUSPENDED)

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(c: Case) = new SuspendCaseController(
    new SuccessfulRequestActions(operator, c = c), casesService, messageApi, appConfig)

  private def controller(requestCase: Case, permission: Set[Permission]) = new SuspendCaseController(
    new RequestActionsWithPermissions(permission, c = requestCase), casesService, messageApi, appConfig)


  "Suspend Case" should {

    "return OK and HTML content type" in {

      val result: Result = await(controller(caseWithStatusOPEN).suspendCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Suspend this case")
    }

    "redirect to Application Details for non OPEN statuses" in {
      val con = controller(caseWithStatusNEW)

      val result: Result = await(con.suspendCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return OK when user has right permissions" in {
      val result: Result = await(controller(caseWithStatusOPEN, Set(Permission.SUSPEND_CASE))
        .suspendCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusNEW, Set.empty)
        .suspendCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Confirm Suspend a Case" should {

    "return OK and HTML content type" in {
      when(casesService.suspendCase(refEq(caseWithStatusOPEN), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUSPENDED))

      val result: Result = await(controller(caseWithStatusOPEN).confirmSuspendCase("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This case has been suspended")
    }

    "return OK and HTML content type for reject error page" in {
      when(casesService.suspendCase(refEq(caseWithStatusOPEN), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUSPENDED))

      val result: Result = await(controller(caseWithStatusOPEN).confirmSuspendCase("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("state" -> "false")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("You must contact the applicant")
    }

    "redirect to Application Details for non OPEN statuses" in {

      val result: Result = await(controller(caseWithStatusNEW).confirmSuspendCase("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return OK when user has right permissions" in {
      when(casesService.suspendCase(any[Case], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUSPENDED))

      val result: Result = await(controller(caseWithStatusOPEN, Set(Permission.SUSPEND_CASE))
        .confirmSuspendCase("reference")
        (newFakePOSTRequestWithCSRF(fakeApplication)
          .withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusNEW, Set.empty)
        .confirmSuspendCase("reference")
        (newFakePOSTRequestWithCSRF(fakeApplication)
          .withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

}
