/*
 * Copyright 2025 HM Revenue & Customs
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

import models._
import models.request.NewEventRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{never, reset, verify, verifyNoMoreInteractions}
import org.scalatest.BeforeAndAfterEach
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import java.time._
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class CasesService_CompleteCaseSpec extends CasesServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val manyCases  = mock[Seq[Case]]
  private val oneCase    = mock[Option[Case]]
  private val clock      = Clock.fixed(LocalDateTime.of(2018, 1, 1, 14, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
  private val aBTI       = Cases.btiCaseExample
  private val aLiability = Cases.liabilityCaseExample

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(connector)
    reset(audit)
    reset(queue)
    reset(oneCase)
    reset(manyCases)
    reset(config)
    reset(emailService)

    when(config.clock).thenReturn(clock)
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

        when(config.decisionLifetimeYears).thenReturn(1)
        when(fileStoreService.upload(any[FileUpload])(any[HeaderCarrier])).thenReturn(
          successful(FileStoreAttachment("id", s"LiabilityDecision_${originalCase.reference}", "application/pdf", 0L))
        )
        when(pdfService.generateFopPdf(any[Html], any[String]))
          .thenReturn(successful(PdfFile(Array.emptyByteArray)))
        when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
        when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
          .thenReturn(successful(mock[Event]))

        await(serviceMockConfig.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

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

        when(config.decisionLifetimeYears).thenReturn(3)
        when(config.decisionLifetimeDays).thenReturn(1)
        when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
        when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
          .thenReturn(successful(mock[Event]))
        when(emailService.sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier]))
          .thenReturn(Future.successful(emailTemplate))
        when(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        await(serviceMockConfig.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

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

        when(connector.updateCase(any[Case])(any[HeaderCarrier]))
          .thenReturn(successful(atarCase))
        when(connector.createEvent(refEq(atarCase), any[NewEventRequest])(any[HeaderCarrier]))
          .thenReturn(successful(mock[Event]))
        when(emailService.sendCaseCompleteEmail(refEq(atarCase), refEq(operator))(any[HeaderCarrier]))
          .thenReturn(Future.successful(emailTemplate))
        when(rulingConnector.notify(refEq(atarCase.reference))(any[HeaderCarrier]))
          .thenReturn(Future.successful(()))

        await(serviceMockConfig.completeCase(atarCase, operator)(hc, messages))

        val caseUpdating = theCaseUpdating(connector)
        caseUpdating.status                        shouldBe CaseStatus.COMPLETED
        caseUpdating.decision.get.effectiveEndDate shouldBe updatedEndDateInstant
      }
    }

    "reject case without a decision" in {

      val operator: Operator = Operator("operator-id")
      val originalCase       = aBTI.copy(status = CaseStatus.OPEN, decision = None)

      intercept[IllegalArgumentException] {
        await(serviceMockConfig.completeCase(originalCase, operator)(hc, messages))
      }

      verifyNoMoreInteractions(audit)
      verifyNoMoreInteractions(connector)
      verifyNoMoreInteractions(emailService)
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalDecision   = Decision("code", None, None, "justification", "goods")
      val originalCase       = aBTI.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))

      when(config.decisionLifetimeYears).thenReturn(1)
      when(queue.id).thenReturn("queue_id")
      when(connector.updateCase(any[Case])(any[HeaderCarrier]))
        .thenReturn(failed(new RuntimeException("Failed to update the Case")))

      intercept[RuntimeException] {
        await(serviceMockConfig.completeCase(originalCase, operator)(hc, messages))
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

      when(config.decisionLifetimeYears).thenReturn(1)
      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(failed(new RuntimeException("Failed to create Event")))
      when(emailService.sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier]))
        .thenReturn(Future.successful(emailTemplate))
      when(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier])).thenReturn(Future.successful(()))

      await(serviceMockConfig.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

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

      when(config.decisionLifetimeYears).thenReturn(1)
      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(successful(mock[Event]))
      when(emailService.sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier]))
        .thenReturn(failed(new RuntimeException("Failed to send Email")))

      await(serviceMockConfig.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

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

      when(config.decisionLifetimeYears).thenReturn(1)
      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(successful(mock[Event]))
      when(emailService.sendCaseCompleteEmail(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier]))
        .thenReturn(Future.successful(emailTemplate))
      when(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Failed to notify ruling store")))

      await(serviceMockConfig.completeCase(originalCase, operator)(hc, messages)) shouldBe caseUpdated

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

    when(fileStoreService.upload(any[FileUpload])(any[HeaderCarrier])).thenReturn(
      successful(FileStoreAttachment("id", s"LiabilityDecision_${originalCase.reference}", "application/pdf", 0L))
    )
    when(pdfService.generateFopPdf(any[Html], any[String]))
      .thenReturn(successful(PdfFile(Array.emptyByteArray)))
    when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))

    await(serviceMockConfig.regenerateDocuments(originalCase, operator)) shouldBe caseUpdated

    val caseUpdating = theCaseUpdating(connector)
    caseUpdating.status shouldBe CaseStatus.COMPLETED

  }

  private def date(yymmdd: String): Instant =
    LocalDate.parse(yymmdd).atStartOfDay(ZoneId.of("UTC")).toInstant

}
