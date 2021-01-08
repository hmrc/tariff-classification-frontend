/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class QueuesControllerSpec extends ControllerBaseSpec {

  private val casesService  = mock[CasesService]
  private val queuesService = mock[QueuesService]
  private val queue         = Queue("0", "queue", "Queue Name")

  private def controller(permission: Set[Permission]) = new QueuesController(
    new RequestActionsWithPermissions(playBodyParsers, permission),
    casesService,
    queuesService,
    mcc,
    realAppConfig
  )

  "Queue" should {

    "redirect to unauthorised if no permission" in {
      val result = await(controller(Set.empty).queue("slug")(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type when Queue is found" in {
      given(
        casesService.getCasesByQueue(refEq(queue), refEq(NoPagination()), any[Seq[ApplicationType]])(any[HeaderCarrier])
      ).willReturn(Future.successful(Paged.empty[Case]))
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier]))
        .willReturn(Future.successful(Map.empty[String, Int]))
      given(queuesService.getOneBySlug("slug")).willReturn(Future.successful(Some(queue)))
      given(queuesService.getAll).willReturn(Future.successful(List(queue)))

      val result = await(controller(Set(Permission.VIEW_QUEUE_CASES)).queue("slug")(fakeRequest))
      status(result)                                                shouldBe Status.OK
      contentType(result)                                           shouldBe Some("text/html")
      charset(result)                                               shouldBe Some("utf-8")
      contentAsString(result)                                       should include("Queue Name")
      session(result).get(SessionKeys.backToQueuesLinkLabel)        shouldBe Some("Queue Name cases")
      session(result).get(SessionKeys.backToQueuesLinkUrl)          shouldBe Some("/manage-tariff-classifications/queues/queue")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe None
      session(result).get(SessionKeys.backToSearchResultsLinkUrl)   shouldBe None
    }

    "return 200 OK and HTML content type when Queue is found with specific case type specified" in {
      given(
        casesService.getCasesByQueue(refEq(queue), refEq(NoPagination()), refEq(Seq(ApplicationType.LIABILITY)))(
          any[HeaderCarrier]
        )
      ).willReturn(Future.successful(Paged.empty[Case]))
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier]))
        .willReturn(Future.successful(Map.empty[String, Int]))
      given(queuesService.getOneBySlug("slug")).willReturn(Future.successful(Some(queue)))
      given(queuesService.getAll).willReturn(Future.successful(List(queue)))

      val result =
        await(controller(Set(Permission.VIEW_QUEUE_CASES)).queue("slug", Some("LIABILITY_ORDER"))(fakeRequest))
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("Queue Name")
      session(result).get(SessionKeys.backToQueuesLinkUrl) shouldBe Some(
        "/manage-tariff-classifications/queues/queue?caseType=LIABILITY_ORDER"
      )
    }

    "return 200 OK and HTML content type when Queue is not found" in {
      given(
        casesService.getCasesByQueue(refEq(queue), refEq(NoPagination()), any[Seq[ApplicationType]])(any[HeaderCarrier])
      ).willReturn(Future.successful(Paged.empty[Case]))
      given(queuesService.getOneBySlug("slug")).willReturn(Future.successful(None))
      given(queuesService.getAll).willReturn(Future.successful(List(queue)))

      val result = await(controller(Set(Permission.VIEW_QUEUE_CASES)).queue("slug")(fakeRequest))
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("Resource not found")
    }

  }

}
