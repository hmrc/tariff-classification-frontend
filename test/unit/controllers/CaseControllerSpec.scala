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

import java.time.Clock

import models.EventType.EventType
import models.forms.{CommodityCodeConstraints, DecisionForm}
import models.{Permission, _}
import org.mockito.ArgumentMatchers.{any, anyString, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito.verify
import play.api.http.Status
import play.api.test.Helpers._
import service._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._
import utils.{Cases, Events}

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

class CaseControllerSpec extends ControllerBaseSpec {

  private val keywordsService      = mock[KeywordsService]
  private val fileService          = mock[FileStoreService]
  private val eventService         = mock[EventsService]
  private val queueService         = mock[QueuesService]
  private val operator             = Operator(id = "id")
  private val event                = mock[Event]
  private val commodityCodeService = mock[CommodityCodeService]
  private val decisionForm         = new DecisionForm(new CommodityCodeConstraints(commodityCodeService, realAppConfig))
  private val countriesService     = new CountriesService

  private def controller(c: Case) = new CaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = c),
    mock[CasesService],
    keywordsService,
    fileService,
    eventService,
    queueService,
    commodityCodeService,
    decisionForm,
    countriesService,
    mcc,
    realAppConfig
  )

  private def controller(c: Case, permission: Set[Permission]) = new CaseController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = c),
    mock[CasesService],
    keywordsService,
    fileService,
    eventService,
    queueService,
    commodityCodeService,
    decisionForm,
    countriesService,
    mcc,
    realAppConfig
  )

  private def controllerWithoutNewLiability(c: Case) = new CaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = c),
    mock[CasesService],
    keywordsService,
    fileService,
    eventService,
    queueService,
    commodityCodeService,
    decisionForm,
    countriesService,
    mcc,
    appConfWithLiabilityToggleOff
  )

  "Case Index" should {
    "redirect to default tab" when {
      "case is a BTI" in {
        val c = aCase(withReference("reference"), withBTIApplication)

        val result = await(controller(c).get("reference")(fakeRequest))

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.CaseController.applicantDetails("reference").url)
      }

      "case is a Liability with newLiabilityDetails toggle is set to false" in {
        val c      = aCase(withReference("reference"), withLiabilityApplication())
        val result = controllerWithoutNewLiability(c).get("reference")(fakeRequest)

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(routes.LiabilityController.liabilityDetails("reference").url)
      }

      "case is a Liability with newLiability toggle is set to true" in {
        val c      = aCase(withReference("reference"), withLiabilityApplication())
        val result = controller(c).get("reference")(fakeRequest)

        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some(v2.routes.LiabilityController.displayLiability("reference").url)
      }
    }
  }

  "Case Trader" should {

    "return 200 OK and HTML content type" in {
      val aCase      = Cases.btiCaseExample
      val attachment = Cases.storedAttachment

      given(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(Some(attachment)))

      val result = controller(Cases.btiCaseExample).applicantDetails("reference")(fakeRequest)

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }
  }

  "Application Details" should {
    val aCase      = Cases.btiCaseExample
    val attachment = Cases.storedAttachment

    "return 200 OK and HTML content type" in {
      given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(Seq(attachment)))
      given(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(Some(attachment)))

      val result = controller(aCase).itemDetails("reference")(fakeRequest)

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

  }

  "Ruling Details" should {

    "return 200 OK and HTML content type" in {
      val aCase      = Cases.btiCaseExample
      val attachment = Cases.storedAttachment
      given(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(Seq(attachment)))
      given(commodityCodeService.find(anyString())).willReturn(None)

      val result = controller(aCase).rulingDetails("reference")(fakeRequest)

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

  }

  "Sample Details" should {

    "return 200 OK and HTML content type" in {
      val aCase = Cases.btiCaseExample

      given(
        eventService.getFilteredEvents(refEq(aCase.reference), refEq(NoPagination()), any[Option[Set[EventType]]])(
          any[HeaderCarrier]
        )
      ) willReturn successful(Paged.empty[Event])

      val result = controller(aCase).sampleDetails(aCase.reference)(newFakeGETRequestWithCSRF(app))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

      verify(eventService).getFilteredEvents(
        refEq(aCase.reference),
        refEq(NoPagination()),
        refEq(Some(EventType.sampleEvents))
      )(any[HeaderCarrier])
    }
  }

  "Activity Details" should {

    "return 200 OK and HTML content type" in {
      val aCase = Cases.btiCaseExample

      given(
        eventService.getFilteredEvents(refEq(aCase.reference), refEq(NoPagination()), any[Option[Set[EventType]]])(
          any[HeaderCarrier]
        )
      ) willReturn successful(Paged(Events.events))

      given(queueService.getAll) willReturn successful(Seq.empty)

      val result = controller(aCase).activityDetails(aCase.reference)(newFakeGETRequestWithCSRF(app))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

      verify(eventService).getFilteredEvents(
        refEq(aCase.reference),
        refEq(NoPagination()),
        refEq(Some(EventType.values.diff(EventType.sampleEvents)))
      )(any[HeaderCarrier])
    }

    "return 200 OK and HTML content type when no Events are present" in {
      val aCase = Cases.btiCaseExample

      given(
        eventService.getFilteredEvents(refEq(aCase.reference), refEq(NoPagination()), any[Option[Set[EventType]]])(
          any[HeaderCarrier]
        )
      ) willReturn successful(Paged.empty[Event])
      given(queueService.getAll) willReturn successful(Seq.empty)

      val result = controller(aCase).activityDetails(aCase.reference)(newFakeGETRequestWithCSRF(app))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

  }

  "Activity: Add Note" should {
    val aCase = Cases.btiCaseExample

    "add a new note when a case note is provided" in {
      val aNote      = "This is a note"
      val aValidForm = newFakePOSTRequestWithCSRF(app, Map("note" -> aNote))
      given(eventService.addNote(refEq(aCase), refEq(aNote), refEq(operator), any[Clock])(any[HeaderCarrier])) willReturn successful(
        event
      )

      val result = await(controller(aCase).addNote(aCase.reference)(aValidForm))
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/1/activity")
    }

    "displays an error when no case note is provided" in {
      val aValidForm = newFakePOSTRequestWithCSRF(app)
      given(eventService.getEvents(refEq(aCase.reference), refEq(NoPagination()))(any[HeaderCarrier])) willReturn successful(
        Paged.empty[Event]
      )
      given(queueService.getAll) willReturn successful(Seq.empty)

      val result = controller(aCase, Set(Permission.ADD_NOTE)).addNote(aCase.reference)(aValidForm)
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("error-summary")
      contentAsString(result) should include("Enter a case note")
    }

    "return OK when user has right permissions" in {
      val aCase = Cases.btiCaseExample
      given(eventService.getEvents(refEq(aCase.reference), refEq(NoPagination()))(any[HeaderCarrier])) willReturn successful(
        Paged(Events.events)
      )
      given(queueService.getAll) willReturn successful(Seq.empty)

      val result = controller(aCase, Set(Permission.ADD_NOTE)).addNote(aCase.reference)(newFakeGETRequestWithCSRF(app))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val aCase = Cases.btiCaseExample

      val result = controller(aCase, Set.empty).addNote(aCase.reference)(newFakeGETRequestWithCSRF(app))

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Keywords Details" should {

    "return 200 OK and HTML content type" in {
      val aCase = Cases.btiCaseExample
      given(keywordsService.autoCompleteKeywords).willReturn(successful(Seq()))

      val result = controller(aCase).keywordsDetails(aCase.reference)(newFakeGETRequestWithCSRF(app))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }
  }

  "Keywords: Add keyword" should {
    val aCase = Cases.btiCaseExample

    "add a new keyword" in {
      val aKeyword   = "Apples"
      val aValidForm = newFakePOSTRequestWithCSRF(app, Map("keyword" -> aKeyword))
      given(keywordsService.addKeyword(refEq(aCase), refEq("Apples"), refEq(operator))(any[HeaderCarrier]))
        .willReturn(successful(aCase))
      given(keywordsService.autoCompleteKeywords).willReturn(successful(Seq()))

      val result = controller(aCase).addKeyword(aCase.reference)(aValidForm)
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("Keywords")
    }

    "displays an error when no keyword is provided" in {
      val aValidForm = newFakePOSTRequestWithCSRF(app)
      given(keywordsService.autoCompleteKeywords).willReturn(successful(Seq()))

      val result = controller(aCase, Set(Permission.KEYWORDS)).addKeyword(aCase.reference)(aValidForm)
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("error-summary")
      contentAsString(result) should include("Enter a keyword")
    }

    "return OK when user has right permissions" in {
      val aKeyword   = "Apples"
      val aValidForm = newFakePOSTRequestWithCSRF(app, Map("keyword" -> aKeyword))
      given(keywordsService.addKeyword(any[Case], any[String], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(aCase))
      given(keywordsService.autoCompleteKeywords).willReturn(successful(Seq()))

      val result = controller(aCase, Set(Permission.KEYWORDS)).addKeyword(aCase.reference)(aValidForm)

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val aValidForm = newFakePOSTRequestWithCSRF(app)
      val result     = controller(aCase, Set.empty).addKeyword(aCase.reference)(aValidForm)

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Keywords: Remove keyword" should {
    val aCase    = Cases.btiCaseExample
    val aKeyword = "Apples"

    "remove an existing keyword" in {
      given(keywordsService.removeKeyword(refEq(aCase), refEq("Apples"), refEq(operator))(any[HeaderCarrier]))
        .willReturn(successful(aCase))

      val result = controller(aCase).removeKeyword(aCase.reference, aKeyword)(newFakeGETRequestWithCSRF(app))
      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include("Keywords")
    }

    "return OK when user has right permissions" in {
      given(keywordsService.removeKeyword(any[Case], any[String], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(aCase))

      val result = controller(aCase, Set(Permission.KEYWORDS))
        .removeKeyword(aCase.reference, aKeyword)(newFakeGETRequestWithCSRF(app))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result = controller(aCase, Set.empty).removeKeyword(aCase.reference, aKeyword)(newFakeGETRequestWithCSRF(app))

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "return a valid country when given a valid country code" in {
    val result: Option[String] = controller(aCase(withReference("withReference"))).getCountryName("IE")

    result shouldBe Some("title.ireland")
  }

}
