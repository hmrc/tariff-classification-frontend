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
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus.AppealStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealType.AppealType
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.tariffclassificationfrontend.utils.Cases._

import scala.concurrent.Future

class AppealCaseControllerSpec extends UnitSpec with Matchers
  with WithFakeApplication with MockitoSugar with ControllerCommons with BeforeAndAfterEach {

  private val fakeRequest = FakeRequest()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private def controller(requestCase: Case) = new AppealCaseController(
    new SuccessfulRequestActions(operator, c = requestCase), casesService, messageApi, appConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new AppealCaseController(
    new RequestActionsWithPermissions(permission, c = requestCase), casesService, messageApi, appConfig
  )

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  "Case Appeal Details" should {
    "Return 200" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val result = await(controller(c).appealDetails(c.reference)(fakeRequest))

          status(result) shouldBe Status.OK
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
          contentAsString(result) should include("appeal-heading")
        }
      }

    }

    "Redirect" when {
      "Case has no decision" in {
        val c = aCase(withStatus(CaseStatus.COMPLETED), withoutDecision())

        val result = await(controller(c).appealDetails(c.reference)(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)

      }

      for (s <- CaseStatus.values.filter(s => s != CaseStatus.CANCELLED && s != CaseStatus.COMPLETED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val result = await(controller(c).appealDetails(c.reference)(fakeRequest))

          status(result) shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)
        }
      }
    }
  }

  "Case Choose Type" should {
    "Return 200" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val request = newFakeGETRequestWithCSRF(fakeApplication)
          val result = await(controller(c).chooseType(c.reference)(request))

          status(result) shouldBe Status.OK
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
          contentAsString(result) should include("appeal_choose_type")
        }
      }

    }

    "Redirect" when {
      "Case has no decision" in {
        val c = aCase(withStatus(CaseStatus.COMPLETED), withoutDecision())

        val request = newFakeGETRequestWithCSRF(fakeApplication)
        val result = await(controller(c).chooseType(c.reference)(request))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)

      }

      for (s <- CaseStatus.values.filter(s => s != CaseStatus.CANCELLED && s != CaseStatus.COMPLETED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val request = newFakeGETRequestWithCSRF(fakeApplication)
          val result = await(controller(c).chooseType(c.reference)(request))

          status(result) shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)
        }
      }
    }
  }

  "Case Confirm Type" should {
    "Redirect to Next Stage" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val request = newFakePOSTRequestWithCSRF(fakeApplication, Map("type" -> AppealType.REVIEW.toString))
          val result = await(controller(c).confirmType(c.reference)(request))

          status(result) shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(routes.AppealCaseController.chooseStatus(c.reference, AppealType.REVIEW.toString).url)
        }
      }
    }

    "Render Form errors" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val request = newFakePOSTRequestWithCSRF(fakeApplication, Map())
      val result = await(controller(c).confirmType(c.reference)(request))

      status(result) shouldBe Status.OK
    }

    "Redirect" when {
      "Case has no decision" in {
        val c = aCase(withStatus(CaseStatus.COMPLETED), withoutDecision())

        val result = await(controller(c).confirmType(c.reference)(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)

      }

      for (s <- CaseStatus.values.filter(s => s != CaseStatus.CANCELLED && s != CaseStatus.COMPLETED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val result = await(controller(c).confirmType(c.reference)(fakeRequest))

          status(result) shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)
        }
      }
    }
  }

  "Case Choose Status" should {
    "Return 200" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val request = newFakeGETRequestWithCSRF(fakeApplication)
          val result = await(controller(c).chooseStatus(c.reference, AppealType.REVIEW.toString)(request))

          status(result) shouldBe Status.OK
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
          contentAsString(result) should include("appeal_choose_status")
        }
      }

    }

    "Redirect" when {
      "Case has no decision" in {
        val c = aCase(withStatus(CaseStatus.COMPLETED), withoutDecision())

        val request = newFakeGETRequestWithCSRF(fakeApplication)
        val result = await(controller(c).chooseStatus(c.reference, AppealType.REVIEW.toString)(request))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)

      }

      for (s <- CaseStatus.values.filter(s => s != CaseStatus.CANCELLED && s != CaseStatus.COMPLETED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val request = newFakeGETRequestWithCSRF(fakeApplication)
          val result = await(controller(c).chooseStatus(c.reference, AppealType.REVIEW.toString)(request))

          status(result) shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)
        }
      }
    }
  }

  "Case Confirm Status" should {
    "Redirect to Appeal View" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())
          given(casesService.addAppeal(any[Case], any[AppealType], any[AppealStatus], any[Operator])(any[HeaderCarrier])) willReturn Future.successful(c)

          val request = newFakePOSTRequestWithCSRF(fakeApplication, Map("status" -> AppealStatus.ALLOWED.toString))
          val result = await(controller(c).confirmStatus(c.reference, AppealType.REVIEW.toString)(request))

          status(result) shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(routes.AppealCaseController.appealDetails(c.reference).url)

          verify(casesService).addAppeal(refEq(c), refEq(AppealType.REVIEW), refEq(AppealStatus.ALLOWED), refEq(operator))(any[HeaderCarrier])
        }
      }
    }

    "Render Form errors" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val request = newFakePOSTRequestWithCSRF(fakeApplication, Map())
      val result = await(controller(c).confirmStatus(c.reference, AppealType.REVIEW.toString)(request))

      status(result) shouldBe Status.OK
    }

    "Redirect" when {
      "Case has no decision" in {
        val c = aCase(withStatus(CaseStatus.COMPLETED), withoutDecision())

        val result = await(controller(c).confirmStatus(c.reference, AppealType.REVIEW.toString)(fakeRequest))

        status(result) shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)

      }

      for (s <- CaseStatus.values.filter(s => s != CaseStatus.CANCELLED && s != CaseStatus.COMPLETED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val result = await(controller(c).confirmStatus(c.reference, AppealType.REVIEW.toString)(fakeRequest))

          status(result) shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(routes.CaseController.trader(c.reference).url)
        }
      }
    }
  }

}
