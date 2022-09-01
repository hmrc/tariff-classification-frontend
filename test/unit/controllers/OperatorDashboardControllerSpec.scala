/*
 * Copyright 2022 HM Revenue & Customs
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
import models.request._
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.Request
import play.api.test.Helpers._
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OperatorDashboardControllerSpec extends ControllerBaseSpec {

  implicit val appConfig = realAppConfig

  implicit def authenticatedRequest[A](
    implicit
    operator: Operator,
    request: Request[A]
  ): AuthenticatedRequest[A] =
    AuthenticatedRequest(operator, request)

  val operator_dashboard_classification = injector.instanceOf[views.html.operator_dashboard_classification]

  val casesCounted: Map[(Option[String], ApplicationType), Int] = Map(
    (None, ApplicationType.ATAR)      -> 2,
    (None, ApplicationType.LIABILITY) -> 3
  )

  private val casesService = mock[CasesService]

  override def beforeEach(): Unit =
    when(casesService.countCasesByQueue(any[HeaderCarrier])) thenReturn casesCounted

  given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
    .willReturn(Future.successful(Paged.empty[Case]))

  private def controller(permission: Set[Permission]) = new OperatorDashboardController(
    new RequestActionsWithPermissions(playBodyParsers, permission),
    casesService,
    mcc,
    operator_dashboard_classification,
    realAppConfig
  )

  "OperatorDashboardClassificationView Controller" must {

    "return OK and the correct view for a GET" in {

      val result = controller(Set(Permission.VIEW_MY_CASES)).onPageLoad()(fakeRequest)

      status(result) shouldBe OK
    }

    "return unauthorised when user does not hold the required permissions" in {

      val result = controller(Set(Permission.VIEW_CASES)).onPageLoad()(fakeRequest)

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

    "return 200 OK for the correct number of refer by me cases" in {

      given(casesService.getCasesByAssignee(any[Operator], any[Pagination])(any[HeaderCarrier]))
        .willReturn(
          Future.successful(
            Paged(
              Seq(
                Cases.btiCaseExample.copy(status = CaseStatus.REFERRED),
                Cases.btiCaseExample.copy(status = CaseStatus.SUPPRESSED)
              )
            )
          )
        )

      val result = controller(Set(Permission.VIEW_MY_CASES)).onPageLoad()(fakeRequest)

      status(result) shouldBe OK

    }
  }

}
