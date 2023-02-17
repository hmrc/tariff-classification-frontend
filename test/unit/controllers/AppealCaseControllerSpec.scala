/*
 * Copyright 2023 HM Revenue & Customs
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

import models.AppealStatus.AppealStatus
import models.AppealType.AppealType
import models._
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._
import views.html.{appeal_change_status, appeal_choose_status, appeal_choose_type}

import scala.concurrent.Future

class AppealCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = Operator(id = "id")

  private val appealChooseStatus = injector.instanceOf[appeal_choose_status]
  private val appealChooseType   = injector.instanceOf[appeal_choose_type]
  private val appealChangeStatus = injector.instanceOf[appeal_change_status]

  private def controller(requestCase: Case) = new AppealCaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    realAppConfig,
    appealChooseStatus,
    appealChooseType,
    appealChangeStatus,
    mcc
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new AppealCaseController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    realAppConfig,
    appealChooseStatus,
    appealChooseType,
    appealChangeStatus,
    mcc
  )

  override def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  "Case Appeal Details" should {
    "Redirect to case appeals page" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val result = await(controller(c).appealDetails(c.reference)(fakeRequest))

          status(result) shouldBe Status.SEE_OTHER

          redirectLocation(result) shouldBe Some(
            v2.routes.AtarController.displayAtar(c.reference).withFragment(Tab.APPEALS_TAB.name).path
          )
        }
      }

    }

    "redirect to Liability v2 controller when case is a liability" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {

          val c = aLiabilityCase(withStatus(s))

          val result = await(controller(c).appealDetails(c.reference)(fakeRequest))

          status(result) shouldBe Status.SEE_OTHER

          redirectLocation(result) shouldBe Some(
            v2.routes.LiabilityController.displayLiability(c.reference).withFragment(Tab.APPEALS_TAB.name).path
          )
        }
      }
    }
  }

  "Case Choose Type" should {
    "Return 200" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val request = newFakeGETRequestWithCSRF()
          val result  = await(controller(c).chooseType(c.reference)(request))

          status(result)          shouldBe Status.OK
          contentType(result)     shouldBe Some("text/html")
          charset(result)         shouldBe Some("utf-8")
          contentAsString(result) should include("appeal_choose_type")
        }
      }

    }

    "Redirect to unauthorised if no permissions" in {
      val result = await(controller(aCase(), Set.empty).chooseType("")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }
  }

  "Case Confirm Type" should {
    "Redirect to Next Stage" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val request = newFakePOSTRequestWithCSRF(Map("type" -> AppealType.REVIEW.toString))
          val result  = await(controller(c).confirmType(c.reference)(request))

          status(result) shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(
            routes.AppealCaseController.chooseStatus(c.reference, AppealType.REVIEW.toString).url
          )
        }
      }
    }

    "Redirect to unauthorised if no permissions" in {
      val result = await(controller(aCase(), Set.empty).confirmType("")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "Render Form errors" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val request = newFakePOSTRequestWithCSRF()
      val result  = await(controller(c).confirmType(c.reference)(request))

      status(result) shouldBe Status.OK
    }
  }

  "Case Choose Status" should {
    "Return 200" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())

          val request = newFakeGETRequestWithCSRF()
          val result  = await(controller(c).chooseStatus(c.reference, AppealType.REVIEW.toString)(request))

          status(result)          shouldBe Status.OK
          contentType(result)     shouldBe Some("text/html")
          charset(result)         shouldBe Some("utf-8")
          contentAsString(result) should include("appeal_choose_status")
        }
      }

    }

    "Redirect to unauthorised if no permissions" in {
      val result = await(controller(aCase(), Set.empty).chooseStatus("", "")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }
  }

  "Case Confirm Status" should {
    "Redirect to Appeal View" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val c = aCase(withStatus(s), withDecision())
          given(
            casesService.addAppeal(any[Case], any[AppealType], any[AppealStatus], any[Operator])(any[HeaderCarrier])
          ) willReturn Future.successful(c)

          val request = newFakePOSTRequestWithCSRF(Map("status" -> AppealStatus.ALLOWED.toString))
          val result  = await(controller(c).confirmStatus(c.reference, AppealType.REVIEW.toString)(request))

          status(result)     shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(routes.AppealCaseController.appealDetails(c.reference).url)

          verify(casesService).addAppeal(
            refEq(c),
            refEq(AppealType.REVIEW),
            refEq(AppealStatus.ALLOWED),
            refEq(operator)
          )(any[HeaderCarrier])
        }
      }
    }

    "Redirect to unauthorised if no permissions" in {
      val result = await(controller(aCase(), Set.empty).confirmStatus("", "")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "Render Form errors" in {
      val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision())

      val request = newFakePOSTRequestWithCSRF()
      val result  = await(controller(c).confirmStatus(c.reference, AppealType.REVIEW.toString)(request))

      status(result) shouldBe Status.OK
    }
  }

  "Case Change Status" should {
    "Return 200" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val appeal = Appeal("appeal-id", AppealStatus.IN_PROGRESS, AppealType.SUPREME_COURT)
          val c      = aCase(withStatus(s), withDecision(appeal = Seq(appeal)))

          val request = newFakeGETRequestWithCSRF()
          val result  = await(controller(c).changeStatus(c.reference, "appeal-id")(request))

          status(result)          shouldBe Status.OK
          contentType(result)     shouldBe Some("text/html")
          charset(result)         shouldBe Some("utf-8")
          contentAsString(result) should include("appeal_choose_status")
        }
      }

    }

    "Redirect to unauthorised if no permissions" in {
      val result = await(controller(aCase(), Set.empty).changeStatus("", "")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }
  }

  "Case Confirm Change Status" should {
    "Redirect to Appeal View" when {
      for (s <- Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)) {
        s"Case has status $s" in {
          val appeal = Appeal("appeal-id", AppealStatus.IN_PROGRESS, AppealType.SUPREME_COURT)
          val c      = aCase(withStatus(s), withDecision(appeal = Seq(appeal)))
          given(
            casesService
              .updateAppealStatus(any[Case], any[Appeal], any[AppealStatus], any[Operator])(any[HeaderCarrier])
          ) willReturn Future.successful(c)

          val request = newFakePOSTRequestWithCSRF(Map("status" -> AppealStatus.ALLOWED.toString))
          val result  = await(controller(c).confirmChangeStatus(c.reference, "appeal-id")(request))

          status(result)     shouldBe Status.SEE_OTHER
          locationOf(result) shouldBe Some(routes.AppealCaseController.appealDetails(c.reference).url)

          verify(casesService).updateAppealStatus(
            refEq(c),
            refEq(appeal),
            refEq(AppealStatus.ALLOWED),
            refEq(operator)
          )(any[HeaderCarrier])
        }
      }
    }

    "Redirect to unauthorised if no permissions" in {
      val result = await(controller(aCase(), Set.empty).confirmChangeStatus("", "")(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "Render Form errors" in {
      val appeal = Appeal("appeal-id", AppealStatus.IN_PROGRESS, AppealType.SUPREME_COURT)
      val c      = aCase(withStatus(CaseStatus.COMPLETED), withDecision(appeal = Seq(appeal)))

      val request = newFakePOSTRequestWithCSRF()
      val result  = await(controller(c).confirmChangeStatus(c.reference, "appeal-id")(request))

      status(result) shouldBe Status.OK
    }

    "Redirect" when {
      "Case does not have appeal with id" in {
        val appeal = Appeal("appeal-id", AppealStatus.IN_PROGRESS, AppealType.SUPREME_COURT)
        val c      = aCase(withStatus(CaseStatus.COMPLETED), withDecision(appeal = Seq(appeal)))

        val result = await(controller(c).confirmChangeStatus(c.reference, "some-id")(fakeRequest))

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.CaseController.get(c.reference).url)
      }
    }
  }

}
