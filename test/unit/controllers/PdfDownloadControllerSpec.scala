/*
 * Copyright 2022 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global
import models.response.FileMetadata
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.scalatest.BeforeAndAfterEach
import views.html.{case_not_found, document_not_found, ruling_not_found}

class PdfDownloadControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val caseService = mock[CasesService]
  private val fileService = mock[FileStoreService]
  private val operator    = Operator(id = "id")

  private val caseNotFound = injector.instanceOf[case_not_found]
  private val rulingNotFound = injector.instanceOf[ruling_not_found]
  private val documentNotFound = injector.instanceOf[document_not_found]

  override protected def beforeEach(): Unit =
    reset(
      caseService,
      fileService
    )

  private val decision = Decision(
    bindingCommodityCode = "040900",
    justification        = "justification-content",
    goodsDescription     = "goods-description",
    methodSearch         = Some("method-to-search"),
    decisionPdf          = Some(Attachment("id", false, Some(Operator("1", None))))
  )

  private val caseWithDecision          = Cases.btiCaseExample.copy(decision       = Some(decision))
  private val caseWithoutDecision       = Cases.btiCaseExample.copy(decision       = None)
  private val liabilityCaseWithDecision = Cases.liabilityCaseExample.copy(decision = Some(decision))

  private val pdfUrl      = "http://localhost:4572/digital-tariffs-local/id"
  private val pdfMetadata = FileMetadata("id", Some("some.pdf"), Some("application/pdf"), Some(pdfUrl))

  private val controller = new PdfDownloadController(
    new SuccessfulAuthenticatedAction(playBodyParsers, operator),
    mcc,
    fileService,
    caseService,
    realAppConfig,
    caseNotFound,
    rulingNotFound,
    documentNotFound
  )

  private def givenCompletedCase(): Unit =
    when(caseService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(caseWithDecision)))

  private def givenCompletedLiabilityCase(): Unit =
    when(caseService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(liabilityCaseWithDecision)))

  private def givenNonDecisionCase(): Unit =
    when(caseService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(caseWithoutDecision)))

  private def givenCaseWithoutAttachments(): Unit =
    when(fileService.getAttachments(any[Case])(any[HeaderCarrier])).thenReturn(successful(Seq.empty))

  private def givenCaseWithoutLetterOfAuth(): Unit =
    when(fileService.getLetterOfAuthority(any[Case])(any[HeaderCarrier])).thenReturn(successful(None))

  private def givenValidStoredPdf(): Unit = {
    when(fileService.getFileMetadata(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(pdfMetadata)))
    when(fileService.downloadFile(any[String])(any[HeaderCarrier]))
      .thenReturn(successful(Some(Source.single(ByteString("Some content".getBytes())))))
  }

  private def givenNotFoundPdf(): Unit =
    when(fileService.getFileMetadata(any[String])(any[HeaderCarrier])).thenReturn(successful(None))

  private def givenNotFoundCase(): Unit =
    when(caseService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(None))

  "PdfDownloadController Application" should {

    "return expected pdf" in {
      givenCompletedCase()
      givenCaseWithoutAttachments()
      givenCaseWithoutLetterOfAuth()
      givenValidStoredPdf()

      val result: Result = await(controller.applicationPdf(caseWithDecision.reference)(fakeRequest))

      status(result)                        shouldBe OK
      contentAsString(result)               shouldBe "Some content"
      contentType(result)                   shouldBe Some("application/pdf")
      header("Content-Disposition", result) shouldBe Some("attachment; filename=some.pdf")
    }

    "error when case not found" in {
      givenNotFoundCase()

      val result = await(controller.applicationPdf(caseWithDecision.reference)(newFakeGETRequestWithCSRF()))

      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

    "error when document not found" in {
      givenCompletedCase()
      givenCaseWithoutAttachments()
      givenNotFoundPdf()

      val result = await(controller.applicationPdf(caseWithDecision.reference)(newFakeGETRequestWithCSRF()))

      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find an application document for case reference")
    }

  }

  "PdfDownloadController Ruling" should {

    "return expected pdf" in {
      givenCompletedCase()
      givenValidStoredPdf()

      val result = await(controller.getRulingPdf(caseWithDecision.reference)(fakeRequest))

      status(result)                        shouldBe OK
      contentAsString(result)               shouldBe "Some content"
      contentType(result)                   shouldBe Some("application/pdf")
      header("Content-Disposition", result) shouldBe (Some("attachment; filename=some.pdf"))
    }

    "return expected pdf for liability case" in {
      givenCompletedLiabilityCase()
      givenValidStoredPdf()

      val result = await(controller.getRulingPdf(liabilityCaseWithDecision.reference)(fakeRequest))

      status(result)                        shouldBe OK
      contentAsString(result)               shouldBe "Some content"
      contentType(result)                   shouldBe Some("application/pdf")
      header("Content-Disposition", result) shouldBe (Some("attachment; filename=some.pdf"))
    }

    "redirect to ruling when no decision found" in {
      givenNonDecisionCase()
      givenValidStoredPdf()

      val result = await(controller.getRulingPdf(caseWithDecision.reference)(fakeRequest))

      status(result)          shouldBe Status.NOT_FOUND
      locationOf(result)      shouldBe None
      contentAsString(result) should include("We could not find a ruling with case reference")
    }

    "error when case not found" in {
      givenNotFoundCase()

      val result = await(controller.getRulingPdf(caseWithDecision.reference)(newFakeGETRequestWithCSRF()))

      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

    "error when document not found" in {
      givenCompletedCase()
      givenCaseWithoutAttachments()
      givenNotFoundPdf()

      val result = await(controller.getRulingPdf(caseWithDecision.reference)(newFakeGETRequestWithCSRF()))

      status(result)          shouldBe Status.NOT_FOUND
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a ruling certificate document for case reference")
    }
  }
}
