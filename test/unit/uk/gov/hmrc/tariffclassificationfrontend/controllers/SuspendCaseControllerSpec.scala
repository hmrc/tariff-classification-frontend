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
import play.api.mvc.{ActionRefiner, Result}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Operator}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.{failed, successful}

class SuspendCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val caseWithStatusNEW = Cases.btiCaseExample.copy(status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
  private val caseWithStatusSUSPENDED = Cases.btiCaseExample.copy(status = CaseStatus.SUSPENDED)

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val requestActions = mock[SuccessfulRequestActions]

  private val controller = new SuspendCaseController(requestActions, casesService, messageApi, appConfig)

  override def beforeEach(): Unit = {
    when(requestActions.authenticate).thenReturn(new SuccessfulAuthenticatedAction(operator))
    when(requestActions.setPermissions).thenReturn( new SuccessfulCheckPermissionsAction(operator))
    when(requestActions.mustHaveWritePermission).thenReturn(new SuccessfulMustHaveWritePermissionAction((operator)))
  }

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  "Suspend Case" should {

    "return OK and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusOPEN)))

      val result: Result = await(controller.suspendCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Suspend this case")
    }

    "redirect to Application Details for non OPEN statuses" in {

      //TODO : How to test this onee


      when(requestActions.caseExists(refEq("reference"))).thenReturn())

      val result: Result = await(controller.suspendCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return Not Found and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(None))

      val result: Result = await(controller.suspendCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("We could not find a Case with reference")
    }

  }

  "Confirm Suspend a Case" should {

    "return OK and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusOPEN)))
      when(casesService.suspendCase(refEq(caseWithStatusOPEN), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUSPENDED))

      val result: Result = await(controller.confirmSuspendCase("reference")
                                (newFakePOSTRequestWithCSRF(fakeApplication)
                                  .withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This case has been suspended")
    }

    "return OK and HTML content type for reject error page" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusOPEN)))
      when(casesService.suspendCase(refEq(caseWithStatusOPEN), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusSUSPENDED))

      val result: Result = await(controller.confirmSuspendCase("reference")
                                (newFakePOSTRequestWithCSRF(fakeApplication)
                                  .withFormUrlEncodedBody("state" -> "false")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("You must contact the applicant")
    }

    "redirect to Application Details for non OPEN statuses" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusNEW)))

      val result: Result = await(controller.confirmSuspendCase("reference")
                                (newFakePOSTRequestWithCSRF(fakeApplication)
                                  .withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return Not Found and HTML content type on missing Case" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(None))
      val result: Result = await(controller.confirmSuspendCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("We could not find a Case with reference")
    }

    "propagate the error in case the CaseService fails to release the case" in {
      val error = new IllegalStateException("expected error")

      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(failed(error))

      val caught = intercept[error.type] {
        await(controller.confirmSuspendCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))
      }
      caught shouldBe error
    }
  }

}
