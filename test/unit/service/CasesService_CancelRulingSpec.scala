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

package service

import java.time._

import audit.AuditService
import config.AppConfig
import connector.{BindingTariffClassificationConnector, RulingConnector}
import models._
import models.request.NewEventRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{never, reset, verify, verifyZeroInteractions}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class CasesService_CancelRulingSpec extends ServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val manyCases = mock[Seq[Case]]
  private val oneCase = mock[Option[Case]]
  private val queue = mock[Queue]
  private val connector = mock[BindingTariffClassificationConnector]
  private val rulingConnector = mock[RulingConnector]
  private val emailService = mock[EmailService]
  private val fileStoreService = mock[FileStoreService]
  private val reportingService = mock[ReportingService]
  private val audit = mock[AuditService]
  private val config = mock[AppConfig]
  private val clock = Clock.fixed(
    LocalDateTime.of(2018, 1, 1, 14, 0).toInstant(ZoneOffset.UTC),
    ZoneId.of("UTC")
  )
  private val aCase = Cases.btiCaseExample

  private val service = new CasesService(config, audit, emailService, fileStoreService, reportingService, connector, rulingConnector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    given(config.clock).willReturn(clock)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    // should never use email service
    verifyZeroInteractions(emailService)
    reset(connector, audit, queue, oneCase, manyCases, config, emailService)
  }

  "Cancel Ruling" should {
    "update case status to CANCELLED and decision end date" in {
      // Given
      val fileUpload = mock[FileUpload]
      val fileUploaded = FileStoreAttachment("id", "email", "application/pdf", 0)

      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalDecision = Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))
      val updatedDecision = originalDecision.copy(effectiveEndDate = Some(date("2019-01-01")))
      val caseUpdated = aCase.copy(status = CaseStatus.CANCELLED, decision = Some(updatedDecision))

      given(fileStoreService.upload(fileUpload)).willReturn(successful(fileUploaded))
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(successful(mock[Event]))
      given(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(()))

      // When Then
      await(service.cancelRuling(originalCase, CancelReason.ANNULLED, fileUpload, "note", operator)) shouldBe caseUpdated

      verify(audit).auditRulingCancelled(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.CANCELLED
      caseUpdating.decision.get.cancellation shouldBe Some(Cancellation(CancelReason.ANNULLED))
      caseUpdating.attachments should have(size(1))

      val attachmentUpdating = caseUpdating.attachments.find(_.id == "id")
      attachmentUpdating.map(_.id) shouldBe Some("id")
      attachmentUpdating.map(_.public) shouldBe Some(false)
      attachmentUpdating.flatMap(_.operator) shouldBe Some(operator)


      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))

      eventCreated.details shouldBe CancellationCaseStatusChange(CaseStatus.COMPLETED, Some("note"), Some("id"), CancelReason.ANNULLED)
    }

    "reject case without a decision" in {
      // Given
      val fileUpload = mock[FileUpload]
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = None)

      // When Then
      intercept[IllegalArgumentException] {
        await(service.cancelRuling(originalCase, CancelReason.ANNULLED, fileUpload, "note", operator))
      }

      verifyZeroInteractions(audit)
      verifyZeroInteractions(connector)
    }

    "generate an exception on attachment upload failure" in {
      // Given
      val fileUpload = mock[FileUpload]
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED)

      given(fileStoreService.upload(fileUpload)).willReturn(failed(new RuntimeException("Error")))

      // When Then
      intercept[RuntimeException] {
        await(service.cancelRuling(originalCase, CancelReason.ANNULLED, fileUpload, "note", operator))
      }

      verifyZeroInteractions(connector)
      verifyZeroInteractions(audit)

    }

    "not create event on update failure" in {
      val fileUpload = mock[FileUpload]
      val operator: Operator = Operator("operator-id")
      val originalDecision = Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(failed(new RuntimeException("Failed to update the Case")))

      intercept[RuntimeException] {
        await(service.cancelRuling(originalCase, CancelReason.ANNULLED, fileUpload, "note", operator))
      }

      verifyZeroInteractions(audit)
      verify(connector, never()).createEvent(refEq(aCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val fileUpload = mock[FileUpload]
      val fileUploaded = FileStoreAttachment("id", "email", "application/pdf", 0)
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalDecision = Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))
      val updatedDecision = originalDecision.copy(effectiveEndDate = Some(date("2019-01-01")))
      val caseUpdated = aCase.copy(status = CaseStatus.CANCELLED, decision = Some(updatedDecision))

      given(fileStoreService.upload(fileUpload)).willReturn(successful(fileUploaded))
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(failed(new RuntimeException("Failed to create Event")))
      given(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier])).willReturn(Future.successful(()))

      // When Then
      await(service.cancelRuling(originalCase, CancelReason.ANNULLED, fileUpload, "note", operator)) shouldBe caseUpdated

      verify(audit).auditRulingCancelled(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.CANCELLED
    }

    "succeed on ruling store notify failure" in {
      // Given
      val fileUpload = mock[FileUpload]
      val fileUploaded = FileStoreAttachment("id", "email", "application/pdf", 0)
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalDecision = Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))
      val updatedDecision = originalDecision.copy(effectiveEndDate = Some(date("2019-01-01")))
      val caseUpdated = aCase.copy(status = CaseStatus.CANCELLED, decision = Some(updatedDecision))

      given(fileStoreService.upload(fileUpload)).willReturn(successful(fileUploaded))
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(successful(mock[Event]))
      given(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier])).willReturn(Future.failed(new RuntimeException("Failed to notify the Ruling Store")))

      // When Then
      await(service.cancelRuling(originalCase, CancelReason.ANNULLED, fileUpload, "note", operator)) shouldBe caseUpdated

      verify(audit).auditRulingCancelled(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.CANCELLED
      caseUpdating.decision.get.cancellation shouldBe Some(Cancellation(CancelReason.ANNULLED))

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))

      eventCreated.details shouldBe CancellationCaseStatusChange(CaseStatus.COMPLETED, Some("note"), Some("id"), CancelReason.ANNULLED)
    }

  }

  private def date(yymmdd: String): Instant = {
    LocalDate.parse(yymmdd).atStartOfDay(ZoneId.of("UTC")).toInstant
  }

}
