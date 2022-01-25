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

import models.Role.Role
import models._
import play.api.http.Status
import play.api.test.Helpers._
import views.html.read_only_home

import scala.concurrent.ExecutionContext.Implicits.global

class IndexControllerSpec extends ControllerBaseSpec {

  private val readOnlyHome = injector.instanceOf[read_only_home]

  private def controller(role: Role) = new IndexController(
    new SuccessfulAuthenticatedAction(playBodyParsers, Operator(id = "0", role = role)),
    mcc,
    readOnlyHome,
    realAppConfig
  )

  "GET" should {

    "Load Homepage for Read Only role" in {
      val result = await(controller(Role.READ_ONLY).get()(fakeRequest))

      status(result)                                                shouldBe OK
      bodyOf(result)                                                should include("read_only_home-heading")
      session(result).get(SessionKeys.backToQueuesLinkLabel)        shouldBe Some("")
      session(result).get(SessionKeys.backToQueuesLinkUrl)          shouldBe Some("/manage-tariff-classifications")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe None
      session(result).get(SessionKeys.backToSearchResultsLinkUrl)   shouldBe None
    }

    "Redirect for Officer role" in {
      val result = await(controller(Role.CLASSIFICATION_OFFICER).get()(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.OperatorDashboardController.onPageLoad.url)
    }

    "Redirect for Manager role" in {
      val result = await(controller(Role.CLASSIFICATION_MANAGER).get()(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.OperatorDashboardController.onPageLoad.url)
    }
  }

}
