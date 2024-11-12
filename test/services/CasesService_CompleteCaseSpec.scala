/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import audit.AuditService
import config.AppConfig
import connectors.{BindingTariffClassificationConnector, RulingConnector}
import models._
import models.request.NewEventRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{never, reset, verify, verifyNoMoreInteractions}
import org.scalatest.BeforeAndAfterEach
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.templates.{cover_letter_template, ruling_template}

import java.time._
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class CasesService_CompleteCaseSpec extends ServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val manyCases             = mock[Seq[Case]]
  private val oneCase               = mock[Option[Case]]
  private val queue                 = mock[Queue]
  private val connector             = mock[BindingTariffClassificationConnector]
  private val rulingConnector       = mock[RulingConnector]
  private val emailService          = mock[EmailService]
  private val reportingService      = mock[ReportingService]
  private val fileStoreService      = mock[FileStoreService]
  private val countriesService      = injector.instanceOf[CountriesService]
  private val pdfService            = mock[PdfService]
  private val audit                 = mock[AuditService]
  private val config                = mock[AppConfig]
  private val cover_letter_template = mock[cover_letter_template]
  private val ruling_template       = mock[ruling_template]
  private val clock      = Clock.fixed(LocalDateTime.of(2018, 1, 1, 14, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
  private val aBTI       = Cases.btiCaseExample
  private val aLiability = Cases.liabilityCaseExample

  private val service =
    new CasesService(
      audit,
      emailService,
      fileStoreService,
      countriesService,
      reportingService,
      pdfService,
      connector,
      rulingConnector,
      cover_letter_template,
      ruling_template
    )(executionContext, config)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(connector)
    reset(audit)
    reset(queue)
    reset(oneCase)
    reset(manyCases)
    reset(config)
    reset(emailService)

    given(config.clock).willReturn(clock)
  }

  "Complete Case" should {
    "update case status to COMPLETED" when {
      "Liability" in {

        val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
        val originalDecision   = Decision("code", None, None, "justification", "goods")
        val originalCase       = aLiability.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))
        val updatedDecision =
          Decision("code", Some(date("2018-01-01")), Some(date("2019-01-01")), "justification", "goods")
        val caseUpdated = aLiability.copy(status = CaseStatus.COMPLETED, decision = Some(updatedDecision))

        given(config.decisionLifetimeYears).willReturn(1)
        given(fileStoreService.upload(any[FileUpload])(any[HeaderCarrier])).willReturn(
          successful(FileStoreAttachment("id", s"LiabilityDecision_${originalCase.reference}", "application/pdf", 0L))
        )
        given(pdfService.generatePdf(any[Html]))
          .willReturn(successful(PdfFile(Array.emptyByteArray)))
        given(pdfService.generateFopPdf(any[Html], any[String]))
          .willReturn(successful(PdfFile(Array.emptyByteArray)))
        given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
        given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
          .willReturn(successful(mock[Event]))

        await(service.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

        verify(audit).auditCaseCompleted(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])
        verify(emailService, never()).sendCaseCompleteEmail(any[Case], refEq(operator))(any[HeaderCarrier])
        verify(rulingConnector, never()).notify(refEq(originalCase.reference))(any[HeaderCarrier])

        val caseUpdating = theCaseUpdating(connector)
        caseUpdating.status shouldBe CaseStatus.COMPLETED

        val eventCreated = theEventCreatedFor(connector, caseUpdated)
        eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))
        eventCreated.details  shouldBe CompletedCaseStatusChange(CaseStatus.OPEN, None, None)
      }

      "BTI" in {

        val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
        val originalDecision   = Decision("code", None, None, "justification", "goods")
        val originalCase       = aBTI.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))
        val updatedDecision =
          Decision("code", Some(date("2018-01-01")), Some(date("2019-01-01")), "justification", "goods")
        val caseUpdated           = aBTI.copy(status = CaseStatus.COMPLETED, decision = Some(updatedDecision))
        val emailTemplate         = EmailTemplate("plain", "html", "from", "subject", "services")
        val updatedEndDateInstant = Some(date("2020-12-31"))

        given(config.decisionLifetimeYears).willReturn(3)
        given(config.decisionLifetimeDays).willReturn(1)
        given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
        given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
          .willReturn(successful(mock[Event]))
        given(emailService.sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier]))
          .willReturn(Future.successful(emailTemplate))
        given(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier]))
          .willReturn(Future.successful(()))

        await(service.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

        verify(audit).auditCaseCompleted(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])
        verify(emailService).sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

        val caseUpdating = theCaseUpdating(connector)
        caseUpdating.status                        shouldBe CaseStatus.COMPLETED
        caseUpdating.decision.get.effectiveEndDate shouldBe updatedEndDateInstant

        val eventCreated = theEventCreatedFor(connector, caseUpdated)
        eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))
        eventCreated.details shouldBe CompletedCaseStatusChange(
          CaseStatus.OPEN,
          None,
          Some("- Subject: subject\n- Body: plain")
        )
      }

      "set the effective end date of an ATaR if an explict end date is defined by the user" in {

        val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
        val atarCase = aBTI.copy(
          status = CaseStatus.OPEN,
          decision = Some(Decision("code", None, Some(date("2022-01-01")), "justification", "goods"))
        )

        val emailTemplate         = EmailTemplate("plain", "html", "from", "subject", "services")
        val updatedEndDateInstant = Some(date("2022-01-01"))

        given(connector.updateCase(any[Case])(any[HeaderCarrier]))
          .willReturn(successful(atarCase))
        given(connector.createEvent(refEq(atarCase), any[NewEventRequest])(any[HeaderCarrier]))
          .willReturn(successful(mock[Event]))
        given(emailService.sendCaseCompleteEmail(refEq(atarCase), refEq(operator))(any[HeaderCarrier]))
          .willReturn(Future.successful(emailTemplate))
        given(rulingConnector.notify(refEq(atarCase.reference))(any[HeaderCarrier]))
          .willReturn(Future.successful(()))

        await(service.completeCase(atarCase, operator)(hc, messages))

        val caseUpdating = theCaseUpdating(connector)
        caseUpdating.status                        shouldBe CaseStatus.COMPLETED
        caseUpdating.decision.get.effectiveEndDate shouldBe updatedEndDateInstant
      }
    }

    "reject case without a decision" in {

      val operator: Operator = Operator("operator-id")
      val originalCase       = aBTI.copy(status = CaseStatus.OPEN, decision = None)

      intercept[IllegalArgumentException] {
        await(service.completeCase(originalCase, operator)(hc, messages))
      }

      verifyNoMoreInteractions(audit)
      verifyNoMoreInteractions(connector)
      verifyNoMoreInteractions(emailService)
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalDecision   = Decision("code", None, None, "justification", "goods")
      val originalCase       = aBTI.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))

      given(config.decisionLifetimeYears).willReturn(1)
      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier]))
        .willReturn(failed(new RuntimeException("Failed to update the Case")))

      intercept[RuntimeException] {
        await(service.completeCase(originalCase, operator)(hc, messages))
      }

      verifyNoMoreInteractions(audit)
      verifyNoMoreInteractions(emailService)
      verify(connector, never()).createEvent(refEq(aBTI), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {

      val operator: Operator = Operator("operator-id")
      val originalDecision   = Decision("code", None, None, "justification", "goods")
      val originalCase       = aBTI.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))
      val updatedDecision =
        Decision("code", Some(date("2018-01-01")), Some(date("2019-01-01")), "justification", "goods")
      val caseUpdated   = aBTI.copy(status = CaseStatus.COMPLETED, decision = Some(updatedDecision))
      val emailTemplate = EmailTemplate("plain", "html", "from", "subject", "services")

      given(config.decisionLifetimeYears).willReturn(1)
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(failed(new RuntimeException("Failed to create Event")))
      given(emailService.sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier]))
        .willReturn(Future.successful(emailTemplate))
      given(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(()))

      await(service.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

      verify(audit).auditCaseCompleted(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])
      verify(emailService).sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.COMPLETED
    }

    "succeed on email send failure" in {

      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalDecision   = Decision("code", None, None, "justification", "goods")
      val originalCase       = aBTI.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))
      val updatedDecision =
        Decision("code", Some(date("2018-01-01")), Some(date("2019-01-01")), "justification", "goods")
      val caseUpdated = aBTI.copy(status = CaseStatus.COMPLETED, decision = Some(updatedDecision))

      given(config.decisionLifetimeYears).willReturn(1)
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))
      given(emailService.sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier]))
        .willReturn(failed(new RuntimeException("Failed to send Email")))

      await(service.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

      verify(audit).auditCaseCompleted(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.COMPLETED

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))
      eventCreated.details shouldBe CompletedCaseStatusChange(
        CaseStatus.OPEN,
        None,
        Some("Attempted to send an email to the applicant which failed")
      )
    }

    "suceed on ruling notify failure" in {

      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalDecision   = Decision("code", None, None, "justification", "goods")
      val originalCase       = aBTI.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))
      val updatedDecision =
        Decision("code", Some(date("2018-01-01")), Some(date("2019-01-01")), "justification", "goods")
      val caseUpdated   = aBTI.copy(status = CaseStatus.COMPLETED, decision = Some(updatedDecision))
      val emailTemplate = EmailTemplate("plain", "html", "from", "subject", "services")

      given(config.decisionLifetimeYears).willReturn(1)
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))
      given(emailService.sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier]))
        .willReturn(Future.successful(emailTemplate))
      given(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier]))
        .willReturn(Future.failed(new RuntimeException("Failed to notify ruling store")))

      await(service.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

      verify(audit).auditCaseCompleted(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])
      verify(emailService).sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.COMPLETED

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))
      eventCreated.details shouldBe CompletedCaseStatusChange(
        CaseStatus.OPEN,
        None,
        Some("- Subject: subject\n- Body: plain")
      )
    }
  }

  "regenerate when document not found" in {
    val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
    val originalDecision   = Decision("code", None, None, "justification", "goods")
    val originalCase       = aLiability.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))

    val coverLetter       = Attachment(id = "865d82ac-49c0-4647-ba2f-88bfba6b4b75", operator = Some(operator))
    val rulingCertificate = Attachment(id = "f6436e76-6ec0-4019-9837-b8639058efa4", operator = Some(operator))

    val updatedDecision =
      Decision(
        "code",
        Some(date("2018-01-01")),
        Some(date("2019-01-01")),
        "justification",
        "goods",
        decisionPdf = Some(rulingCertificate),
        letterPdf = Some(coverLetter)
      )
    val caseUpdated = aBTI.copy(status = CaseStatus.COMPLETED, decision = Some(updatedDecision))

    given(fileStoreService.upload(any[FileUpload])(any[HeaderCarrier])).willReturn(
      successful(FileStoreAttachment("id", s"LiabilityDecision_${originalCase.reference}", "application/pdf", 0L))
    )
    given(pdfService.generatePdf(any[Html]))
      .willReturn(successful(PdfFile(Array.emptyByteArray)))
    given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))

    await(service.regenerateDocuments(originalCase, operator)) shouldBe caseUpdated

    val caseUpdating = theCaseUpdating(connector)
    caseUpdating.status shouldBe CaseStatus.COMPLETED

  }

  private def date(yymmdd: String): Instant =
    LocalDate.parse(yymmdd).atStartOfDay(ZoneId.of("UTC")).toInstant

}
