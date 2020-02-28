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

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito.when
import org.scalatest.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api
import play.api.http.Status
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import config.AppConfig
import models.{Permission, _}
import service.{CasesService, FileStoreService}
import utils.Cases

import scala.concurrent.Future.successful

class AttachmentsControllerSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {
  private val fakeRequest = FakeRequest(onwardRoute)
  private val env = Environment.simple()

  private implicit val mtrlzr: Materializer = app.injector.instanceOf[Materializer]
  private val messageApi = inject[MessagesControllerComponents]
  private val appConfig = inject[AppConfig]
  private val casesService = mock[CasesService]
  private val fileService = mock[FileStoreService]
  private val operator = mock[Operator]

  private val controller = new AttachmentsController(
    new SuccessfulRequestActions(inject[BodyParsers.Default], operator, c = Cases.btiCaseExample), casesService, fileService, messageApi, appConfig, mtrlzr
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new AttachmentsController(
    new RequestActionsWithPermissions(inject[BodyParsers.Default], permission, c = requestCase), casesService, fileService, messageApi, appConfig, mtrlzr)

  private def onwardRoute = Call("POST", "/foo")

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "Attachments Details" should {

    "return 200 OK and HTML content type" in {
      val aCase = Cases.btiCaseExample
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(successful(Some(aCase)))
      given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(Seq(Cases.storedAttachment, Cases.storedOperatorAttachment)))
      given(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(Some(Cases.letterOfAuthority)))

      val result = await(controller(aCase,Set(Permission.ADD_ATTACHMENT)).attachmentsDetails("reference")(fakeRequest))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 200 OK and HTML content type when no files are present" in {
      val aCase = Cases.btiCaseExample
      givenACaseWithNoAttachmentsAndNoLetterOfAuthority("reference", aCase)

      val result = await(controller(aCase,Set(Permission.ADD_ATTACHMENT)).attachmentsDetails("reference")(fakeRequest))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 404 Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(successful(None))

      val result = await(controller.attachmentsDetails("reference")(fakeRequest))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Attachment upload" should {

    val testReference = "test-reference"

    def aMultipartFile: MultipartFormData[TemporaryFile] = {
      val file = SingletonTemporaryFileCreator.create("example-file.txt")
      val filePart = FilePart[TemporaryFile](key = "file-input", "file.txt", contentType = Some("text/plain"), ref = file)
      MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
    }

    def aEmptyNameMultipartFile: MultipartFormData[TemporaryFile] = {
      val filePart = FilePart[TemporaryFile](key = "file-input", "", contentType = Some("text/plain"), ref = SingletonTemporaryFileCreator.create("example-file.txt"))
      MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
    }

    def aMultipartFileOfType(mimeType: String): MultipartFormData[TemporaryFile] = {
      val file = SingletonTemporaryFileCreator.create("example-file")
      val filePart = FilePart[TemporaryFile](key = "file-input", "example-file", contentType = Some(mimeType), ref = file)
      MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
    }

    "reload page when valid data is submitted" in {
      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)
      val updatedCase = aCase.copy(attachments = aCase.attachments :+ Cases.attachment("anyUrl"))

      val postRequest = fakeRequest.withBody(Right(aMultipartFile))
      val fileUpload = FileUpload(SingletonTemporaryFileCreator.create("example-file.txt"), "file.txt", "text/plain")

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(successful(Some(aCase)))
      given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(updatedCase))
      given(casesService.addAttachment(any[Case], any[FileUpload], any[Operator])(any[HeaderCarrier])).willReturn(successful(updatedCase))
      given(fileService.upload(refEq(fileUpload))(any[HeaderCarrier])).willReturn(successful(FileStoreAttachment("id", "file-name", "type", 0)))

      // When
      val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.AttachmentsController.attachmentsDetails(testReference).toString)
    }

    "show not found case page when non existing case is provided" in {
      //Given
      val postRequest = fakeRequest.withBody(Right(aMultipartFile))

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(successful(None))

      // When
      val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

    "show file required error message when no file provided" in {
      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)

      val form = MultipartFormData[TemporaryFile](dataParts = Map.empty, files = Seq.empty, badParts = Seq.empty)
      val postRequest = fakeRequest.withBody(Right(form))

      givenACaseWithNoAttachmentsAndNoLetterOfAuthority(testReference, aCase)

      // When
      val result: Result = await(controller(aCase, Set(Permission.ADD_ATTACHMENT)).uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe OK
      contentAsString(result) should include("error-summary")
      contentAsString(result) should include("Select a file to upload")
    }


    "show file required error message when a empty file is provided" in {
      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)
      val postRequest = fakeRequest.withBody(Right(aEmptyNameMultipartFile))

      givenACaseWithNoAttachmentsAndNoLetterOfAuthority(testReference, aCase)

      // When
      val result: Result = await(controller(aCase, Set(Permission.ADD_ATTACHMENT)).uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe OK
      contentAsString(result) should include("Select a file to upload")
    }

    "upload a file higher than the size permitted shows expected error" in {
      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)

      val postRequest = FakeRequest().withBody(Left(MaxSizeExceeded(1)))

      givenACaseWithNoAttachmentsAndNoLetterOfAuthority(testReference, aCase)

      // When
      val result = await(controller(aCase, Set(Permission.ADD_ATTACHMENT)).uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe OK
      contentAsString(result) should include("Your file will not upload")
    }

    "upload a file of wrong type shows expected error" in {
      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)

      val postRequest = FakeRequest().withBody(Right(aMultipartFileOfType("audio/mpeg")))

      givenACaseWithNoAttachmentsAndNoLetterOfAuthority(testReference, aCase)

      // When
      val result = await(controller(aCase, Set(Permission.ADD_ATTACHMENT)).uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe OK
      contentAsString(result) should include("not a valid file type")
    }

    "upload a file of valid type should reload page" in {

      appConfig.fileUploadMimeTypes foreach { mimeType =>
        //Given
        val aCase = Cases.btiCaseExample.copy(reference = testReference)
        val updatedCase = aCase.copy(attachments = aCase.attachments :+ Cases.attachment("anyUrl"))

        val postRequest = fakeRequest.withBody(Right(aMultipartFileOfType(mimeType)))

        given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(successful(Some(aCase)))
        given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(updatedCase))
        given(casesService.addAttachment(any[Case], any[FileUpload], any[Operator])(any[HeaderCarrier])).willReturn(successful(updatedCase))
        given(fileService.upload(any[FileUpload])(any[HeaderCarrier])).willReturn(successful(FileStoreAttachment("id", "file-name", "type", 0)))

        // When
        val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

        // Then
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.AttachmentsController.attachmentsDetails(testReference).toString)
      }
    }

    "file service fails while upload show expected message" in {
      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)
      val updatedCase = aCase.copy(attachments = aCase.attachments :+ Cases.attachment("anyUrl"))

      val postRequest = fakeRequest.withBody(Right(aMultipartFile))
      val fileUpload = FileUpload(SingletonTemporaryFileCreator.create("example-file.txt"), "file.txt", "text/plain")

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(successful(Some(aCase)))
      given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(updatedCase))
      given(casesService.addAttachment(any[Case], any[FileUpload], any[Operator])(any[HeaderCarrier])).willReturn(successful(updatedCase))
      given(fileService.upload(refEq(fileUpload))(any[HeaderCarrier])).willReturn(successful(FileStoreAttachment("id", "file-name", "type", 0)))

      // When
      val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.AttachmentsController.attachmentsDetails(testReference).toString)
    }

  }

  "Remove attachment" should {

    val aCase = Cases.btiCaseExample

    "return OK when user has correct permissions" in {
      val result: Result = await(controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
        .removeAttachment(aCase.reference, "reference", "some-file.jpg")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Are you sure you want to remove some-file.jpg from this case?")
    }

    "redirect unauthorised when does not have correct permissions" in {
      val result: Result = await(controller(aCase, Set.empty)
        .removeAttachment(aCase.reference, "reference", "some-file.jpg")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Confirm Remove Attachment" should {

    val aCase = Cases.btiCaseExample

    "redirect to attachments tab when user selects `yes`" in {
      when(casesService.removeAttachment(any[Case], any[String])(any[HeaderCarrier])).thenReturn(successful(aCase))

      val result: Result = await(controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
        .confirmRemoveAttachment(aCase.reference, "fileId", "some-file.jpg")
        (newFakePOSTRequestWithCSRF(fakeApplication)
          .withFormUrlEncodedBody("state" -> "true")))

      redirectLocation(result) shouldBe Some("/manage-tariff-classifications/cases/1/attachments")
    }

    "redirect to attachments tab when user selects `no`" in {
      val result: Result = await(controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
        .confirmRemoveAttachment(aCase.reference, "reference", "some-file.jpg")
        (newFakePOSTRequestWithCSRF(fakeApplication)
          .withFormUrlEncodedBody("state" -> "false")))

      redirectLocation(result) shouldBe Some("/manage-tariff-classifications/cases/1/attachments")
    }

    "redirect back to confirm remove view on form error" in {
      when(casesService.removeAttachment(any[Case], any[String])(any[HeaderCarrier])).thenReturn(successful(aCase))

      val result: Result = await(controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
        .confirmRemoveAttachment(aCase.reference, "fileId", "some-file.jpg")
        (newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentAsString(result) should include("Select yes if you want to remove the attachment")
    }

  }

  private def givenACaseWithNoAttachmentsAndNoLetterOfAuthority(testReference: String, aCase: Case) = {
    given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(successful(Some(aCase)))
    given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(Seq.empty))
    given(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(None))
  }

}
