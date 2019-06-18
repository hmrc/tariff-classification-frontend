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
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, _}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.successful

class ReassignCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val queueService = mock[QueuesService]
  private val queue = mock[Queue]
  private val operator = mock[Operator]

  private val caseWithStatusNEW = Cases.caseQueueExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.caseQueueExample.copy(reference = "reference", status = CaseStatus.OPEN,
    assignee = Some(Operator("12345", Some("Operator Test"))))

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(requestCase: Case): ReassignCaseController = new ReassignCaseController(
    new SuccessfulRequestActions(operator, c = requestCase), casesService, queueService, messageApi, appConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new ReassignCaseController(
    new RequestActionsWithPermissions(permission, c = requestCase), casesService, queueService, messageApi, appConfig)


  "Reassign Case" should {

    "return OK and HTML content type" in {
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))
      when(queueService.getOneById(any())).thenReturn(successful(None))

      val result: Result = await(controller(caseWithStatusOPEN).showAvailableQueues("reference", "origin")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Move this case back to a queue")
    }

    "redirect to Application Details for non OPEN, REFERRED or SUSPENDED statuses" in {

      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))

      val result: Result = await(controller(caseWithStatusNEW).showAvailableQueues("reference", "origin")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return OK when user has right permissions" in {
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))
      when(queueService.getOneById(any())).thenReturn(successful(None))

      val result: Result = await(controller(caseWithStatusOPEN, Set(Permission.MOVE_CASE_BACK_TO_QUEUE))
        .showAvailableQueues("reference", "origin")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusOPEN, Set.empty)
        .showAvailableQueues("reference", "origin")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Reassign Case To Queue" should {

    "return OK and HTML content type" in {

      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getOneById("1")).thenReturn(successful(Some(queue)))
      when(queue.name).thenReturn("SOME_QUEUE")
      when(casesService.reassignCase(refEq(caseWithStatusOPEN), any[Queue], refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(controller(caseWithStatusOPEN).reassignCase("reference", "origin")(requestWithQueue("queue")))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/reassign-case/confirmation?origin=origin")
    }

    "show error message when no option is selected" in {

      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))
      when(queue.name).thenReturn("SOME_QUEUE")
      when(queueService.getOneById(any())).thenReturn(successful(None))
      when(casesService.reassignCase(refEq(caseWithStatusOPEN), any[Queue], refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(controller(caseWithStatusOPEN).reassignCase("reference", "origin")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Select a queue to release this case")
    }

    "redirect to Application Details for non  OPEN, REFERRED or SUSPENDED statuses" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))

      val result: Result = await(controller(caseWithStatusNEW).reassignCase("reference", "origin")(requestWithQueue("queue")))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return Not Found and HTML content type on missing Queue" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(None))

      val result: Result = await(controller(caseWithStatusNEW).reassignCase("reference", "origin")(requestWithQueue("queue")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Queue queue not found")
    }

    "return OK when user has right permissions" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getOneById("1")).thenReturn(successful(Some(queue)))
      when(queue.name).thenReturn("SOME_QUEUE")
      when(casesService.reassignCase(any[Case], any[Queue], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(controller(caseWithStatusOPEN, Set(Permission.MOVE_CASE_BACK_TO_QUEUE))
        .reassignCase("reference", "origin")(requestWithQueue("queue")))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/reassign-case/confirmation?origin=origin")
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusOPEN, Set.empty)
        .reassignCase("reference", "origin")(requestWithQueue("queue")))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "View Confirm page for a reassign case queue" should {

    val caseWithQueue = caseWithStatusOPEN.copy(queueId = Some("1"))
    val caseWithoutQueue = caseWithStatusOPEN.copy(queueId = None)

    "return OK and HTML content type" in {
      when(queueService.getOneById(refEq("1"))).thenReturn(successful(Some(Queue("1", "SLUG", "NAME"))))
      val result: Result = await(controller(caseWithQueue).confirmReassignCase("reference", "origin")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("This case has been moved to the NAME queue")
    }

    "return resource not found when the queue is not found" in {
      when(queueService.getOneById(refEq("1"))).thenReturn(successful(None))
      val result: Result = await(controller(caseWithQueue).confirmReassignCase("reference", "origin")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Case Queue not found")
    }

    "return resource not found when the case have no queue assign" in {
      val result: Result = await(controller(caseWithoutQueue).confirmReassignCase("reference", "origin")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Case Queue not found")
    }

    "redirect to a default page if the status is not right" in {
      val result: Result = await(controller(caseWithStatusNEW).confirmReassignCase("reference", "origin")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }
  }

  private def requestWithQueue(queue: String): FakeRequest[AnyContentAsFormUrlEncoded] = {
    newFakePOSTRequestWithCSRF(fakeApplication, Map("queue" -> queue))
  }

}
