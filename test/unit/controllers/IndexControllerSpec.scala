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

import akka.stream.Materializer
import org.scalatest.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.{BodyParsers, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import config.AppConfig
import models.Role.Role
import models._

class IndexControllerSpec extends ControllerBaseSpec {

  private def controller(role: Role) = new IndexController(
    new SuccessfulAuthenticatedAction(defaultPlayBodyParsers, Operator(id = "0", role = role)), mcc, realAppConfig
  )

  "GET" should {

    "Load Homepage for Read Only role" in {
      val result = await(controller(Role.READ_ONLY).get()(fakeRequest))

      status(result) shouldBe OK
      bodyOf(result) should include("read_only_home-heading")
      session(result).get(SessionKeys.backToQueuesLinkLabel) shouldBe Some("")
      session(result).get(SessionKeys.backToQueuesLinkUrl) shouldBe Some("/manage-tariff-classifications")
      session(result).get(SessionKeys.backToSearchResultsLinkLabel) shouldBe None
      session(result).get(SessionKeys.backToSearchResultsLinkUrl) shouldBe None
    }

    "Redirect for Officer role" in {
      val result = await(controller(Role.CLASSIFICATION_OFFICER).get()(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MyCasesController.myCases().url)
    }

    "Redirect for Manager role" in {
      val result = await(controller(Role.CLASSIFICATION_MANAGER).get()(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.MyCasesController.myCases().url)
    }
  }

}
