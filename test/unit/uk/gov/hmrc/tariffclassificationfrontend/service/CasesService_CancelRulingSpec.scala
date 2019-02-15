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

package uk.gov.hmrc.tariffclassificationfrontend.service

import java.time._

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{never, reset, verify, verifyZeroInteractions}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditService
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class CasesService_CancelRulingSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with ConnectorCaptor {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val manyCases = mock[Seq[Case]]
  private val oneCase = mock[Option[Case]]
  private val queue = mock[Queue]
  private val connector = mock[BindingTariffClassificationConnector]
  private val emailService = mock[EmailService]
  private val fileStoreService = mock[FileStoreService]
  private val audit = mock[AuditService]
  private val config = mock[AppConfig]
  private val clock = Clock.fixed(LocalDateTime.of(2018, 1, 1, 14, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
  private val aCase = Cases.btiCaseExample

  private val service = new CasesService(config, audit, emailService, fileStoreService, connector)

  override protected def afterEach(): Unit = {
    super.afterEach()

    // should never use email service
    verifyZeroInteractions(emailService)
    reset(connector, audit, queue, oneCase, manyCases, config, emailService)
  }

  "Cancel Ruling" should {
    "update case status to CANCELLED and decision end date" in {
      // Given
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalDecision = Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))
      val updatedDecision = originalDecision.copy(effectiveEndDate = Some(date("2019-01-01")))
      val caseUpdated = aCase.copy(status = CaseStatus.CANCELLED, decision = Some(updatedDecision))

      given(config.zoneId).willReturn(ZoneId.of("UTC"))
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(successful(mock[Event]))

      // When Then
      await(service.cancelRuling(originalCase, CancelReason.ANNULLED, operator, clock)) shouldBe caseUpdated

      verify(audit).auditRulingCancelled(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.CANCELLED
      caseUpdating.decision.get.cancelReason shouldBe Some(CancelReason.ANNULLED)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))
      eventCreated.details shouldBe CaseStatusChange(CaseStatus.COMPLETED, CaseStatus.CANCELLED)
    }

    "reject case without a decision" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = None)
      given(config.zoneId).willReturn(ZoneId.of("UTC"))

      // When Then
      intercept[IllegalArgumentException] {
        await(service.cancelRuling(originalCase, CancelReason.ANNULLED, operator))
      }

      verifyZeroInteractions(audit)
      verifyZeroInteractions(connector)
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalDecision = Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))

      given(config.zoneId).willReturn(ZoneId.of("UTC"))
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(failed(new RuntimeException("Failed to update the Case")))

      intercept[RuntimeException] {
        await(service.cancelRuling(originalCase, CancelReason.ANNULLED, operator))
      }

      verifyZeroInteractions(audit)
      verify(connector, never()).createEvent(refEq(aCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalDecision = Decision("code", Some(date("2018-01-01")), Some(date("2021-01-01")), "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(originalDecision))
      val updatedDecision = originalDecision.copy(effectiveEndDate = Some(date("2019-01-01")))
      val caseUpdated = aCase.copy(status = CaseStatus.CANCELLED, decision = Some(updatedDecision))

      given(config.zoneId).willReturn(ZoneId.of("UTC"))
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(failed(new RuntimeException("Failed to create Event")))

      // When Then
      await(service.cancelRuling(originalCase, CancelReason.ANNULLED, operator)) shouldBe caseUpdated

      verify(audit).auditRulingCancelled(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.CANCELLED
    }

  }

  private def date(yymmdd: String): Instant = {
    LocalDate.parse(yymmdd).atStartOfDay(ZoneId.of("UTC")).toInstant
  }

}
