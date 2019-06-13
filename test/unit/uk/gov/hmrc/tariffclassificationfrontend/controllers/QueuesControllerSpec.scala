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

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.ApplicationType.ApplicationType
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}

import scala.concurrent.Future

class QueuesControllerSpec extends UnitSpec with Matchers with WithFakeApplication with MockitoSugar {

  private val fakeRequest = FakeRequest()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val queuesService = mock[QueuesService]
  private val queue = Queue("0", "queue", "Queue Name")
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def controller(permission: Set[Permission]) = new QueuesController(
    new RequestActionsWithPermissions(permission), casesService, queuesService, messageApi, appConfig
  )

  "Queue" should {

    "redirect to unauthorised if no permission" in {
      val result = await(controller(Set.empty).queue("slug")(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type when Queue is found" in {
      given(casesService.getCasesByQueue(refEq(queue), refEq(NoPagination()),any[Seq[ApplicationType]])(any[HeaderCarrier])).willReturn(Future.successful(Paged.empty[Case]))
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(queuesService.getOneBySlug("slug")).willReturn(Future.successful(Some(queue)))
      given(queuesService.getAll).willReturn(Future.successful(Seq(queue)))

      val result = await(controller(Set(Permission.VIEW_QUEUE_CASES)).queue("slug")(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Queue Name")
      session(result).get(SessionKeys.backToQueuesLinkLabel) shouldBe Some("Queue Name cases")
      session(result).get(SessionKeys.backToQueuesLinkUrl) shouldBe Some("/tariff-classification/queues/queue")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe None
      session(result).get(SessionKeys.backToSearchResultsLinkUrl) shouldBe None
    }

    "return 200 OK and HTML content type when Queue is found with specific case type specified" in {
      given(casesService.getCasesByQueue(refEq(queue), refEq(NoPagination()),refEq(Seq(ApplicationType.LIABILITY_ORDER)))(any[HeaderCarrier])).willReturn(Future.successful(Paged.empty[Case]))
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(queuesService.getOneBySlug("slug")).willReturn(Future.successful(Some(queue)))
      given(queuesService.getAll).willReturn(Future.successful(Seq(queue)))

      val result = await(controller(Set(Permission.VIEW_QUEUE_CASES)).queue("slug",Some("LIABILITY_ORDER"))(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Queue Name")
      session(result).get(SessionKeys.backToQueuesLinkUrl) shouldBe Some("/tariff-classification/queues/queue?caseType=LIABILITY_ORDER")
    }

    "return 200 OK and HTML content type when Queue is not found" in {
      given(casesService.getCasesByQueue(refEq(queue), refEq(NoPagination()),any[Seq[ApplicationType]])(any[HeaderCarrier])).willReturn(Future.successful(Paged.empty[Case]))
      given(queuesService.getOneBySlug("slug")).willReturn(Future.successful(None))
      given(queuesService.getAll).willReturn(Future.successful(Seq(queue)))

      val result = await(controller(Set(Permission.VIEW_QUEUE_CASES)).queue("slug")(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Resource not found")
    }

  }

}
