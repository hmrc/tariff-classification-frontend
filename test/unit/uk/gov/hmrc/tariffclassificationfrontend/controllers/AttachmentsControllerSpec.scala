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

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{Call, MultipartFormData}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Event, FileStoreAttachment, Operator}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future

class AttachmentsControllerSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {

  private def onwardRoute = Call("POST", "/foo")

  private val fakeRequest = FakeRequest(onwardRoute)

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val fileService = mock[FileStoreService]
  private val operator = mock[Operator]
  private val event = mock[Event]

  private val controller = new AttachmentsController(new SuccessfulAuthenticatedAction(operator),
    casesService, fileService, messageApi, appConfig)

  private implicit val hc = HeaderCarrier()


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

    def prepareValidForm = {
      val file = TemporaryFile("example-file.txt")
      val filePart = FilePart[TemporaryFile](key = "file-input", "file.txt", contentType = Some("text/plain"), ref = file)
      MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
    }


    "reload page when valid data is submitted" in {

      //Given

      val aCase = Cases.btiCaseExample.copy(reference = testReference)
      val updatedCase = aCase.copy(attachments = aCase.attachments :+ Cases.createAttachment("anyUrl"))

      val file = TemporaryFile("example-file.txt")
      val filePart = FilePart[TemporaryFile](key = "file-input", "file.txt", contentType = Some("text/plain"), ref = file)
      val form = MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(filePart), badParts = Seq.empty)
      val postRequest = fakeRequest.withBody(form)

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))
      given(fileService.upload(refEq(filePart))(any[HeaderCarrier])).willReturn(Future.successful(FileStoreAttachment("id", "file-name", "type", 0)))

      // When
      val result = controller.uploadAttachment(testReference)(postRequest)

      // Then
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.AttachmentsController.attachmentsDetails(testReference).toString)
    }


    "show not found case page when non existing case is provided" in {

      //Given
      val postRequest = fakeRequest.withBody(prepareValidForm)

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(Future.successful(None))

      // When
      val result = controller.uploadAttachment(testReference)(postRequest)

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
      val postRequest = fakeRequest.withBody(form)

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(Seq.empty))
      given(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(None))

      // When
      val result = controller.uploadAttachment(testReference)(postRequest)

      // Then
      status(result) shouldBe OK
      contentAsString(result) should include("no file provided.")
    }


    "without file driver to attachments page with error" in {

      //Given
      val aCase = Cases.btiCaseExample.copy(reference = testReference)
      val updatedCase = aCase.copy(attachments = aCase.attachments :+ Cases.createAttachment("anyUrl"))
      val returnedAttachments = Seq(Cases.storedAttachment, Cases.storedOperatorAttachment)

      val form = MultipartFormData[TemporaryFile](dataParts = Map(), files = Seq(), badParts = Seq.empty)
      val postRequest = fakeRequest.withBody(form)

      given(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(fileService.upload(any[FilePart[TemporaryFile]])(any[HeaderCarrier])).willReturn(Future.successful(FileStoreAttachment("id", "file-name", "type", 0)))
      given(casesService.updateCase(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))
      given(fileService.getAttachments(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(returnedAttachments))
      given(fileService.getLetterOfAuthority(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(Some(Cases.letterOfAuthority)))

      // When
      val result = controller.uploadAttachment(testReference)(postRequest)

      // Then
      status(result) shouldBe OK

    }
  }

}
