/*
 * Copyright 2025 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.{confirm_reassign_case, reassign_queue_case, resource_not_found}

import scala.concurrent.Future.successful

class ReassignCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val queueService = mock[QueuesService]
  private val queue        = mock[Queue]
  private val operator     = Operator(id = "id")

  private val reassignQueueCase   = injector.instanceOf[reassign_queue_case]
  private val confirmReassignCase = injector.instanceOf[confirm_reassign_case]
  private val resourceNotFound    = injector.instanceOf[resource_not_found]

  private val caseWithStatusNEW = Cases.caseQueueExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.caseQueueExample
    .copy(reference = "reference", status = CaseStatus.OPEN, assignee = Some(Operator("12345", Some("Operator Test"))))

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(requestCase: Case): ReassignCaseController = new ReassignCaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    queueService,
    mcc,
    reassignQueueCase,
    confirmReassignCase,
    resourceNotFound,
    realAppConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new ReassignCaseController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      queueService,
      mcc,
      reassignQueueCase,
      confirmReassignCase,
      resourceNotFound,
      realAppConfig
    )

  "Reassign Case" should {

    "return OK and HTML content type" in {
      when(queueService.getAllForCaseType(any[ApplicationType])).thenReturn(successful(List.empty))
      when(queueService.getOneById(any[String])).thenReturn(successful(None))

      val result: Result =
        await(controller(caseWithStatusOPEN).showAvailableQueues("reference", "origin")(newFakeGETRequestWithCSRF()))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)          should include("Choose a team to move this case to")
    }

    "return OK when user has right permissions" in {
      when(queueService.getAllForCaseType(any[ApplicationType])).thenReturn(successful(List.empty))
      when(queueService.getOneById(any[String])).thenReturn(successful(None))

      val result: Result = await(
        controller(caseWithStatusOPEN, Set(Permission.MOVE_CASE_BACK_TO_QUEUE))
          .showAvailableQueues("reference", "origin")(newFakeGETRequestWithCSRF())
      )

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .showAvailableQueues("reference", "origin")(newFakeGETRequestWithCSRF())
      )

      status(result)             shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Reassign Case To Queue" should {

    "return OK and HTML content type" in {

      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getOneById("1")).thenReturn(successful(Some(queue)))
      when(queue.name).thenReturn("SOME_QUEUE")
      when(casesService.reassignCase(refEq(caseWithStatusOPEN), any[Queue], any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(caseWithStatusOPEN))

      val result: Result =
        await(controller(caseWithStatusOPEN).reassignCase("reference", "origin")(requestWithQueue()))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(
        "/manage-tariff-classifications/cases/reference/reassign-case/confirmation?origin=origin"
      )
    }

    "show error message when no option is selected" in {

      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getAllForCaseType(any[ApplicationType])).thenReturn(successful(List.empty))
      when(queue.name).thenReturn("SOME_QUEUE")
      when(queueService.getOneById(any[String])).thenReturn(successful(None))
      when(casesService.reassignCase(refEq(caseWithStatusOPEN), any[Queue], refEq(operator))(any[HeaderCarrier]))
        .thenReturn(successful(caseWithStatusOPEN))

      val result: Result =
        await(controller(caseWithStatusOPEN).reassignCase("reference", "origin")(newFakePOSTRequestWithCSRF()))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)          should include("Select a team")
    }

    "return Not Found and HTML content type on missing Queue" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(None))

      val result: Result =
        await(controller(caseWithStatusNEW).reassignCase("reference", "origin")(requestWithQueue()))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)          should include("Queue queue not found")
    }

    "return OK when user has right permissions" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getOneById("1")).thenReturn(successful(Some(queue)))
      when(queue.name).thenReturn("SOME_QUEUE")
      when(casesService.reassignCase(any[Case], any[Queue], any[Operator])(any[HeaderCarrier]))
        .thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(
        controller(caseWithStatusOPEN, Set(Permission.MOVE_CASE_BACK_TO_QUEUE))
          .reassignCase("reference", "origin")(requestWithQueue())
      )

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(
        "/manage-tariff-classifications/cases/reference/reassign-case/confirmation?origin=origin"
      )
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .reassignCase("reference", "origin")(requestWithQueue())
      )

      status(result)             shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "View Confirm page for a reassign case queue" should {

    val caseWithQueue    = caseWithStatusOPEN.copy(queueId = Some("1"))
    val caseWithoutQueue = caseWithStatusOPEN.copy(queueId = None)

    "return OK and HTML content type" in {
      when(queueService.getOneById(refEq("1"))).thenReturn(successful(Some(Queue("1", "SLUG", "NAME"))))
      val result: Result =
        await(controller(caseWithQueue).confirmReassignCase("reference", "origin")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
      bodyOf(result)   should include("case has been moved")
    }

    "return resource not found when the queue is not found" in {
      when(queueService.getOneById(refEq("1"))).thenReturn(successful(None))
      val result: Result =
        await(controller(caseWithQueue).confirmReassignCase("reference", "origin")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
      bodyOf(result)   should include("Case Queue not found")
    }

    "return resource not found when the case have no queue assign" in {
      val result: Result =
        await(controller(caseWithoutQueue).confirmReassignCase("reference", "origin")(newFakeGETRequestWithCSRF()))

      status(result) shouldBe Status.OK
      bodyOf(result)   should include("Case Queue not found")
    }

    "redirect to a default page if the status is not right" in {
      val result: Result =
        await(controller(caseWithStatusNEW).confirmReassignCase("reference", "origin")(newFakeGETRequestWithCSRF()))

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference")
    }
  }

  private def requestWithQueue(queue: String = "queue"): FakeRequest[AnyContentAsFormUrlEncoded] =
    newFakePOSTRequestWithCSRF(Map("queue" -> queue))

}
