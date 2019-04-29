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
import uk.gov.hmrc.tariffclassificationfrontend.models.Role.Role
import uk.gov.hmrc.tariffclassificationfrontend.models._

class IndexControllerSpec extends UnitSpec with Matchers with WithFakeApplication with MockitoSugar {

  private val fakeRequest = FakeRequest()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def controller(role: Role) = new IndexController(new SuccessfulAuthenticatedAction(Operator(id = "0", role = role)), messageApi, appConfig)

  "GET" should {

    "Load Homepage for Read Only role" in {
      val result = await(controller(Role.READ_ONLY).get()(fakeRequest))

      status(result) shouldBe OK
      bodyOf(result) should include("read_only_home-heading")
      session(result).get(SessionKeys.backToQueuesLinkLabel) shouldBe Some("Search")
      session(result).get(SessionKeys.backToQueuesLinkUrl) shouldBe Some("/tariff-classification")
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
