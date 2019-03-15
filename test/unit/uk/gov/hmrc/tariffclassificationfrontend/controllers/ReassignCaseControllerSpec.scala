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
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Operator, Queue}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.{failed, successful}

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

  private val caseWithStatusNEW = Cases.caseQueueExample.copy(status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.caseQueueExample.copy(status = CaseStatus.OPEN, assignee = Some(Operator("12345", Some("Operator Test"))))

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val controller = new ReassignCaseController(
    new SuccessfulAuthenticatedAction(operator), casesService, queueService, messageApi, appConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  "Reassign Case" should {

    "return OK and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusOPEN)))
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))
      when(queueService.getOneById(any())).thenReturn(successful(None))

      val result: Result = await(controller.showAvailableQueues("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Move this case back to a queue")
    }

    "redirect to Application Details for non OPEN, REFERRED or SUSPENDED statuses" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusNEW)))
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))

      val result: Result = await(controller.showAvailableQueues("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return Not Found and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(None))
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))

      val result: Result = await(controller.showAvailableQueues("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("We could not find a Case with reference")
    }

  }

  "Reassign Case To Queue" should {

    "return OK and HTML content type" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusOPEN)))
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getOneById("1")).thenReturn(successful(Some(queue)))
      when(queue.name).thenReturn(("SOME_QUEUE"))
      when(casesService.reassignCase(refEq(caseWithStatusOPEN), any[Queue], refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(controller.reassignCase("reference")(requestWithQueue("queue")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This case has been moved to the SOME_QUEUE queue")
    }

    "show error message when no option is selected" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusOPEN)))
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))
      when(queue.name).thenReturn(("SOME_QUEUE"))
      when(queueService.getOneById(any())).thenReturn(successful(None))
      when(casesService.reassignCase(refEq(caseWithStatusOPEN), any[Queue], refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(controller.reassignCase("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This field is required")
    }

    "redirect to Application Details for non  OPEN, REFERRED or SUSPENDED statuses" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusNEW)))
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))

      val result: Result= await(controller.reassignCase("reference")(requestWithQueue("queue")))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return Not Found and HTML content type on missing Case" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(None))
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))

      val result: Result = await(controller.reassignCase("reference")(requestWithQueue("queue")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("We could not find a Case with reference")
    }

    "return Not Found and HTML content type on missing Queue" in {
      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(successful(Some(caseWithStatusNEW)))
      when(queueService.getOneBySlug("queue")).thenReturn(successful(None))

      val result: Result = await(controller.reassignCase("reference")(requestWithQueue("queue")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Queue queue not found")
    }

    "propagate the error in case the CaseService fails to release the case" in {
      val error = new IllegalStateException("expected error")

      when(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).thenReturn(failed(error))
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))

      val caught = intercept[error.type] {
        await(controller.reassignCase("reference")(requestWithQueue("queue")))
      }
      caught shouldBe error
    }
  }

  private def requestWithQueue(queue : String) : FakeRequest[AnyContentAsFormUrlEncoded] = {
    newFakePOSTRequestWithCSRF(fakeApplication, Map("queue" -> queue))
  }

}
