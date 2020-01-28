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

import akka.stream.Materializer
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, Messages}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes._
import uk.gov.hmrc.tariffclassificationfrontend.forms.CaseStatusRadioInputFormProvider
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases

class ReleaseOrSuppressCaseControllerSpec extends WordSpec
  with Matchers
  with UnitSpec
  with WithFakeApplication
  with MockitoSugar
  with BeforeAndAfterEach
  with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]
  private val messages: Messages = messageApi.preferred(newFakeGETRequestWithCSRF(fakeApplication))

  private val caseWithStatusNEW = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusNEWWithDecision = Cases.btiCaseWithIncompleteDecision.copy(reference = "reference", status = CaseStatus.OPEN)

  

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def controller(c: Case) = new ReleaseOrSuppressCaseController(
    new SuccessfulRequestActions(operator, c = c), casesService, messageApi, appConfig)

  private def controller(requestCase: Case, permission: Set[Permission]) = new ReleaseOrSuppressCaseController(
    new RequestActionsWithPermissions(permission, c = requestCase), casesService, messageApi, appConfig)

  val form = new CaseStatusRadioInputFormProvider()()


  "ReleaseOrSuppressCaseControllerSpec" should {

    "return OK with correct HTML" in {
      val result = await(controller(caseWithStatusNEW).onPageLoad("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return OK when the user has release case permissions" in {
      val result = await(controller(caseWithStatusNEW, Set(Permission.RELEASE_CASE)).onPageLoad("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Change case status")
    }

    "return OK when the user has the suppress case permissions" in {
      val result = await(controller(caseWithStatusNEW, Set(Permission.SUPPRESS_CASE)).onPageLoad("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Change case status")
    }

    "return unauthorised when user does not have the necessary permissions" in {
      val result = await(controller(caseWithStatusNEW, Set(Permission.VIEW_ASSIGNED_CASES)).onPageLoad("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

    "redirect to Release case page POST" in {
      val result = await(
        controller(caseWithStatusNEW, Set(Permission.RELEASE_CASE))
          .onSubmit("reference")(
            newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Release.toString)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe ReleaseCaseController.releaseCase("reference").url
    }

    "redirect to Suppress case page POST" in {
      val result = await(
        controller(caseWithStatusNEW, Set(Permission.SUPPRESS_CASE))
          .onSubmit("reference")(
            newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Suppress.toString)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get shouldBe SuppressCaseController.getSuppressCase("reference").url
    }


    "redirect to change case status page when form has errors" in {
      val result =
        controller(caseWithStatusNEW, Set(Permission.RELEASE_CASE))
          .onSubmit("reference")(
            newFakePOSTRequestWithCSRF(fakeApplication).withFormUrlEncodedBody("caseStatus" -> ""))

      contentAsString(result) should include("Select a case status")
    }
  }
}
