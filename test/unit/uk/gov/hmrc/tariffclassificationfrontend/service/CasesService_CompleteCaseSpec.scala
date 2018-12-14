/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.ArgumentCaptor
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

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class CasesService_CompleteCaseSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val manyCases = mock[Seq[Case]]
  private val oneCase = mock[Option[Case]]
  private val queue = mock[Queue]
  private val connector = mock[BindingTariffClassificationConnector]
  private val emailService = mock[EmailService]
  private val audit = mock[AuditService]
  private val config = mock[AppConfig]
  private val clock = Clock.fixed(LocalDateTime.of(2018,1,1, 14,0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
  private val aCase = Case("ref", CaseStatus.OPEN, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)
  private val epoch = date("1970-01-01")

  private val service = new CasesService(config, audit, emailService, connector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit, queue, oneCase, manyCases, config, emailService)
  }

  "Complete Case" should {
    "update case status to COMPLETED" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalDecision = Decision("code", epoch, epoch, "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))
      val updatedDecision = Decision("code", date("2018-01-01"), date("2019-01-01"), "justification", "goods")
      val caseUpdated = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(updatedDecision))

      given(config.zoneId).willReturn(ZoneId.of("UTC"))
      given(config.decisionLifetimeYears).willReturn(1)
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])).willReturn(successful(mock[Event]))
      given(emailService.sendCaseCompleteEmail(refEq(caseUpdated))(any[HeaderCarrier])).willReturn(Future.successful())

      // When Then
      await(service.completeCase(originalCase, operator, clock)) shouldBe caseUpdated

      verify(audit).auditCaseCompleted(refEq(caseUpdated))(any[HeaderCarrier])
      verify(emailService).sendCaseCompleteEmail(refEq(caseUpdated))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating()
      caseUpdating.status shouldBe CaseStatus.COMPLETED

      val eventCreated = theEventCreatedFor(caseUpdated)
      eventCreated.userId shouldBe "operator-id"
      eventCreated.details shouldBe CaseStatusChange(CaseStatus.OPEN, CaseStatus.COMPLETED)
    }

    "reject case without a decision" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(status = CaseStatus.OPEN, decision = None)
      given(config.zoneId).willReturn(ZoneId.of("UTC"))

      // When Then
      intercept[IllegalArgumentException] {
        await(service.completeCase(originalCase, operator))
      }

      verifyZeroInteractions(audit)
      verifyZeroInteractions(connector)
      verifyZeroInteractions(emailService)
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalDecision = Decision("code", epoch, epoch, "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))

      given(config.decisionLifetimeYears).willReturn(1)
      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.completeCase(originalCase, operator))
      }

      verifyZeroInteractions(audit)
      verifyZeroInteractions(emailService)
      verify(connector, never()).createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalDecision = Decision("code", epoch, epoch, "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))
      val updatedDecision = Decision("code", date("2018-01-01"), date("2019-01-01"), "justification", "goods")
      val caseUpdated = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(updatedDecision))

      given(config.zoneId).willReturn(ZoneId.of("UTC"))
      given(config.decisionLifetimeYears).willReturn(1)
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))
      given(emailService.sendCaseCompleteEmail(refEq(caseUpdated))(any[HeaderCarrier])).willReturn(Future.successful())

      // When Then
      await(service.completeCase(originalCase, operator)) shouldBe caseUpdated

      verify(audit).auditCaseCompleted(refEq(caseUpdated))(any[HeaderCarrier])
      verify(emailService).sendCaseCompleteEmail(refEq(caseUpdated))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating()
      caseUpdating.status shouldBe CaseStatus.COMPLETED
    }

    "succeed on email send failure" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalDecision = Decision("code", epoch, epoch, "justification", "goods")
      val originalCase = aCase.copy(status = CaseStatus.OPEN, decision = Some(originalDecision))
      val updatedDecision = Decision("code", date("2018-01-01"), date("2019-01-01"), "justification", "goods")
      val caseUpdated = aCase.copy(status = CaseStatus.COMPLETED, decision = Some(updatedDecision))

      given(config.zoneId).willReturn(ZoneId.of("UTC"))
      given(config.decisionLifetimeYears).willReturn(1)
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])).willReturn(successful(mock[Event]))
      given(emailService.sendCaseCompleteEmail(refEq(caseUpdated))(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      // When Then
      await(service.completeCase(originalCase, operator)) shouldBe caseUpdated

      verify(audit).auditCaseCompleted(refEq(caseUpdated))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating()
      caseUpdating.status shouldBe CaseStatus.COMPLETED

      val eventCreated = theEventCreatedFor(caseUpdated)
      eventCreated.userId shouldBe "operator-id"
      eventCreated.details shouldBe CaseStatusChange(CaseStatus.OPEN, CaseStatus.COMPLETED)
    }
  }

  private def theEventCreatedFor(c: Case): NewEventRequest = {
    val captor: ArgumentCaptor[NewEventRequest] = ArgumentCaptor.forClass(classOf[NewEventRequest])
    verify(connector).createEvent(refEq(c), captor.capture())(any[HeaderCarrier])
    captor.getValue
  }

  private def theCaseUpdating(): Case = {
    val captor: ArgumentCaptor[Case] = ArgumentCaptor.forClass(classOf[Case])
    verify(connector).updateCase(captor.capture())(any[HeaderCarrier])
    captor.getValue
  }

  private def date(yymmdd: String): ZonedDateTime = {
    LocalDate.parse(yymmdd).atStartOfDay(ZoneId.of("UTC"))
  }

}
