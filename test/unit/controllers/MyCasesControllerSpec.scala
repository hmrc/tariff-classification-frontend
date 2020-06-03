/*
 * Copyright 2020 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class MyCasesControllerSpec extends ControllerBaseSpec {

  private val casesService = mock[CasesService]
  private val queuesService = mock[QueuesService]
  private val queue = Queue("0", "queue", "Queue Name")

  private def controller(permission: Set[Permission]) = new MyCasesController(
    new RequestActionsWithPermissions(defaultPlayBodyParsers, permission), casesService, queuesService, mcc, realAppConfig
  )

  "My Cases" should {

    "redirect to unauthorised if no permission" in {
      val result = await(controller(Set.empty).myCases()(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier])).willReturn(Future.successful(Paged.empty[Case]))
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(queuesService.getAll).willReturn(Future.successful(Seq(queue)))

      val result = await(controller(Set(Permission.VIEW_MY_CASES)).myCases()(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      session(result).get(SessionKeys.backToQueuesLinkLabel) shouldBe Some("My cases")
      session(result).get(SessionKeys.backToQueuesLinkUrl) shouldBe Some("/manage-tariff-classifications/queues/my-cases")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe None
      session(result).get(SessionKeys.backToSearchResultsLinkUrl) shouldBe None
    }

  }

}
