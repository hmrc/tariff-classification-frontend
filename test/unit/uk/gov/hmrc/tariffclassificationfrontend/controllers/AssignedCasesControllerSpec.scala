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
import uk.gov.hmrc.tariffclassificationfrontend.models
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.tariffclassificationfrontend.utils.Cases.btiCaseExample

import scala.concurrent.Future

class AssignedCasesControllerSpec extends UnitSpec with Matchers with WithFakeApplication with MockitoSugar {

  private val fakeRequest = FakeRequest()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val queuesService = mock[QueuesService]
  private val queue = Queue("0", "queue", "Queue Name")
  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val assignedCase = btiCaseExample.copy(assignee = Some(Operator("1", Some("Test User"))))

  private val requiredPermissions: Set[models.Permission.Value] = Set(Permission.VIEW_ASSIGNED_CASES)
  private val noPermissions: Set[models.Permission.Value] = Set.empty

  private def controller(permission: Set[Permission]) = new AssignedCasesController(
    new RequestActionsWithPermissions(permission), casesService, queuesService, messageApi, appConfig
  )

  "Assigned Cases" should {
    "redirect to unauthorised if not a manager" in {
      val result = await(controller(noPermissions).assignedCases()(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type when no cases returned" in {
      given(casesService.getAssignedCases(refEq(NoPagination()))(any[HeaderCarrier])).willReturn(Future.successful(Paged.empty[Case]))
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(queuesService.getAll).willReturn(Future.successful(Seq(queue)))

      val result = await(controller(requiredPermissions).assignedCases()(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Assigned cases")
      session(result).get(SessionKeys.backToQueuesLinkLabel) shouldBe Some("Assigned cases")
      session(result).get(SessionKeys.backToQueuesLinkUrl) shouldBe Some("/tariff-classification/queues/assigned")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe None
      session(result).get(SessionKeys.backToSearchResultsLinkUrl) shouldBe None
    }
  }

  "Assigned Cases by Operator" should {

    "redirect to unauthorised if not a manager" in {
      val result = await(controller(noPermissions).assignedCasesFor("1",0)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type when no cases returned" in {
      given(casesService.getAssignedCases(refEq(NoPagination()))(any[HeaderCarrier])).willReturn(Future.successful(Paged.empty[Case]))
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(queuesService.getAll).willReturn(Future.successful(Seq(queue)))

      val result = await(controller(requiredPermissions).assignedCasesFor("1",0)(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Assigned cases")
      session(result).get(SessionKeys.backToQueuesLinkLabel) shouldBe Some("Assigned cases")
      session(result).get(SessionKeys.backToQueuesLinkUrl) shouldBe Some("/tariff-classification/queues/assigned/1")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe None
      session(result).get(SessionKeys.backToSearchResultsLinkUrl) shouldBe None
    }

    "return 200 OK and HTML content type when case is returned" in {
      given(casesService.getAssignedCases(refEq(NoPagination()))(any[HeaderCarrier])).willReturn(Future.successful(Paged(Seq(assignedCase))))
      given(queuesService.getAll).willReturn(Future.successful(Seq(queue)))

      val result = await(controller(requiredPermissions).assignedCasesFor("1",0)(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Assigned to Test User")
      session(result).get(SessionKeys.backToQueuesLinkUrl) shouldBe Some("/tariff-classification/queues/assigned/1")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe None
      session(result).get(SessionKeys.backToSearchResultsLinkUrl) shouldBe None
    }
  }

}
