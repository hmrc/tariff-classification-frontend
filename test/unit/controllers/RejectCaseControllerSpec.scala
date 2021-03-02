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

import models.RejectReason.RejectReason

import java.io.File
import models.{Permission, _}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MultipartFormData, Result}
import play.api.test.Helpers.{redirectLocation, _}
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global
import java.nio.file.Path

class RejectCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = Operator(id = "id")

  private val caseWithStatusNEW      = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN     = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusREJECTED = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.REJECTED)

  private val largeFileSize: Long = 16485760

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(c: Case) =
    new RejectCaseController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      mcc,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new RejectCaseController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      mcc,
      realAppConfig
    )

  private def aMultipartFileWithParams(params: (String, Seq[String])*): MultipartFormData[TemporaryFile] = {
    val file     = SingletonTemporaryFileCreator.create("example-file.txt")
    val filePart = FilePart[TemporaryFile](key = "file-input", "file.txt", contentType = Some("text/plain"), ref = file)
    MultipartFormData[TemporaryFile](dataParts = params.toMap, files = Seq(filePart), badParts = Seq.empty)
  }

  private def aEmptyMultipartFileWithParams(params: (String, Seq[String])*): MultipartFormData[TemporaryFile] = {
    val file     = SingletonTemporaryFileCreator.create("example-file.txt")
    val filePart = FilePart[TemporaryFile](key = "file-input", "", contentType = Some("text/plain"), ref = file)
    MultipartFormData[TemporaryFile](dataParts = params.toMap, files = Seq(filePart), badParts = Seq.empty)
  }

  private def aMultipartFileOfType(mimeType: String): MultipartFormData[TemporaryFile] = {
    val file     = SingletonTemporaryFileCreator.create("example-file")
    val filePart = FilePart[TemporaryFile](key = "file-input", "example-file", contentType = Some(mimeType), ref = file)
    MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
  }

  private def aMultipartFileOfLargeSize: MultipartFormData[TemporaryFile] = {
    val file      = mock[TemporaryFile]
    val filePath  = mock[Path]
    val innerFile = mock[File]
    when(file.path).thenReturn(filePath)
    when(filePath.toFile).thenReturn(innerFile)
    when(innerFile.length()).thenReturn(largeFileSize)
    val filePart =
      FilePart[TemporaryFile](key = "file-input", "example-file", contentType = Some("text/plain"), ref = file)
    MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
  }

  "Reject Case" should {

    "return OK and HTML content type" in {

      val result: Result =
        await(controller(caseWithStatusOPEN).getRejectCase("reference")(newFakeGETRequestWithCSRF(app)))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("Reject case for")
    }

    "return OK when user has right permissions" in {
      val result: Result = await(
        controller(caseWithStatusOPEN, Set(Permission.REJECT_CASE))
          .getRejectCase("reference")(newFakeGETRequestWithCSRF(app))
      )

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(caseWithStatusNEW, Set.empty)
          .getRejectCase("reference")(newFakeGETRequestWithCSRF(app))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Post Confirm Reject a Case" should {

    "redirect to confirmation page when data filled in" in {
      when(
        casesService
          .rejectCase(refEq(caseWithStatusOPEN), any[RejectReason], any[FileUpload], any[String], any[Operator])(any[HeaderCarrier])
      ).thenReturn(successful(caseWithStatusREJECTED))

      val result: Result = await(
        controller(caseWithStatusOPEN).postRejectCase("reference")(
          newFakePOSTRequestWithCSRF(app)
            .withBody(aMultipartFileWithParams(
              "reason" -> Seq(RejectReason.DUPLICATE_APPLICATION.toString),
              "note" -> Seq("some-note")
            )
          )
        )
      )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/reject/confirmation")
    }

    "return to form on missing file" in {
      val result: Result = await(
        controller(caseWithStatusOPEN)
          .postRejectCase("reference")(newFakePOSTRequestWithCSRF(app).withBody(aEmptyMultipartFileWithParams()))
      )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Reject case for")
    }

    "return to form on wrong type of file" in {
      val result: Result = await(
        controller(caseWithStatusOPEN).postRejectCase("reference")(
          newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileOfType("audio/mpeg"))
        )
      )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Reject case for")
    }

    "return to form on file size too large" in {
      val result: Result = await(
        controller(caseWithStatusOPEN)
          .postRejectCase("reference")(newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileOfLargeSize))
      )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Reject case for")
    }

    "return to form on missing form field" in {
      val result: Result = await(
        controller(caseWithStatusOPEN)
          .postRejectCase("reference")(newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams()))
      )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Reject case for")
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(caseWithStatusOPEN, Set.empty).postRejectCase("reference")(
          newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams("note" -> Seq("some-note")))
        )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "View Confirm page for a rejected case" should {

    "return OK and HTML content type" in {
      val result: Result = await(
        controller(caseWithStatusREJECTED).confirmRejectCase("reference")(
          newFakePOSTRequestWithCSRF(app)
            .withFormUrlEncodedBody("state" -> "true")
        )
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("Case rejected for")
    }
  }

}
