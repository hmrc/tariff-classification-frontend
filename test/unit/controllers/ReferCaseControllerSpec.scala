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
import org.mockito.ArgumentCaptor
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
import models.ReferralReason.ReferralReason
import models._
import service.CasesService
import utils.Cases

import scala.concurrent.Future.successful

class ReferCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val largeFileSize :Long = 16485760
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]

  private val caseWithStatusNEW = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusREFERRED = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.REFERRED)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(requestedCase: Case) = new ReferCaseController(
    new SuccessfulRequestActions(inject[BodyParsers.Default], operator, c = requestedCase), casesService, mcc, realAppConfig)

  private def controller(requestCase: Case, permission: Set[Permission]) = new ReferCaseController(
    new RequestActionsWithPermissions(inject[BodyParsers.Default], permission, c = requestCase), casesService, mcc, realAppConfig)

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

  "Refer Case" should {

    "return OK and HTML content type" in {
      val result: Result = await(controller(caseWithStatusOPEN).getReferCase("reference", None)(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Change case status to: Referred")
    }

    "return OK when user has right permissions" in {
      val result: Result = await(controller(caseWithStatusOPEN, Set(Permission.REFER_CASE))
        .getReferCase("reference", None)(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusNEW, Set.empty).getReferCase("reference", None)(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "Post Confirm Refer a Case" should {

    "redirect to confirmation page when data filled correctly" in {
      when(casesService.referCase(refEq(caseWithStatusOPEN),any[String],any[Seq[ReferralReason]],any[FileUpload],
        any[String], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusREFERRED))

      val result: Result = await(controller(caseWithStatusOPEN).postReferCase("reference", None)(newFakePOSTRequestWithCSRF(fakeApplication)
        .withBody(aMultipartFileWithParams("referredTo" -> Seq("Applicant"), "reasons[0]" -> Seq(ReferralReason.REQUEST_SAMPLE.toString), "note" -> Seq("some-note")))))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/refer/confirmation")
    }

    "display required field when failing to submit referred to" in {
      when(casesService.referCase(refEq(caseWithStatusOPEN), any[String],any[Seq[ReferralReason]],any[FileUpload],
        any[String], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusREFERRED))

      val result: Result = await(controller(caseWithStatusOPEN).postReferCase("reference", None)(newFakePOSTRequestWithCSRF(fakeApplication)
        .withBody(aMultipartFileWithParams("note" -> Seq("some-note")))))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Select who you are referring this case to")
    }

    "display required field when failing to submit reason when referred to is Applicant" in {
      when(casesService.referCase(refEq(caseWithStatusOPEN), any[String],any[Seq[ReferralReason]],any[FileUpload],
        any[String], refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusREFERRED))

      val result: Result = await(controller(caseWithStatusOPEN).postReferCase("reference", None)(newFakePOSTRequestWithCSRF(fakeApplication)
        .withBody(aMultipartFileWithParams("referredTo" -> Seq("Applicant"), "note" -> Seq("some-note")))))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Select why you are referring this case")
    }

    "display required field when failing to submit referManually detail when referred to is Other" in {
      when(casesService.referCase(refEq(caseWithStatusOPEN), any[String],any[Seq[ReferralReason]],any[FileUpload],
        any[String], refEq(operator))(any[HeaderCarrier])).thenReturn(successful(caseWithStatusREFERRED))

      val result: Result = await(controller(caseWithStatusOPEN).postReferCase("reference", None)(newFakePOSTRequestWithCSRF(fakeApplication)
        .withBody(aMultipartFileWithParams("referredTo" -> Seq("Other"), "note" -> Seq("some-note")))))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Enter who you are referring this case to")
    }

    "remove reasons when referred to is set to Lab Analyst" in {
      val captor = ArgumentCaptor.forClass(classOf[Seq[ReferralReason]])
      when(casesService.referCase(refEq(caseWithStatusOPEN), any[String],captor.capture(),any[FileUpload],
        any[String], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusREFERRED))

      val result: Result = await(controller(caseWithStatusOPEN).postReferCase("reference", None)(newFakePOSTRequestWithCSRF(fakeApplication)
        .withBody(aMultipartFileWithParams("referredTo" -> Seq("Lab Analyst"), "reasons[0]" -> Seq(ReferralReason.REQUEST_SAMPLE.toString),
          "reasons[1]" -> Seq(ReferralReason.REQUEST_MORE_INFO.toString), "note" -> Seq("some-note")))))

      assert(captor.getValue == Seq.empty)
      status(result) shouldBe Status.SEE_OTHER
    }

    "display required field when failing to submit a note" in {
      when(casesService.referCase(refEq(caseWithStatusOPEN), any[String],any[Seq[ReferralReason]],any[FileUpload],
        any[String], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusREFERRED))

      val result: Result = await(controller(caseWithStatusOPEN).postReferCase("reference", None)(newFakePOSTRequestWithCSRF(fakeApplication)
        .withBody(aMultipartFileWithParams("referredTo" -> Seq("LAB")))))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("Add a note giving details of why you are referring this case")
    }

    "return to form on missing file" in {
      val result: Result = await(controller(caseWithStatusOPEN).postReferCase("reference", None)
      (newFakePOSTRequestWithCSRF(fakeApplication).withBody(aEmptyMultipartFileWithParams())))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Referred")
    }

    "return to form on wrong type of file" in {
      val result: Result = await(controller(caseWithStatusOPEN).postReferCase("reference", None)
      (newFakePOSTRequestWithCSRF(fakeApplication).withBody(aMultipartFileOfType("audio/mpeg"))))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Referred")
    }

    "return to form on file size too large" in {
      val result: Result = await(controller(caseWithStatusOPEN).postReferCase("reference", None)
      (newFakePOSTRequestWithCSRF(fakeApplication).withBody(aMultipartFileOfLargeSize)))

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Referred")
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(caseWithStatusNEW, Set.empty)
        .postReferCase("reference", None)(newFakePOSTRequestWithCSRF(fakeApplication)
          .withBody(aMultipartFileWithParams("referredTo" -> Seq("APPLICANT"), "reason" -> Seq(ReferralReason.REQUEST_SAMPLE.toString), "note" -> Seq("some-note")))))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "View Confirm page for a referred case" should {

    "return OK and HTML content type" in {
      when(casesService.referCase(refEq(caseWithStatusREFERRED),any[String],any[Seq[ReferralReason]],any[FileUpload],
        any[String], any[Operator])(any[HeaderCarrier])).thenReturn(successful(caseWithStatusREFERRED))

      val result: Result = await(controller(caseWithStatusREFERRED).confirmReferCase("reference")
      (newFakePOSTRequestWithCSRF(fakeApplication)
        .withFormUrlEncodedBody("state" -> "true")))

      status(result) shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result) shouldBe Some("utf-8")
      bodyOf(result) should include("This case has been referred")
    }
  }

}
