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
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify, verifyNoMoreInteractions}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future.{failed, successful}

class CasesService_RejectCaseSpec extends CasesServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val manyCases = mock[Seq[Case]]
  private val oneCase   = mock[Option[Case]]
  private val aCase     = Cases.btiCaseExample

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
    reset(audit)
    reset(oneCase)
    reset(manyCases)
  }

  "Reject a Case" should {
    "update case status to REJECTED" in {

      val existingAttachment = mock[Attachment]

      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val attachment         = Attachment("id", operator = Some(operator))
      val originalCase       = aCase.copy(status = CaseStatus.OPEN, attachments = Seq(existingAttachment))
      val caseUpdated        = aCase.copy(status = CaseStatus.REJECTED)

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(successful(mock[Event]))

      await(
        service.rejectCase(originalCase, RejectReason.NO_INFO_FROM_TRADER, attachment, "note", operator)
      ) shouldBe caseUpdated

      verify(audit).auditCaseRejected(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status    shouldBe CaseStatus.REJECTED
      caseUpdating.attachments should have(size(2))

      val attachmentUpdating = caseUpdating.attachments.find(_.id == "id")
      attachmentUpdating.map(_.id)           shouldBe Some("id")
      attachmentUpdating.map(_.public)       shouldBe Some(false)
      attachmentUpdating.flatMap(_.operator) shouldBe Some(operator)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))
      eventCreated.details shouldBe RejectCaseStatusChange(
        CaseStatus.OPEN,
        CaseStatus.REJECTED,
        Some("note"),
        Some("id"),
        RejectReason.NO_INFO_FROM_TRADER
      )
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val attachment         = Attachment("id", operator = Some(operator))
      val originalCase       = aCase.copy(status = CaseStatus.OPEN)

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.rejectCase(originalCase, RejectReason.NO_INFO_FROM_TRADER, attachment, "note", operator))
      }

      verifyNoMoreInteractions(audit)
      verify(connector, never()).createEvent(refEq(aCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {

      val operator: Operator = Operator("operator-id")
      val attachment         = Attachment("id", operator = Some(operator))
      val originalCase       = aCase.copy(status = CaseStatus.OPEN)
      val caseUpdated        = aCase.copy(status = CaseStatus.REJECTED)

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(failed(new RuntimeException()))

      await(
        service.rejectCase(originalCase, RejectReason.NO_INFO_FROM_TRADER, attachment, "note", operator)
      ) shouldBe caseUpdated

      verify(audit).auditCaseRejected(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.REJECTED

    }
  }

}
