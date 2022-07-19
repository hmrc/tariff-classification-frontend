/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.routes._
import models._
import models.forms.CaseStatusRadioInputFormProvider
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service.CasesService
import utils.Cases
import views.html.release_or_suppress

import scala.concurrent.ExecutionContext.Implicits.global

class ReleaseOrSuppressCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = Operator(id = "id")
  private val releaseOrSuppress = app.injector.instanceOf[release_or_suppress]

  private val caseBTIWithStatusNEW = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseLiabilityWithStatusNEW =
    Cases.liabilityCaseExample.copy(reference = "reference", status = CaseStatus.NEW)

  private def controller(c: Case) =
    new ReleaseOrSuppressCaseController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      releaseOrSuppress,
      mcc,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new ReleaseOrSuppressCaseController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      releaseOrSuppress,
      mcc,
      realAppConfig
    )

  val form = new CaseStatusRadioInputFormProvider()()

  "ReleaseOrSuppressCaseControllerSpec" should {
    "case is BTI" when {
      "return OK with correct HTML" in {
        val result = await(controller(caseBTIWithStatusNEW).onPageLoad("reference")(newFakeGETRequestWithCSRF()))

        status(result)      shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result)     shouldBe Some("utf-8")
      }

      "return OK when the user has release case permissions" in {
        val result = await(
          controller(caseBTIWithStatusNEW, Set(Permission.RELEASE_CASE))
            .onPageLoad("reference")(newFakeGETRequestWithCSRF())
        )

        status(result)          shouldBe Status.OK
        contentType(result)     shouldBe Some("text/html")
        charset(result)         shouldBe Some("utf-8")
        contentAsString(result) should include("Action this case")
      }

      "return OK when the user has the suppress case permissions" in {
        val result = await(
          controller(caseBTIWithStatusNEW, Set(Permission.SUPPRESS_CASE))
            .onPageLoad("reference")(newFakeGETRequestWithCSRF())
        )

        status(result)          shouldBe Status.OK
        contentType(result)     shouldBe Some("text/html")
        charset(result)         shouldBe Some("utf-8")
        contentAsString(result) should include("Action this case")
      }

      "return unauthorised when user does not have the necessary permissions" in {
        val result = await(
          controller(caseBTIWithStatusNEW, Set(Permission.VIEW_ASSIGNED_CASES))
            .onPageLoad("reference")(newFakeGETRequestWithCSRF())
        )

        status(result)               shouldBe Status.SEE_OTHER
        redirectLocation(result).get should include("unauthorized")
      }

      "redirect to Release case page POST" in {
        val result = await(
          controller(caseBTIWithStatusNEW, Set(Permission.RELEASE_CASE))
            .onSubmit("reference")(
              newFakePOSTRequestWithCSRF()
                .withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Release.toString)
            )
        )

        status(result)               shouldBe Status.SEE_OTHER
        redirectLocation(result).get shouldBe ReleaseCaseController.releaseCase("reference").url
      }

      "redirect to Suppress case page POST" in {
        val result = await(
          controller(caseBTIWithStatusNEW, Set(Permission.SUPPRESS_CASE))
            .onSubmit("reference")(
              newFakePOSTRequestWithCSRF()
                .withFormUrlEncodedBody("caseStatus" -> CaseStatusRadioInput.Suppress.toString)
            )
        )

        status(result)               shouldBe Status.SEE_OTHER
        redirectLocation(result).get shouldBe SuppressCaseController.getSuppressCaseReason("reference").url
      }

      "redirect to change case status page when form has errors" in {
        val result =
          controller(caseBTIWithStatusNEW, Set(Permission.RELEASE_CASE))
            .onSubmit("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("caseStatus" -> ""))

        contentAsString(result) should include("Select a case status")
      }
    }

    "case is Liability" when {
      "return OK with correct HTML" in {
        val result =
          await(controller(caseLiabilityWithStatusNEW).onPageLoad("reference")(newFakeGETRequestWithCSRF()))

        status(result)      shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result)     shouldBe Some("utf-8")
      }

      "return OK when the user has release case permissions" in {
        val result = await(
          controller(caseLiabilityWithStatusNEW, Set(Permission.RELEASE_CASE))
            .onPageLoad("reference")(newFakeGETRequestWithCSRF())
        )

        status(result)          shouldBe Status.OK
        contentType(result)     shouldBe Some("text/html")
        charset(result)         shouldBe Some("utf-8")
        contentAsString(result) should include("Action this case")
      }

      "return unauthorised when user does not have the necessary permissions" in {
        val result = await(
          controller(caseLiabilityWithStatusNEW, Set(Permission.VIEW_ASSIGNED_CASES))
            .onPageLoad("reference")(newFakeGETRequestWithCSRF())
        )

        status(result)               shouldBe Status.SEE_OTHER
        redirectLocation(result).get should include("unauthorized")
      }
    }
  }
}
