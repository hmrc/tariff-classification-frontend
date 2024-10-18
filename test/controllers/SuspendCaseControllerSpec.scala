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

package controllers

import models._
import models.request.FileStoreInitiateRequest
import models.response._
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.test.Helpers._
import services.{CasesService, FakeDataCacheService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.{confirm_suspended, suspend_case_email, suspend_case_reason}

import scala.concurrent.Future.successful

class SuspendCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService      = mock[CasesService]
  private val fileService       = mock[FileStoreService]
  private val operator          = Operator(id = "id")
  private val suspendCaseReason = app.injector.instanceOf[suspend_case_reason]
  private val suspendCaseEmail  = app.injector.instanceOf[suspend_case_email]
  private val confirmSuspended  = app.injector.instanceOf[confirm_suspended]

  private val caseWithStatusNEW  = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusSUSPENDED =
    Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.SUSPENDED)

  private val initiateResponse = FileStoreInitiateResponse(
    id = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  private def controller(requestCase: Case) = new SuspendCaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    fileService,
    FakeDataCacheService,
    mcc,
    suspendCaseReason,
    suspendCaseEmail,
    confirmSuspended,
    realAppConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new SuspendCaseController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    fileService,
    FakeDataCacheService,
    mcc,
    suspendCaseReason,
    suspendCaseEmail,
    confirmSuspended,
    realAppConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
    reset(fileService)
    await(FakeDataCacheService.clear())
  }

  "GET suspend case reason" should {

    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.SUSPEND_CASE))
          .getSuspendCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("change_case_status.suspended.reason.heading", caseWithStatusOPEN.application.goodsName)
      )
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .getSuspendCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

  }

  "POST suspend case reason" should {
    "redirect to suspend case email page when filled correctly" in {
      val result = await(
        controller(caseWithStatusOPEN).postSuspendCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(
            Map("note" -> "some-note")
          )
        )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.SuspendCaseController.getSuspendCaseEmail(caseWithStatusOPEN.reference).path
      )
    }

    "display error page when note is missing" in {
      val result = await(
        controller(caseWithStatusOPEN).postSuspendCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF()
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)          should include(messages("error.empty.suspend.note"))
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty).postSuspendCaseReason(caseWithStatusNEW.reference)(
          newFakePOSTRequestWithCSRF(Map("note" -> "some-note"))
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET suspend case email" should {

    "return OK and HTML content type" in {
      given(fileService.initiate(any[FileStoreInitiateRequest])(any[HeaderCarrier])) willReturn successful(
        initiateResponse
      )

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.SUSPEND_CASE))
          .getSuspendCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("change_case_status.suspended.email.heading", caseWithStatusOPEN.application.goodsName)
      )
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .getSuspendCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET suspend case" should {

    "redirect to confirmation page" in {
      val cacheKey = s"suspend_case-${caseWithStatusOPEN.reference}"
      val cacheMap = UserAnswers(cacheKey).set("note", "some-note").cacheMap
      await(FakeDataCacheService.save(cacheMap))

      given(
        casesService.suspendCase(
          refEq(caseWithStatusOPEN),
          any[Attachment],
          any[String],
          any[Operator]
        )(any[HeaderCarrier])
      ) willReturn successful(caseWithStatusSUSPENDED)

      val result = await(
        controller(caseWithStatusOPEN).suspendCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.SuspendCaseController.confirmSuspendCase(caseWithStatusSUSPENDED.reference).path
      )
    }

    "redirect to unauthorised when the user does not have any saved answers" in {
      val result = await(
        controller(caseWithStatusOPEN).suspendCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val cacheKey = s"suspend_case-${caseWithStatusOPEN.reference}"
      val cacheMap = UserAnswers(cacheKey).set("note", "some-note").cacheMap
      await(FakeDataCacheService.save(cacheMap))

      val result = await(
        controller(caseWithStatusOPEN, Set.empty).suspendCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET confirm suspend case" should {
    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusSUSPENDED).confirmSuspendCase(caseWithStatusSUSPENDED.reference)(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(messages("case.suspended.header", caseWithStatusSUSPENDED.application.goodsName))
    }
  }
}
