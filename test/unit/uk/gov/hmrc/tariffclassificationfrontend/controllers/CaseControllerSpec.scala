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

import java.time.Clock

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.DecisionFormMapper
import uk.gov.hmrc.tariffclassificationfrontend.models.{Event, Operator}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, EventsService, FileStoreService}
import uk.gov.tariffclassificationfrontend.utils.{Cases, Events}

import scala.concurrent.Future

class CaseControllerSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {

  private val fakeRequest = FakeRequest()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val fileService = mock[FileStoreService]
  private val mapper = mock[DecisionFormMapper]
  private val eventService = mock[EventsService]
  private val operator = mock[Operator]
  private val event = mock[Event]

  private val controller = new CaseController(new SuccessfulAuthenticatedAction(operator),
                                              casesService, fileService, eventService, mapper, messageApi, appConfig)

  private implicit val hc = HeaderCarrier()

  "Case Summary" should {

    "return 200 OK and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(Cases.btiCaseExample)))

      val result = controller.summary("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 404 Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = controller.summary("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }

  "Application Details" should {
    val aCase = Cases.btiCaseExample
    val attachment = Cases.storedAttachment

    "return 200 OK and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))
      given(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(Some(attachment)))

      val result = controller.applicationDetails("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 404 Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = controller.applicationDetails("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }

  "Ruling Details" should {

    "return 200 OK and HTML content type" in {
      val aCase = Cases.btiCaseExample
      val attachment = Cases.storedAttachment
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

      val result = controller.rulingDetails("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 404 Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = controller.rulingDetails("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }

  "Activity Details" should {

    "return 200 OK and HTML content type" in {
      val aCase = Cases.btiCaseExample
      given(casesService.getOne(refEq(aCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(eventService.getEvents(refEq(aCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(Events.events))

      val result = controller.activityDetails(aCase.reference)(newFakeGETRequestWithCSRF(app))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 200 OK and HTML content type when no Events are present" in {
      val aCase = Cases.btiCaseExample
      given(casesService.getOne(refEq(aCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(eventService.getEvents(refEq(aCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(Seq()))

      val result = controller.activityDetails(aCase.reference)(newFakeGETRequestWithCSRF(app))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 404 Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = controller.activityDetails("reference")(fakeRequest)

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

  }


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

  "Activity: Add Note" should {
    val aCase = Cases.btiCaseExample

    "add a new note when a case note is provided" in {
      val aNote = "This is a note"
      val aValidForm = newFakePOSTRequestWithCSRF(app, Map("note" -> aNote))
      given(casesService.getOne(refEq(aCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(eventService.addNote(refEq(aCase), refEq(aNote), refEq(operator), any[Clock])(any[HeaderCarrier]))
        .willReturn(Future.successful(event))

      val result = await(controller.addNote(aCase.reference)(aValidForm))
      locationOf(result) shouldBe Some("/tariff-classification/cases/1/activity")
    }

    "displays an error when no case note is provided" in {
      val aValidForm = newFakePOSTRequestWithCSRF(app, Map())
      given(casesService.getOne(refEq(aCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(Some(aCase)))
      given(eventService.getEvents(refEq(aCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(Seq()))

      val result = controller.addNote(aCase.reference)(aValidForm)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("This field is required")
    }

    "displays case not found message" in {
      val aValidForm = newFakePOSTRequestWithCSRF(app, Map("note" -> "note"))
      given(casesService.getOne(refEq(aCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = controller.addNote(aCase.reference)(aValidForm)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }
  }

}
