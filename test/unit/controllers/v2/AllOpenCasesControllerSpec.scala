/*
 * Copyright 2021 HM Revenue & Customs
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
import models.viewmodels.{ATaRTab, CorrespondenceTab, LiabilitiesTab, MiscellaneousTab}
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.v2.open_cases_view

import scala.concurrent.ExecutionContext.Implicits.global

class AllOpenCasesControllerSpec extends ControllerBaseSpec {

  private val casesService  = mock[CasesService]
  private val queuesService = app.injector.instanceOf[QueuesService]

  private lazy val open_cases_view = injector.instanceOf[open_cases_view]

  private def controller(permission: Set[Permission]) = new AllOpenCasesController(
    new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
    casesService,
    queuesService,
    mcc,
    open_cases_view,
    realAppConfig
  )

  "Open cases" should {

    "return 200 OK and HTML content type" in {
      given(
        casesService
          .getCasesByAllQueues(any[Seq[Queue]], any[Pagination], any[Seq[ApplicationType]], any[String])(any[HeaderCarrier])
      ).willReturn(
        Paged(
          Seq(
            Cases.aCase(),
            Cases.aLiabilityCase().copy(queueId             = Some("3")),
            Cases.aLiabilityCase().copy(daysElapsed         = 35, queueId = Some("3")),
            Cases.liabilityLiveCaseExample.copy(queueId     = Some("3")),
            Cases.liabilityLiveCaseExample.copy(daysElapsed = 6, queueId = Some("3")),
            Cases.correspondenceCaseExample.copy(queueId    = Some("2")),
            Cases.correspondenceCaseExample.copy(queueId    = Some("1"))
          )
        )
      )
      val result = await(controller(Set(Permission.VIEW_QUEUE_CASES)).displayAllOpenCases(ATaRTab)(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return 200 OK and HTML content type for ATaR tab" in {
      given(
        casesService
          .getCasesByAllQueues(any[Seq[Queue]], any[Pagination], any[Seq[ApplicationType]], any[String])(any[HeaderCarrier])
      ).willReturn(
        Paged(
          Seq(
            Cases.aCase(),
            Cases.btiCaseExample.copy(queueId    = Some("2")),
            Cases.btiCaseExample.copy(queueId    = Some("1")),
            Cases.btiCaseExample.copy(queueId    = Some("3")),
            Cases.simpleCaseExample.copy(queueId = Some("3"))
          )
        )
      )
      val result = await(controller(Set(Permission.VIEW_QUEUE_CASES)).displayAllOpenCases(ATaRTab)(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).displayAllOpenCases(ATaRTab)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

    "return 200 OK and HTML content type for Liability tab" in {
      given(
        casesService
          .getCasesByAllQueues(any[Seq[Queue]], any[Pagination], any[Seq[ApplicationType]], any[String])(any[HeaderCarrier])
      ).willReturn(
        Paged(
          Seq(
            Cases.aCase(),
            Cases.aLiabilityCase().copy(queueId             = Some("3")),
            Cases.aLiabilityCase().copy(daysElapsed         = 35, queueId = Some("3")),
            Cases.liabilityLiveCaseExample.copy(queueId     = Some("3")),
            Cases.liabilityLiveCaseExample.copy(daysElapsed = 6, queueId = Some("3"))
          )
        )
      )
      val result = await(
        controller(Set(Permission.VIEW_CASES, Permission.VIEW_QUEUE_CASES, Permission.CREATE_CASES)).displayAllOpenCases(LiabilitiesTab)(fakeRequest)
      )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return 200 OK and HTML content type for Correspondence tab" in {

      given(
        casesService
          .getCasesByAllQueues(any[Seq[Queue]], any[Pagination], any[Seq[ApplicationType]], any[String])(any[HeaderCarrier])
      ).willReturn(
        Paged(
          Seq(
            Cases.aCase(),
            Cases.aLiabilityCase().copy(queueId             = Some("3")),
            Cases.aLiabilityCase().copy(daysElapsed         = 35, queueId = Some("3")),
            Cases.liabilityLiveCaseExample.copy(queueId     = Some("3")),
            Cases.liabilityLiveCaseExample.copy(daysElapsed = 6, queueId = Some("3")),
            Cases.correspondenceCaseExample.copy(queueId    = Some("2")),
            Cases.correspondenceCaseExample.copy(queueId    = Some("1"))
          )
        )
      )

      val result = await(
        controller(Set(Permission.VIEW_CASES, Permission.VIEW_QUEUE_CASES, Permission.CREATE_CASES))
          .displayAllOpenCases(CorrespondenceTab)(fakeRequest)
      )
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return 200 OK and HTML content type for Miscellaneous tab" in {
      given(
        casesService
          .getCasesByAllQueues(any[Seq[Queue]], any[Pagination], any[Seq[ApplicationType]], any[String])(any[HeaderCarrier])
      ).willReturn(
        Paged(
          Seq(
            Cases.aCase(),
            Cases.aLiabilityCase().copy(queueId             = Some("3")),
            Cases.aLiabilityCase().copy(daysElapsed         = 35, queueId = Some("3")),
            Cases.liabilityLiveCaseExample.copy(queueId     = Some("3")),
            Cases.liabilityLiveCaseExample.copy(daysElapsed = 6, queueId = Some("3")),
            Cases.correspondenceCaseExample.copy(queueId    = Some("2")),
            Cases.correspondenceCaseExample.copy(queueId    = Some("1"))
          )
        )
      )
      val result = await(controller(Set(Permission.VIEW_CASES, Permission.VIEW_QUEUE_CASES)).displayAllOpenCases(MiscellaneousTab)(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

  }

}
