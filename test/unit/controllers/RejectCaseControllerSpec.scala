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

package controllers

import connector.FakeDataCacheConnector
import models._
import models.request.FileStoreInitiateRequest
import models.response._
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.test.Helpers._
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import utils.JsonFormatters._

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

class RejectCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val fileService  = mock[FileStoreService]
  private val operator     = Operator(id = "id")

  private val caseWithStatusNEW      = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN     = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusREJECTED = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.REJECTED)

  private val initiateResponse = FileStoreInitiateResponse(
    id              = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService, fileService)
    await(FakeDataCacheConnector.clear())
  }

  private def controller(c: Case) =
    new RejectCaseController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      fileService,
      FakeDataCacheConnector,
      mcc,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new RejectCaseController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      fileService,
      FakeDataCacheConnector,
      mcc,
      realAppConfig
    )

  "GET reject case reason" should {

    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.REJECT_CASE))
          .getRejectCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF(app))
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
          .getRejectCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF(app))
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
            app,
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
          newFakePOSTRequestWithCSRF(app, Map("note" -> "some-note"))
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messages("error.empty.reject.reason"))
    }

    "display error page when note is missing" in {
      val result = await(
        controller(caseWithStatusOPEN).postRejectCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(app, Map("reason" -> RejectReason.ATAR_RULING_ALREADY_EXISTS.toString))
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messages("error.empty.reject.note"))
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .postRejectCaseReason(caseWithStatusNEW.reference)(
            newFakePOSTRequestWithCSRF(
              app,
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
      given(fileService.initiate(any[FileStoreInitiateRequest])(any[HeaderCarrier])) willReturn successful(
        initiateResponse
      )

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.REJECT_CASE))
          .getRejectCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF(app))
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
          .getRejectCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF(app))
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET reject case" should {

    "redirect to confirmation page" in {
      val cacheKey = s"reject_case-${caseWithStatusOPEN.reference}"
      val rejection = CaseRejection(RejectReason.APPLICATION_WITHDRAWN, "some-note")
      val cacheMap = UserAnswers(cacheKey).set("rejection", rejection).cacheMap
      await(FakeDataCacheConnector.save(cacheMap))

      given(
        casesService.rejectCase(
          refEq(caseWithStatusOPEN),
          refEq(RejectReason.APPLICATION_WITHDRAWN),
          any[Attachment],
          any[String],
          any[Operator]
        )(any[HeaderCarrier])
      ) willReturn successful(caseWithStatusREJECTED)

      val result = await(
        controller(caseWithStatusOPEN).rejectCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF(app)
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
          newFakeGETRequestWithCSRF(app)
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val cacheKey = s"reject_case-${caseWithStatusOPEN.reference}"
      val cacheMap = UserAnswers(cacheKey).set("note", "some-note").cacheMap
      await(FakeDataCacheConnector.save(cacheMap))

      val result = await(
        controller(caseWithStatusOPEN, Set.empty).rejectCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF(app)
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
          newFakeGETRequestWithCSRF(app)
        )
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messages("page.title.case.rejected"))
    }
  }
}
