/*
 * Copyright 2020 HM Revenue & Customs
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

import java.io.File

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{MimeTypes, Status}
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{BodyParsers, MessagesControllerComponents, MultipartFormData, Result}
import play.api.test.Helpers.{redirectLocation, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import config.AppConfig
import models.{Permission, _}
import service.CasesService
import utils.Cases

import scala.concurrent.Future.successful

class CancelRulingControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val caseWithStatusCOMPLETED = Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED)
  private val caseWithStatusCANCELLED = Cases.btiCaseExample.copy(status = CaseStatus.CANCELLED)

  private val largeFileSize :Long = 16485760

  private def controller(requestCase: Case) = new CancelRulingController(
    new SuccessfulRequestActions(defaultPlayBodyParsers, operator, c = requestCase), casesService, mcc, realAppConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new CancelRulingController(
    new RequestActionsWithPermissions(defaultPlayBodyParsers, permission, c = requestCase), casesService, mcc, realAppConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def aMultipartFileWithParams(params: (String, Seq[String])*): MultipartFormData[TemporaryFile] = {
    val file = SingletonTemporaryFileCreator.create("example-file.txt")
    val filePart = FilePart[TemporaryFile](key = "email", "file.txt", contentType = Some("text/plain"), ref = file)
    MultipartFormData[TemporaryFile](dataParts = params.toMap, files = Seq(filePart), badParts = Seq.empty)
  }

  private def aEmptyMultipartFileWithParams(params: (String, Seq[String])*): MultipartFormData[TemporaryFile] = {
    val filePart = FilePart[TemporaryFile](key = "email", "", contentType = Some("text/plain"), ref = SingletonTemporaryFileCreator.create("example-file.txt"))
    MultipartFormData[TemporaryFile](dataParts =params.toMap, files = Seq(filePart), badParts = Seq.empty)
  }

  private def aMultipartFileOfType(mimeType: String): MultipartFormData[TemporaryFile] = {
    val file = SingletonTemporaryFileCreator.create("example-file")
    val filePart = FilePart[TemporaryFile](key = "email", "example-file", contentType = Some(mimeType), ref = file)
    MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
  }

  private def aMultipartFileOfLargeSize: MultipartFormData[TemporaryFile] = {
    val file = mock[TemporaryFile]
    val innerFile: File = mock[File]
    when(file.file).thenReturn(innerFile)
    when(innerFile.length()).thenReturn(largeFileSize)
    val filePart = FilePart[TemporaryFile](key = "email", "example-file", contentType = Some("text/plain"), ref = file)
    MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
  }

  "Cancel Ruling" should {

    "return OK and HTML content type" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED).getCancelRuling("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Cancel the ruling")
    }

    "return OK when user has right permissions" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED, Set(Permission.CANCEL_CASE))
        .getCancelRuling("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED, Set.empty)
        .getCancelRuling("reference")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "Post Confirm Cancel a Ruling" should {

    "redirect to confirmation page when data filled correctly" in {
      when(casesService.cancelRuling(refEq(caseWithStatusCOMPLETED), refEq(CancelReason.ANNULLED), any[FileUpload], any[String], any[Operator])
      (any[HeaderCarrier])).thenReturn(successful(caseWithStatusCANCELLED))

      val result: Result = await(controller(caseWithStatusCOMPLETED).postCancelRuling("reference")(newFakePOSTRequestWithCSRF(fakeApplication)
        .withBody(aMultipartFileWithParams("reason" -> Seq("ANNULLED"), "note" -> Seq("some-note")))))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/1/ruling/cancel/confirmation")
    }

    "display required field when failing to submit reason" in {
      when(casesService.cancelRuling(refEq(caseWithStatusCOMPLETED), refEq(CancelReason.ANNULLED), any[FileUpload], any[String], any[Operator])
      (any[HeaderCarrier])).thenReturn(successful(caseWithStatusCANCELLED))

      val result: Result = await(controller(caseWithStatusCOMPLETED).postCancelRuling("reference")(newFakePOSTRequestWithCSRF(fakeApplication)
        .withBody(aMultipartFileWithParams("note" -> Seq("some-note")))))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Select a reason for cancelling this case")
    }

    "return to form on missing file" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED).postCancelRuling("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication).withBody(aEmptyMultipartFileWithParams())))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Cancelled")
    }

    "return to form on wrong type of file" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED).postCancelRuling("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication).withBody(aMultipartFileOfType("audio/mpeg"))))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Cancelled")
    }

    "return to form on file size too large" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED).postCancelRuling("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication).withBody(aMultipartFileOfLargeSize)))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Cancelled")
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusCOMPLETED, Set.empty)
        .postCancelRuling("reference")(newFakePOSTRequestWithCSRF(fakeApplication)
          .withBody(aMultipartFileWithParams("reason" -> Seq("ANNULLED"), "note" -> Seq("some-note")))))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

}
