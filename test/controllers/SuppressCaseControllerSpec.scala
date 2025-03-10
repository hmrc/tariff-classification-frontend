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
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.test.Helpers._
import services.{CasesService, FakeDataCacheService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.{confirm_supressed_case, suppress_case_email, suppress_case_reason}

import scala.concurrent.Future.successful

class SuppressCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService          = mock[CasesService]
  private val fileService           = mock[FileStoreService]
  private val operator              = Operator(id = "id")
  private val suppressCaseReason    = app.injector.instanceOf[suppress_case_reason]
  private val suppressCaseEmail     = app.injector.instanceOf[suppress_case_email]
  private val confirmSuppressedCase = app.injector.instanceOf[confirm_supressed_case]

  private val caseWithStatusNEW  = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusSUPPRESSED =
    Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.SUPPRESSED)

  private val initiateResponse = FileStoreInitiateResponse(
    id = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  private def controller(requestCase: Case) = new SuppressCaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    fileService,
    FakeDataCacheService,
    mcc,
    suppressCaseReason,
    suppressCaseEmail,
    confirmSuppressedCase,
    realAppConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new SuppressCaseController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    fileService,
    FakeDataCacheService,
    mcc,
    suppressCaseReason,
    suppressCaseEmail,
    confirmSuppressedCase,
    realAppConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
    reset(fileService)
    await(FakeDataCacheService.clear())
  }

  "GET suppress case reason" should {

    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.SUPPRESS_CASE))
          .getSuppressCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("change_case_status.suppressed.reason.heading", caseWithStatusOPEN.application.goodsName)
      )
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .getSuppressCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

  }

  "POST suppress case reason" should {
    "redirect to suppress case email page when filled correctly" in {
      val result = await(
        controller(caseWithStatusOPEN).postSuppressCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(
            Map("note" -> "some-note")
          )
        )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.SuppressCaseController.getSuppressCaseEmail(caseWithStatusOPEN.reference).path
      )
    }

    "display error page when note is missing" in {
      val result = await(
        controller(caseWithStatusOPEN).postSuppressCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(Map.empty[String, String])
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)          should include(messages("error.empty.suppress.note"))
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty).postSuppressCaseReason(caseWithStatusNEW.reference)(
          newFakePOSTRequestWithCSRF(Map("note" -> "some-note"))
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET suppress case email" should {

    "return OK and HTML content type" in {
      when(fileService.initiate(any[FileStoreInitiateRequest])(any[HeaderCarrier])) thenReturn successful(
        initiateResponse
      )

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.SUPPRESS_CASE))
          .getSuppressCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("change_case_status.suppressed.email.heading", caseWithStatusOPEN.application.goodsName)
      )
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .getSuppressCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET suppress case" should {

    "redirect to confirmation page" in {
      val cacheKey = s"suppress_case-${caseWithStatusOPEN.reference}"
      val cacheMap = UserAnswers(cacheKey).set("note", "some-note").cacheMap
      await(FakeDataCacheService.save(cacheMap))

      when(
        casesService.suppressCase(
          refEq(caseWithStatusOPEN),
          any[Attachment],
          any[String],
          any[Operator]
        )(any[HeaderCarrier])
      ) thenReturn successful(caseWithStatusSUPPRESSED)

      val result = await(
        controller(caseWithStatusOPEN).suppressCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.SuppressCaseController.confirmSuppressCase(caseWithStatusSUPPRESSED.reference).path
      )
    }

    "redirect to unauthorised when the user does not have any saved answers" in {
      val result = await(
        controller(caseWithStatusOPEN).suppressCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val cacheKey = s"suppress_case-${caseWithStatusOPEN.reference}"
      val cacheMap = UserAnswers(cacheKey).set("note", "some-note").cacheMap
      await(FakeDataCacheService.save(cacheMap))

      val result = await(
        controller(caseWithStatusOPEN, Set.empty).suppressCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET confirm suppress case" should {
    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusSUPPRESSED).confirmSuppressCase(caseWithStatusSUPPRESSED.reference)(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("suppress_case.confirm.header", caseWithStatusSUPPRESSED.application.goodsName)
      )
    }
  }
}
