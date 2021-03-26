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
import models.response.{FileStoreInitiateResponse, UpscanFormTemplate}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{AnyContentAsMultipartFormData, MultipartFormData}
import play.api.test.Helpers._
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import utils.JsonFormatters._

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

class CancelRulingControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {
  private val casesService = mock[CasesService]
  private val fileService  = mock[FileStoreService]

  private val caseWithStatusCOMPLETED = Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED)
  private val caseWithStatusCANCELLED = Cases.btiCaseExample.copy(status = CaseStatus.CANCELLED)

  private val initiateResponse = FileStoreInitiateResponse(
    id              = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  private def controller(requestCase: Case) = new CancelRulingController(
    new SuccessfulRequestActions(playBodyParsers, Cases.operatorWithPermissions, c = requestCase),
    casesService,
    fileService,
    FakeDataCacheConnector,
    mcc,
    realAppConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new CancelRulingController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    fileService,
    FakeDataCacheConnector,
    mcc,
    realAppConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService, fileService)
    await(FakeDataCacheConnector.clear())
  }

  private def aMultipartBodyWithParams(params: (String, Seq[String])*): AnyContentAsMultipartFormData =
    AnyContentAsMultipartFormData(
      MultipartFormData[TemporaryFile](dataParts = params.toMap, files = Seq.empty, badParts = Seq.empty)
    )

  "GET cancel ruling reason" should {

    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusCOMPLETED, Set(Permission.CANCEL_CASE))
          .getCancelRulingReason(caseWithStatusCOMPLETED.reference)(newFakeGETRequestWithCSRF(app))
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messages("change_case_status.cancelled.reason.heading", caseWithStatusCOMPLETED.application.goodsName))
    }

    "redirect to unauthorised when the user does not have right permissions" in {
      val result = await(
        controller(caseWithStatusCOMPLETED, Set.empty)
          .getCancelRulingReason(caseWithStatusCOMPLETED.reference)(newFakeGETRequestWithCSRF(app))
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

  }

  "POST cancel ruling reason" should {
    "redirect to cancel ruling email page when filled correctly" in {
      val result = await(
        controller(caseWithStatusCOMPLETED, Set(Permission.CANCEL_CASE))
          .postCancelRulingReason(caseWithStatusCOMPLETED.reference)(
            newFakePOSTRequestWithCSRF(app, Map("reason" -> "ANNULLED", "note" -> "some-note"))
          )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.CancelRulingController.getCancelRulingEmail(caseWithStatusCOMPLETED.reference).path
      )
    }

    "display error page when reason is missing" in {
      val result = await(
        controller(caseWithStatusCOMPLETED, Set(Permission.CANCEL_CASE))
          .postCancelRulingReason(caseWithStatusCOMPLETED.reference)(
            newFakePOSTRequestWithCSRF(app, Map("note" -> "some-note"))
          )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messages("status.change.cancel.reason.error"))
    }

    "display error page when note is missing" in {
      val result = await(
        controller(caseWithStatusCOMPLETED, Set(Permission.CANCEL_CASE))
          .postCancelRulingReason(caseWithStatusCOMPLETED.reference)(
            newFakePOSTRequestWithCSRF(app, Map("reason" -> "ANNULLED"))
          )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messages("error.empty.cancel.note"))
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusCOMPLETED, Set.empty)
          .postCancelRulingReason(caseWithStatusCOMPLETED.reference)(newFakeGETRequestWithCSRF(app))
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET cancel ruling email" should {

    "return OK and HTML content type" in {
      given(fileService.initiate(any[FileStoreInitiateRequest])(any[HeaderCarrier])) willReturn successful(
        initiateResponse
      )

      val result = await(
        controller(caseWithStatusCOMPLETED, Set(Permission.CANCEL_CASE))
          .getCancelRulingEmail(caseWithStatusCOMPLETED.reference)(newFakeGETRequestWithCSRF(app))
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messages("change_case_status.cancelled.email.heading", caseWithStatusCOMPLETED.application.goodsName))
    }

    "redirect to unauthorised when the user does not have right permissions" in {
      val result = await(
        controller(caseWithStatusCOMPLETED, Set.empty)
          .getCancelRulingEmail(caseWithStatusCOMPLETED.reference)(newFakeGETRequestWithCSRF(app))
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET cancel ruling" should {

    "redirect to confirmation page" in {
      val cacheKey = s"cancel_ruling-${caseWithStatusCOMPLETED.reference}"
      val cacheMap = UserAnswers(cacheKey).set("cancellation", RulingCancellation("ANNULLED", "some-note")).cacheMap
      await(FakeDataCacheConnector.save(cacheMap))

      given(
        casesService.cancelRuling(
          refEq(caseWithStatusCOMPLETED),
          refEq(CancelReason.ANNULLED),
          any[Attachment],
          any[String],
          any[Operator]
        )(any[HeaderCarrier])
      ) willReturn successful(caseWithStatusCANCELLED)

      val result = await(
        controller(caseWithStatusCOMPLETED).cancelRuling(caseWithStatusCOMPLETED.reference, "id")(
          newFakeGETRequestWithCSRF(app)
        )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.CancelRulingController.confirmCancelRuling(caseWithStatusCOMPLETED.reference).path
      )
    }

    "redirect to unauthorised when the user does not have any saved answers" in {
      val result = await(
        controller(caseWithStatusCOMPLETED).cancelRuling(caseWithStatusCOMPLETED.reference, "id")(
          newFakeGETRequestWithCSRF(app)
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val cacheKey = s"cancel_ruling-${caseWithStatusCOMPLETED.reference}"
      val cacheMap = UserAnswers(cacheKey).set("cancellation", RulingCancellation("ANNULLED", "some-note")).cacheMap
      await(FakeDataCacheConnector.save(cacheMap))

      val result = await(
        controller(caseWithStatusCOMPLETED, Set.empty)
          .postCancelRulingReason(caseWithStatusCOMPLETED.reference)(
            newFakePOSTRequestWithCSRF(app)
              .withBody(aMultipartBodyWithParams("reason" -> Seq("ANNULLED"), "note" -> Seq("some-note")))
          )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET confirm cancel ruling" should {
    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusCANCELLED).confirmCancelRuling(caseWithStatusCANCELLED.reference)(
          newFakeGETRequestWithCSRF(app)
        )
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("The ruling has been cancelled")
    }
  }
}
