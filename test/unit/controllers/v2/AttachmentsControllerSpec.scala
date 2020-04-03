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

package controllers.v2

import play.api.mvc.Results.Ok
import akka.stream.Materializer
import config.AppConfig
import controllers.v2.routes.LiabilityController
import controllers.{RequestActionsWithPermissions, SuccessfulRequestActions}
import models.request.AuthenticatedRequest
import models.{Permission, _}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.{ArgumentMatcher, ArgumentMatchers, Mockito}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Environment
import play.api.http.Status
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases
import views.html.v2.partials.attachments_details
import views.html.v2.{case_heading, liability_view, remove_attachment}
import views.html._
import views.html.partials.error_summary

import scala.concurrent.Future.successful

class AttachmentsControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {
  lazy val messageApi = inject[MessagesControllerComponents]

  lazy val mtrlzr: Materializer = inject[Materializer]
  lazy val appConfig = inject[AppConfig]
  lazy val casesService = mock[CasesService]
  lazy val fileService = mock[FileStoreService]
  lazy val operator = mock[Operator]
  lazy val liabilityController = mock[LiabilityController]
  lazy val attachments_details = mock[attachments_details]
  lazy val remove_attachment = mock[remove_attachment]
  private val fakeRequest = FakeRequest(onwardRoute)

  def controller: AttachmentsController = {
    new AttachmentsController(
      verify = new SuccessfulRequestActions(inject[BodyParsers.Default], mock[Operator], c = Cases.btiCaseExample),
      casesService = casesService,
      fileService = fileService,
      mcc = messageApi,
      liabilityController = liabilityController,
      remove_attachment = remove_attachment,
      appConfig = appConfig,
      mat = mtrlzr
    )
  }

  def controller(requestCase: Case, permission: Set[Permission]): AttachmentsController = {
    new AttachmentsController(
      verify = new RequestActionsWithPermissions(inject[BodyParsers.Default], permission, c = requestCase),
      casesService = casesService,
      fileService = fileService,
      mcc = messageApi,
      liabilityController = liabilityController,
      remove_attachment = remove_attachment,
      appConfig = appConfig,
      mat = mtrlzr
    )
  }

  override protected def beforeEach(): Unit = {
    reset(remove_attachment)
  }

  private implicit val hc: HeaderCarrier = HeaderCarrier()

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

      when(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).thenReturn(successful(Some(aCase)))
      when(casesService.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(updatedCase))
      when(casesService.addAttachment(any[Case], any[FileUpload], any[Operator])(any[HeaderCarrier])).thenReturn(successful(updatedCase))
      when(fileService.upload(refEq(fileUpload))(any[HeaderCarrier])).thenReturn(successful(FileStoreAttachment("id", "file-name", "type", 0)))

      // When
      val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(LiabilityController.displayLiability(testReference).toString)
    }

    "show not found case page when non existing case is provided" in {
      //Given
      val postRequest = fakeRequest.withBody(Right(aMultipartFile))

      when(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).thenReturn(successful(None))

      // When
      val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

    "upload a file of valid type should reload page" in {

      appConfig.fileUploadMimeTypes foreach { mimeType =>
        //Given
        val aCase = Cases.btiCaseExample.copy(reference = testReference)
        val updatedCase = aCase.copy(attachments = aCase.attachments :+ Cases.attachment("anyUrl"))

        val postRequest = fakeRequest.withBody(Right(aMultipartFileOfType(mimeType)))

        when(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).thenReturn(successful(Some(aCase)))
        when(casesService.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(updatedCase))
        when(casesService.addAttachment(any[Case], any[FileUpload], any[Operator])(any[HeaderCarrier])).thenReturn(successful(updatedCase))
        when(fileService.upload(any[FileUpload])(any[HeaderCarrier])).thenReturn(successful(FileStoreAttachment("id", "file-name", "type", 0)))

        // When
        val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

        // Then
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(LiabilityController.displayLiability(testReference).toString)
      }
    }

    "file service fails while upload show expected message" in {
      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)
      val updatedCase = aCase.copy(attachments = aCase.attachments :+ Cases.attachment("anyUrl"))

      val postRequest = fakeRequest.withBody(Right(aMultipartFile))
      val fileUpload = FileUpload(SingletonTemporaryFileCreator.create("example-file.txt"), "file.txt", "text/plain")

      when(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).thenReturn(successful(Some(aCase)))
      when(casesService.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(updatedCase))
      when(casesService.addAttachment(any[Case], any[FileUpload], any[Operator])(any[HeaderCarrier])).thenReturn(successful(updatedCase))
      when(fileService.upload(refEq(fileUpload))(any[HeaderCarrier])).thenReturn(successful(FileStoreAttachment("id", "file-name", "type", 0)))

      // When
      val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(LiabilityController.displayLiability(testReference).toString)
    }

  }

  "Remove attachment" should {

    val aCase = Cases.btiCaseExample

    "return OK when user has correct permissions" in {
      givenACaseWithNoAttachmentsAndNoLetterOfAuthority(aCase.reference, aCase)

      val result: Result = await(controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
        .removeAttachment(aCase.reference, "reference", "some-file.jpg")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
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

      redirectLocation(result) shouldBe Some("/manage-tariff-classifications/cases/v2/1/liability")
    }

    "redirect to attachments tab when user selects `no`" in {
      val result: Result = await(controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
        .confirmRemoveAttachment(aCase.reference, "reference", "some-file.jpg")
        (newFakePOSTRequestWithCSRF(fakeApplication)
          .withFormUrlEncodedBody("state" -> "false")))

      redirectLocation(result) shouldBe Some("/manage-tariff-classifications/cases/v2/1/liability")
    }

    "redirect back to confirm remove view on form error" in {
      givenACaseWithNoAttachmentsAndNoLetterOfAuthority(aCase.reference, aCase)
      when(casesService.removeAttachment(any[Case], any[String])(any[HeaderCarrier])).thenReturn(successful(aCase))

      val result: Result = await(controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
        .confirmRemoveAttachment(aCase.reference, "fileId", "some-file.jpg")
        (newFakePOSTRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      verify(remove_attachment, times(1)).apply(
        any(),
        any(),
        anyString(),
        anyString()
      )(any(), any(), any())
    }

  }

  private def onwardRoute = Call("POST", "/foo")

  private def givenACaseWithNoAttachmentsAndNoLetterOfAuthority(testReference: String, aCase: Case) = {
    when(remove_attachment.apply(any(), any(), anyString(), anyString())(any(), any(), any())).thenReturn(Html("heading"))
    //    when(inject[views.html.partials.error_summary].apply(any())).thenReturn(Html(""))

    when(liabilityController.buildLiabilityView(refEq(testReference), any())(any())).thenReturn(successful(Ok("Ok")))
    when(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).thenReturn(successful(Some(aCase)))
    when(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).thenReturn(successful(Seq.empty))
    when(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).thenReturn(successful(None))
  }

}
