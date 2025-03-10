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
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import java.time._
import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class CasesService_CancelRulingSpec extends CasesServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val manyCases = mock[Seq[Case]]
  private val oneCase   = mock[Option[Case]]
  private val clock = Clock.fixed(
    LocalDateTime.of(2018, 1, 1, 14, 0).toInstant(ZoneOffset.UTC),
    ZoneId.of("UTC")
  )
  private val aCase = Cases.btiCaseExample

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    when(config.clock).thenReturn(clock)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    // should never use email service
    verifyNoMoreInteractions(emailService)
    reset(connector)
    reset(audit)
    reset(queue)
    reset(oneCase)
    reset(manyCases)
    reset(config)
    reset(emailService)
  }

  "Cancel Ruling" should {
    "update case status to CANCELLED and decision end date" in {

      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val attachment         = Attachment("id", operator = Some(operator))
      val originalDecision =
        Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase    = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))
      val updatedDecision = originalDecision.copy(effectiveEndDate = Some(date("2019-01-01")))
      val caseUpdated     = aCase.copy(status = CaseStatus.CANCELLED, decision = Some(updatedDecision))

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(successful(mock[Event]))
      when(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier])).thenReturn(Future.successful(()))

      await(
        serviceMockConfig.cancelRuling(originalCase, CancelReason.ANNULLED, attachment, "note", operator)
      ) shouldBe caseUpdated

      verify(audit).auditRulingCancelled(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status                    shouldBe CaseStatus.CANCELLED
      caseUpdating.decision.get.cancellation shouldBe Some(Cancellation(CancelReason.ANNULLED))
      caseUpdating.attachments                 should have(size(1))

      val attachmentUpdating = caseUpdating.attachments.find(_.id == "id")
      attachmentUpdating.map(_.id)           shouldBe Some("id")
      attachmentUpdating.map(_.public)       shouldBe Some(false)
      attachmentUpdating.flatMap(_.operator) shouldBe Some(operator)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))

      eventCreated.details shouldBe CancellationCaseStatusChange(
        CaseStatus.COMPLETED,
        Some("note"),
        Some("id"),
        CancelReason.ANNULLED
      )
    }

    "reject case without a decision" in {

      val operator: Operator = Operator("operator-id")
      val attachment         = Attachment("id", operator = Some(operator))
      val originalCase       = aCase.copy(status = CaseStatus.COMPLETED, decision = None)

      intercept[IllegalArgumentException] {
        await(serviceMockConfig.cancelRuling(originalCase, CancelReason.ANNULLED, attachment, "note", operator))
      }

      verifyNoMoreInteractions(audit)
      verifyNoMoreInteractions(connector)
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val attachment         = Attachment("id", operator = Some(operator))
      val originalDecision =
        Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))

      when(connector.updateCase(any[Case])(any[HeaderCarrier]))
        .thenReturn(failed(new RuntimeException("Failed to update the Case")))

      intercept[RuntimeException] {
        await(serviceMockConfig.cancelRuling(originalCase, CancelReason.ANNULLED, attachment, "note", operator))
      }

      verifyNoMoreInteractions(audit)
      verify(connector, never()).createEvent(refEq(aCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {

      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val attachment         = Attachment("id", operator = Some(operator))
      val originalDecision =
        Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase    = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))
      val updatedDecision = originalDecision.copy(effectiveEndDate = Some(date("2019-01-01")))
      val caseUpdated     = aCase.copy(status = CaseStatus.CANCELLED, decision = Some(updatedDecision))

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(failed(new RuntimeException("Failed to create Event")))
      when(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier])).thenReturn(Future.successful(()))

      await(
        serviceMockConfig.cancelRuling(originalCase, CancelReason.ANNULLED, attachment, "note", operator)
      ) shouldBe caseUpdated

      verify(audit).auditRulingCancelled(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.CANCELLED
    }

    "succeed on ruling store notify failure" in {

      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val attachment         = Attachment("id", operator = Some(operator))
      val originalDecision =
        Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase    = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))
      val updatedDecision = originalDecision.copy(effectiveEndDate = Some(date("2019-01-01")))
      val caseUpdated     = aCase.copy(status = CaseStatus.CANCELLED, decision = Some(updatedDecision))

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(successful(mock[Event]))
      when(rulingConnector.notify(refEq(originalCase.reference))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Failed to notify the Ruling Store")))

      await(
        serviceMockConfig.cancelRuling(originalCase, CancelReason.ANNULLED, attachment, "note", operator)
      ) shouldBe caseUpdated

      verify(audit).auditRulingCancelled(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status                    shouldBe CaseStatus.CANCELLED
      caseUpdating.decision.get.cancellation shouldBe Some(Cancellation(CancelReason.ANNULLED))

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))

      eventCreated.details shouldBe CancellationCaseStatusChange(
        CaseStatus.COMPLETED,
        Some("note"),
        Some("id"),
        CancelReason.ANNULLED
      )
    }

  }

  private def date(yymmdd: String): Instant =
    LocalDate.parse(yymmdd).atStartOfDay(ZoneId.of("UTC")).toInstant

}
