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

package controllers.v2

import controllers.{ControllerBaseSpec, RequestActionsWithPermissions}
import models.viewmodels.{AssignedToMeTab, CompletedByMeTab, ReferredByMeTab}
import models.{Operator, Paged, Pagination, Permission}
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.v2.common_cases_view

import scala.concurrent.ExecutionContext.Implicits.global

class MyCasesControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private lazy val common_cases_view = injector.instanceOf[common_cases_view]

  private val casesService = mock[CasesService]

  private def controller(permission: Set[Permission]): MyCasesController = {
    new MyCasesController(
      new RequestActionsWithPermissions(playBodyParsers, permissions = permission),
      casesService,
      mcc,
      common_cases_view,
      realAppConfig
    )
  }

  "MyCasesController" should {

    "return 200 and the correct content when no tab has ben specified" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier])).
        willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      val result = await(controller(Set(Permission.VIEW_MY_CASES))).displayMyCases()(fakeRequest)

      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      status(result) shouldBe Status.OK
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).displayMyCases()(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER

      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)
    }

    "return 200 OK with the correct subNavigation tab for AssignedToMe" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier])).
        willReturn(Paged(Seq(Cases.aCase(), Cases.aLiabilityCase())))

      val result = await(controller(Set(Permission.VIEW_MY_CASES)).displayMyCases(AssignedToMeTab)(fakeRequest))

      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      status(result) shouldBe Status.OK
    }

    "return 200 OK with the correct subNavigation tab for ReferredByMe" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier])).
        willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      val result = await(controller(Set(Permission.VIEW_MY_CASES)).displayMyCases(ReferredByMeTab)(fakeRequest))

      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      status(result) shouldBe Status.OK
    }

    "return 200 OK with the correct subNavigation tab for CompletedByMe" in {
      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier])).
        willReturn(Paged(Seq(Cases.aCase(), Cases.aCase())))

      val result = await(controller(Set(Permission.VIEW_MY_CASES)).displayMyCases(CompletedByMeTab)(fakeRequest))

      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      status(result) shouldBe Status.OK
    }
  }

}
