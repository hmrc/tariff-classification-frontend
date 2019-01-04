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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Queue
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}

import scala.concurrent.Future

class QueuesControllerSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar {

  private val fakeRequest = FakeRequest()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val queuesService = mock[QueuesService]
  private val queue = Queue("0", "queue", "Queue Name")
  private implicit val hc = HeaderCarrier()

  private val controller = new QueuesController(new SuccessfulAuthenticatedAction, casesService, queuesService, messageApi, appConfig)

  "Queue" should {

    "return 200 OK and HTML content type when Queue is found" in {
      given(casesService.getCasesByQueue(refEq(queue))(any[HeaderCarrier])).willReturn(Future.successful(Seq.empty))
      given(queuesService.getOneBySlug("slug")).willReturn(Some(queue))
      given(queuesService.getAll).willReturn(Seq(queue))

      val result = await(controller.queue("slug")(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Queue Name")
    }

    "return 200 OK and HTML content type when Queue is not found" in {
      given(casesService.getCasesByQueue(refEq(queue))(any[HeaderCarrier])).willReturn(Future.successful(Seq.empty))
      given(queuesService.getOneBySlug("slug")).willReturn(None)
      given(queuesService.getAll).willReturn(Seq(queue))

      val result = await(controller.queue("slug")(fakeRequest))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include ("Resource not found")
    }

  }

}
