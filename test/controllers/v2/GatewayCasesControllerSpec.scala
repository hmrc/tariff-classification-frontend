/*
 * Copyright 2024 HM Revenue & Customs
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
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.v2.gateway_cases_view

class GatewayCasesControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private lazy val gateway_cases_view = injector.instanceOf[gateway_cases_view]

  private val casesService = mock[CasesService]

  private def controller(permission: Set[Permission]): GatewayCasesController =
    new GatewayCasesController(
      new RequestActionsWithPermissions(playBodyParsers, permissions = permission),
      casesService,
      mcc,
      gateway_cases_view,
      realAppConfig
    )

  "GatewayCasesController" should {

    "return 200 and the correct content when no tab has ben specified" in {
      given(casesService.getCasesByQueue(any[Queue], any[Pagination], any[Set[ApplicationType]])(any[HeaderCarrier]))
        .willReturn(Paged(Seq(Cases.btiNewCase, Cases.aCase(), Cases.correspondenceCaseExample)))

      val result = await(controller(Set(Permission.VIEW_QUEUE_CASES))).displayGatewayCases()(fakeRequest)

      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      status(result)      shouldBe Status.OK
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set())).displayGatewayCases()(fakeRequest)

      status(result) shouldBe Status.SEE_OTHER

      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

}
