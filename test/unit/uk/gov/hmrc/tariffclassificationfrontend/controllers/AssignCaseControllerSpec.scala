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
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import play.api.http.{MimeTypes, Status}
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.mvc.Result
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases._

import scala.concurrent.Future.successful

class AssignCaseControllerSpec extends WordSpec with Matchers with UnitSpec
  with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()

  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = Operator("id")

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def controller(requestCase: Case) = new AssignCaseController(
    new SuccessfulRequestActions(operator, c = requestCase), casesService, messageApi, appConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  "Assign Case" should {

    "return OK and HTML content type" in {
      val aCaseWithQueue = aCase(withQueue("1"))

      val result: Result = await(controller(aCaseWithQueue).get("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("assign_case-heading")
    }

    "redirect to Trader Details for cases without a queue" in {
      val aCaseWithoutQueue = aCase(withoutQueue())

      val result: Result = await(controller(aCaseWithoutQueue).get("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(routes.CaseController.trader("reference").url)
    }

    "redirect to Trader Details for cases assigned to self" in {
      val aCaseAssignedToSelf = aCase(withQueue("1"), withAssignee(Some(operator)))

      val result: Result = await(controller(aCaseAssignedToSelf).get("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(routes.CaseController.trader("reference").url)
    }

  }

  "Confirm Assign Case" should {

    "return OK and HTML content type" in {
      val aCaseWithQueue = aCase(withQueue("1"))
      when(casesService.assignCase(refEq(aCaseWithQueue), refEq(operator))(any[HeaderCarrier])).thenReturn(successful(aCaseWithQueue))

      val result: Result = await(controller(aCaseWithQueue).post("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(routes.CaseController.trader("reference").url)
    }

    "redirect to Trader Details for cases in a queue" in {
      val aCaseWithoutQueue = aCase(withoutQueue())

      val result: Result = await(controller(aCaseWithoutQueue).post("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(routes.CaseController.trader("reference").url)
    }

    "redirect to Trader Details for cases assigned to self" in {
      val aCaseAssignedToSelf = aCase(withQueue("1"), withAssignee(Some(operator)))

      val result: Result = await(controller(aCaseAssignedToSelf).post("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(routes.CaseController.trader("reference").url)
    }

    "redirect to Assign for cases already assigned" in {
      val aCaseAssignedToSelf = aCase(withQueue("1"), withAssignee(Some(Operator("other-id"))))

      val result: Result = await(controller(aCaseAssignedToSelf).post("reference")(newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result) shouldBe None
      locationOf(result) shouldBe Some(routes.AssignCaseController.get("reference").url)
    }

  }

}
