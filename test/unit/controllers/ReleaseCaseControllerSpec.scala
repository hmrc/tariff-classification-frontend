/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{Permission, _}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, _}
import service.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.resource_not_found

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

class ReleaseCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService               = mock[CasesService]
  private val queueService               = mock[QueuesService]
  private val queue                      = mock[Queue]
  private val operator                   = Operator(id = "id")
  private val releaseCaseView            = injector.instanceOf[views.html.release_case]
  private val confirmation_case_creation = injector.instanceOf[views.html.v2.confirmation_case_creation]
  private val resourceNotFound           = injector.instanceOf[resource_not_found]
  private val caseWithStatusNEW          = Cases.btiCaseExample.copy(status = CaseStatus.NEW)
  private val caseWithStatusOPEN         = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(requestCase: Case) = new ReleaseCaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    queueService,
    mcc,
    releaseCaseView,
    confirmation_case_creation,
    resourceNotFound,
    realAppConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new ReleaseCaseController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    queueService,
    mcc,
    releaseCaseView,
    confirmation_case_creation,
    resourceNotFound,
    realAppConfig
  )

  "Release Case" should {

    "return OK and HTML content type" in {
      when(queueService.getAllForCaseType(any[ApplicationType])).thenReturn(successful(List.empty))

      val result: Result =
        await(controller(caseWithStatusNEW).releaseCase("reference")(newFakeGETRequestWithCSRF()))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("Choose a team to release this case to")
    }

    "return OK when user has right permissions" in {
      when(queueService.getAllForCaseType(any[ApplicationType])).thenReturn(successful(List.empty))

      val result: Result = await(
        controller(caseWithStatusNEW, Set(Permission.RELEASE_CASE))
          .releaseCase("reference")(newFakeGETRequestWithCSRF())
      )

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result =
        await(controller(caseWithStatusNEW, Set.empty).releaseCase("reference")(newFakeGETRequestWithCSRF()))

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "Release Case To Queue" should {

    "return OK and HTML content type" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(casesService.releaseCase(refEq(caseWithStatusNEW), any[Queue], any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(caseWithStatusOPEN))

      val result: Result =
        await(controller(caseWithStatusNEW).releaseCaseToQueue("reference")(requestWithQueue()))

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/release/confirmation")
    }

    "redirect to resource not found when the queue specified is not recognised" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(None))

      val result: Result =
        await(controller(caseWithStatusNEW).releaseCaseToQueue("reference")(requestWithQueue()))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("Queue queue not found")
    }

    "redirect back to case on Form Error" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getAllForCaseType(any[ApplicationType])).thenReturn(successful(List.empty))
      when(casesService.releaseCase(refEq(caseWithStatusNEW), any[Queue], refEq(operator))(any[HeaderCarrier]))
        .thenReturn(successful(caseWithStatusOPEN))

      val result: Result =
        await(controller(caseWithStatusNEW).releaseCaseToQueue("reference")(newFakePOSTRequestWithCSRF()))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("Choose a team to release this case to")
    }

    "redirect to confirmation when user has right permissions" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(casesService.releaseCase(any[Case], any[Queue], any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(
        controller(caseWithStatusNEW, Set(Permission.RELEASE_CASE))
          .releaseCaseToQueue("reference")(requestWithQueue())
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/release/confirmation")
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result =
        await(controller(caseWithStatusNEW, Set.empty).releaseCaseToQueue("reference")(requestWithQueue()))

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "View Confirm page for a release case" should {

    val caseWithQueue    = caseWithStatusOPEN.copy(queueId = Some("1"))
    val caseWithoutQueue = caseWithStatusOPEN.copy(queueId = None)

    "return OK and HTML content type" in {
      when(queueService.getOneById(refEq("1"))).thenReturn(successful(Some(Queue("1", "SLUG", "NAME"))))

      val result: Result =
        await(controller(caseWithQueue).confirmReleaseCase("reference")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("case has been released")
    }

    "return resource not found when the queue is not found" in {
      when(queueService.getOneById(refEq("1"))).thenReturn(successful(None))

      val result: Result =
        await(controller(caseWithQueue).confirmReleaseCase("reference")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Case Queue not found")
    }

    "return resource not found when the case have no queue assign" in {
      val result: Result =
        await(controller(caseWithoutQueue).confirmReleaseCase("reference")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Case Queue not found")
    }

    "redirect to a default page on validation error" in {
      val result: Result = await(controller(caseWithStatusNEW).confirmReleaseCase("1")(newFakeGETRequestWithCSRF()))

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some("/manage-tariff-classifications/cases/1")
    }
  }

  private def requestWithQueue(queue: String = "queue"): FakeRequest[AnyContentAsFormUrlEncoded] =
    newFakePOSTRequestWithCSRF(Map("queue" -> queue))
}
