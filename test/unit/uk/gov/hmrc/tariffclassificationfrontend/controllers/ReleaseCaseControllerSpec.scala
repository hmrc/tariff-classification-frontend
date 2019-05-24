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
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.successful

class ReleaseCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val queueService = mock[QueuesService]
  private val queue = mock[Queue]
  private val operator = mock[Operator]

  private val caseWithStatusNEW = Cases.btiCaseExample.copy(status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(requestCase: Case) = new ReleaseCaseController(
    new SuccessfulRequestActions(operator, c = requestCase), casesService, queueService, messageApi, appConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new ReleaseCaseController(
    new RequestActionsWithPermissions(permission, c = requestCase), casesService, queueService, messageApi, appConfig
  )

  "Release Case" should {

    "return OK and HTML content type" in {
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))

      val result: Result = await(controller(caseWithStatusNEW).releaseCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Release this case for classification")
    }

    "redirect to Application Details for non NEW statuses" in {
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))

      val result: Result = await(controller(caseWithStatusOPEN).releaseCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/application")
    }

    "return OK when user has right permissions" in {
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))

      val result: Result = await(controller(caseWithStatusNEW, Set(Permission.RELEASE_CASE))
        .releaseCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusNEW, Set.empty).releaseCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "Release Case To Queue" should {

    "return OK and HTML content type" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(casesService.releaseCase(refEq(caseWithStatusNEW), any[Queue], refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(controller(caseWithStatusNEW).releaseCaseToQueue("reference")(requestWithQueue("queue")))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/release/confirmation")
    }

    "redirect back to case on Form Error" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(queueService.getNonGateway).thenReturn(successful(Seq.empty))
      when(casesService.releaseCase(refEq(caseWithStatusNEW), any[Queue], refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(controller(caseWithStatusNEW).releaseCaseToQueue("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Release this case for classification")
    }

    "redirect to Application Details for non NEW statuses" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))

      val result: Result = await(controller(caseWithStatusOPEN).releaseCaseToQueue(caseWithStatusOPEN.reference)(requestWithQueue("queue")))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/1/application")
    }

    "return OK when user has right permissions" in {
      when(queueService.getOneBySlug("queue")).thenReturn(successful(Some(queue)))
      when(casesService.releaseCase(any[Case], any[Queue], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusOPEN))

      val result: Result = await(controller(caseWithStatusNEW, Set(Permission.RELEASE_CASE)).releaseCaseToQueue("reference")(requestWithQueue("queue")))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/tariff-classification/cases/reference/release/confirmation")
    }


    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusNEW, Set.empty).releaseCaseToQueue("reference")(requestWithQueue("queue")))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "View Confirm page for a release case" should {

    val caseWithQueue = caseWithStatusOPEN.copy(queueId = Some("1"))
    val caseWithoutQueue = caseWithStatusOPEN.copy(queueId = None)

    "return OK and HTML content type" in {
      when(queueService.getOneById(refEq("1"))).thenReturn(successful(Some(Queue("1", "SLUG", "NAME"))))

      val result: Result = await(controller(caseWithQueue).confirmReleaseCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("This case has been released")
    }

    "return resource not found when the queue is not found" in {
      when(queueService.getOneById(refEq("1"))).thenReturn(successful(None))

      val result: Result = await(controller(caseWithQueue).confirmReleaseCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Case Queue not found")
    }

    "return resource not found when the case have no queue assign" in {
      val result: Result = await(controller(caseWithoutQueue).confirmReleaseCase("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Case Queue not found")
    }

    "redirect to a default page if the status is not right" in {
      val result: Result = await(controller(caseWithStatusNEW).confirmReleaseCase("1")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some("/tariff-classification/cases/1/application")
    }


  }

  private def requestWithQueue(queue: String): FakeRequest[AnyContentAsFormUrlEncoded] = {
    newFakePOSTRequestWithCSRF(fakeApplication, Map("queue" -> queue))
  }

}
