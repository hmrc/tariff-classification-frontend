/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{Call, MultipartFormData, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future

class AttachmentsControllerSpec extends UnitSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {

  private def onwardRoute = Call("POST", "/foo")

  private val fakeRequest = FakeRequest(onwardRoute)

  private implicit def application: api.Application = new GuiceApplicationBuilder().build()
  private implicit val mtrlzr: Materializer = application.injector.instanceOf[Materializer]

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val fileService = mock[FileStoreService]
  private val operator = mock[Operator]
  private val event = mock[Event]

  private val controller = new AttachmentsController(
    new SuccessfulAuthenticatedAction(operator), casesService, fileService, messageApi, appConfig, mtrlzr
  )

  private implicit val hc: HeaderCarrier = HeaderCarrier()


  "Attachments Details" should {

    "return 200 OK and HTML content type" in {
      val aCase = Cases.btiCaseExample
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(Seq(Cases.storedAttachment, Cases.storedOperatorAttachment)))
      given(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(Some(Cases.letterOfAuthority)))

      val result = controller.attachmentsDetails("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 200 OK and HTML content type when no files are present" in {
      val aCase = Cases.btiCaseExample
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(Seq.empty))
      given(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = controller.attachmentsDetails("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 404 Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = controller.attachmentsDetails("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Attachment upload" should {

    val testReference = "test-reference"

    def aMultipartFile: MultipartFormData[TemporaryFile] = {
      val file = TemporaryFile("example-file.txt")
      val filePart = FilePart[TemporaryFile](key = "file-input", "file.txt", contentType = Some("text/plain"), ref = file)
      MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
    }

    "reload page when valid data is submitted" in {
      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)
      val updatedCase = aCase.copy(attachments = aCase.attachments :+ Cases.attachment("anyUrl"))

      val postRequest = fakeRequest.withBody(Right(aMultipartFile))
      val fileUpload = FileUpload(TemporaryFile("example-file.txt"), "file.txt", "text/plain")

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))
      given(fileService.upload(refEq(fileUpload))(any[HeaderCarrier])).willReturn(Future.successful(FileStoreAttachment("id", "file-name", "type", 0)))

      // When
      val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.AttachmentsController.attachmentsDetails(testReference).toString)
    }


    "show not found case page when non existing case is provided" in {
      //Given
      val postRequest = fakeRequest.withBody(Right(aMultipartFile))

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(Future.successful(None))

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

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(Seq.empty))
      given(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(None))

      // When
      val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe OK
      contentAsString(result) should include("You must select a file")
    }

    "file service fails while upload show expected message" in {
      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)
      val updatedCase = aCase.copy(attachments = aCase.attachments :+ Cases.attachment("anyUrl"))

      val postRequest = fakeRequest.withBody(Right(aMultipartFile))
      val fileUpload = FileUpload(TemporaryFile("example-file.txt"), "file.txt", "text/plain")

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))
      given(fileService.upload(refEq(fileUpload))(any[HeaderCarrier])).willReturn(Future.successful(FileStoreAttachment("id", "file-name", "type", 0)))

      // When
      val result: Result = await(controller.uploadAttachment(testReference)(postRequest))

      // Then
      status(result) shouldBe OK
      contentAsString(result) should include("file service has failed while uploading")
    }

  }

}
