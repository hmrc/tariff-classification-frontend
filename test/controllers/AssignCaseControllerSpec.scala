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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.mvc.Result
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation}
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._
import views.html.assign_case

import scala.concurrent.Future.successful

class AssignCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = Operator("id")
  private val assignCase   = injector.instanceOf[assign_case]

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(requestCase: Case) = new AssignCaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    mcc,
    assignCase,
    realAppConfig
  )

  private def controller(requestCase: Case, permissions: Set[Permission]) = new AssignCaseController(
    new RequestActionsWithPermissions(playBodyParsers, permissions = permissions, c = requestCase),
    casesService,
    mcc,
    assignCase,
    realAppConfig
  )

  "Assign Case" should {

    "redirect to unauthorised if no permissions" in {
      val result = await(controller(aCase(), Set.empty).get("")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return OK and HTML content type when user has right permissions" in {
      val aCaseWithQueue = aCase(withQueue("1"))

      val result: Result =
        await(controller(aCaseWithQueue, Set(Permission.ASSIGN_CASE)).get("reference")(newFakeGETRequestWithCSRF()))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("assign_case-heading")
    }

    "redirect to Case Index for cases assigned to self" in {
      val aCaseAssignedToSelf = aCase(withQueue("1"), withAssignee(Some(operator)))

      val result: Result = await(controller(aCaseAssignedToSelf).get("reference")(newFakeGETRequestWithCSRF()))

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.CaseController.get("reference").url)
    }

  }

  "Confirm Assign Case" should {

    "redirect to unauthorised if no permissions" in {
      val result = await(controller(aCase(), Set.empty).post("reference")(newFakePOSTRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return OK and HTML content type when user has right permissions" in {
      val aCaseWithQueue = aCase(withQueue("1"))
      when(casesService.assignCase(any[Case], any[Operator])(any[HeaderCarrier])).thenReturn(successful(aCaseWithQueue))

      val result: Result = await(
        controller(aCaseWithQueue, Set(Permission.ASSIGN_CASE))
          .post("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("state" -> "true"))
      )

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.CaseController.get("reference").url)
    }

    "redirect to Case Index for cases assigned to self" in {
      val aCaseAssignedToSelf = aCase(withQueue("1"), withAssignee(Some(operator)))

      val result: Result = await(
        controller(aCaseAssignedToSelf)
          .post("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("state" -> "true"))
      )

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.CaseController.get("reference").url)
    }

    "redirect to Assign for cases already assigned" in {
      val aCaseAssignedToSelf = aCase(withQueue("1"), withAssignee(Some(Operator("other-id"))))

      val result: Result = await(
        controller(aCaseAssignedToSelf)
          .post("reference")(newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("state" -> "true"))
      )

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some(routes.AssignCaseController.get("reference").url)
    }

  }

}
