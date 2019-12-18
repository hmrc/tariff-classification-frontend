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
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.Result
import play.api.test.Helpers.{redirectLocation, _}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus, Operator, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.successful

class ReopenCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val btiCaseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val btiCaseWithStatusREFERRED = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.REFERRED)
  private val btiCaseWithStatusSUSPENDED = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.SUSPENDED)

  private val liabilityCaseWithStatusOpen = Cases.liabilityCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val liabilityCaseWithStatusSuspended = Cases.liabilityCaseExample.copy(reference = "reference", status = CaseStatus.SUSPENDED)

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(c: Case) = new ReopenCaseController(
    new SuccessfulRequestActions(operator, c = c), casesService, messageApi, appConfig)

  private def controller(requestCase: Case, permission: Set[Permission]) = new ReopenCaseController(
    new RequestActionsWithPermissions(permission, c = requestCase), casesService, messageApi, appConfig)


  "ReopenCaseControllerSpec" should {

    "return 303 and redirect to applicant details (case_details page) for BTI when case is referred" in {
      when(casesService.reopenCase(refEq(btiCaseWithStatusREFERRED), any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(btiCaseWithStatusOPEN))

      val result: Result = await(controller(btiCaseWithStatusREFERRED)
        .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/applicant")
    }

    "return 303 and redirect to applicant details (case_details page) for BTI when case is suspended" in {
      when(casesService.reopenCase(refEq(btiCaseWithStatusSUSPENDED), any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(btiCaseWithStatusOPEN))

      val result: Result = await(controller(btiCaseWithStatusSUSPENDED)
        .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/applicant")
    }

    "return 303 and redirect to liability details (liability_details page) for liability when case is suspended" in {
      when(casesService.reopenCase(refEq(liabilityCaseWithStatusSuspended), any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(liabilityCaseWithStatusOpen))

      val result: Result = await(controller(liabilityCaseWithStatusSuspended)
        .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/liability")
    }

    "return 303 when user has right permissions" in {
      when(casesService.reopenCase(any[Case], any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(btiCaseWithStatusOPEN))

      val result: Result = await(controller(btiCaseWithStatusREFERRED, Set(Permission.REOPEN_CASE))
        .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/applicant")
    }

    "redirect to unauthorised when user does not have the right permissions" in {
      val result: Result = await(controller(btiCaseWithStatusREFERRED, Set.empty)
        .confirmReopenCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }
}
