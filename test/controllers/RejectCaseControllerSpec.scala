/*
 * Copyright 2025 HM Revenue & Customs
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
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.test.Helpers._
import services.{CasesService, FakeDataCacheService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import utils.JsonFormatters._
import views.html.{confirm_rejected, reject_case_email, reject_case_reason}

import scala.concurrent.Future.successful

class RejectCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val fileService  = mock[FileStoreService]
  private val operator     = Operator(id = "id")

  private val rejectCaseReason = injector.instanceOf[reject_case_reason]
  private val rejectCaseEmail  = injector.instanceOf[reject_case_email]
  private val confirmRejected  = injector.instanceOf[confirm_rejected]

  private val caseWithStatusNEW      = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN     = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusREJECTED = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.REJECTED)

  private val initiateResponse = FileStoreInitiateResponse(
    id = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
    reset(fileService)
    await(FakeDataCacheService.clear())
  }

  private def controller(c: Case) =
    new RejectCaseController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      fileService,
      FakeDataCacheService,
      mcc,
      rejectCaseReason,
      rejectCaseEmail,
      confirmRejected,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new RejectCaseController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      fileService,
      FakeDataCacheService,
      mcc,
      rejectCaseReason,
      rejectCaseEmail,
      confirmRejected,
      realAppConfig
    )

  "GET reject case reason" should {

    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.REJECT_CASE))
          .getRejectCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("change_case_status.rejected.reason.heading", caseWithStatusOPEN.application.goodsName)
      )
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .getRejectCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

  }

  "POST reject case reason" should {
    "redirect to reject case email page when filled correctly" in {
      val result = await(
        controller(caseWithStatusOPEN).postRejectCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(
            Map(
              "reason" -> RejectReason.DUPLICATE_APPLICATION.toString,
              "note"   -> "some-note"
            )
          )
        )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.RejectCaseController.getRejectCaseEmail(caseWithStatusOPEN.reference).path
      )
    }

    "display error page when reason is missing" in {
      val result = await(
        controller(caseWithStatusOPEN).postRejectCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(Map("note" -> "some-note"))
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)          should include(messages("error.empty.reject.reason"))
    }

    "display error page when note is missing" in {
      val result = await(
        controller(caseWithStatusOPEN).postRejectCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(Map("reason" -> RejectReason.ATAR_RULING_ALREADY_EXISTS.toString))
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)          should include(messages("error.empty.reject.note"))
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .postRejectCaseReason(caseWithStatusNEW.reference)(
            newFakePOSTRequestWithCSRF(
              Map(
                "reason" -> RejectReason.APPLICATION_WITHDRAWN.toString,
                "note"   -> "some-note"
              )
            )
          )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET reject case email" should {

    "return OK and HTML content type" in {
      when(fileService.initiate(any[FileStoreInitiateRequest])(any[HeaderCarrier])).thenReturn(
        successful(
          initiateResponse
        )
      )

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.REJECT_CASE))
          .getRejectCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("change_case_status.rejected.email.heading", caseWithStatusOPEN.application.goodsName)
      )
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .getRejectCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET reject case" should {

    "redirect to confirmation page" in {
      val cacheKey  = s"reject_case-${caseWithStatusOPEN.reference}"
      val rejection = CaseRejection(RejectReason.APPLICATION_WITHDRAWN, "some-note")
      val cacheMap  = UserAnswers(cacheKey).set("rejection", rejection).cacheMap
      await(FakeDataCacheService.save(cacheMap))

      when(
        casesService.rejectCase(
          refEq(caseWithStatusOPEN),
          refEq(RejectReason.APPLICATION_WITHDRAWN),
          any[Attachment],
          any[String],
          any[Operator]
        )(any[HeaderCarrier])
      ).thenReturn(successful(caseWithStatusREJECTED))

      val result = await(
        controller(caseWithStatusOPEN).rejectCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.RejectCaseController.confirmRejectCase(caseWithStatusREJECTED.reference).path
      )
    }

    "redirect to unauthorised when the user does not have any saved answers" in {
      val result = await(
        controller(caseWithStatusOPEN).rejectCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val cacheKey = s"reject_case-${caseWithStatusOPEN.reference}"
      val cacheMap = UserAnswers(cacheKey).set("note", "some-note").cacheMap
      await(FakeDataCacheService.save(cacheMap))

      val result = await(
        controller(caseWithStatusOPEN, Set.empty).rejectCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET confirm reject case" should {
    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusREJECTED).confirmRejectCase(caseWithStatusREJECTED.reference)(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)          should include(messages("page.title.case.rejected"))
    }
  }
}
