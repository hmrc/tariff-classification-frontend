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
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify}
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

class CasesService_ReleaseCaseSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val manyCases = mock[Seq[Case]]
  private val oneCase = mock[Option[Case]]
  private val queue = mock[Queue]
  private val connector = mock[BindingTariffClassificationConnector]
  private val audit = mock[AuditService]
  private val config = mock[AppConfig]
  private val aCase = Case("ref", CaseStatus.OPEN, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)

  private val service = new CasesService(config, audit, connector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(connector, audit, queue, oneCase, manyCases, config)
  }

  "Release Case" should {
    "update case queue_id and status to NEW" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(status = CaseStatus.NEW)
      val caseUpdated = aCase.copy(status = CaseStatus.OPEN, queueId = Some("queue_id"))

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(caseUpdated))
      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])).willReturn(Future.successful(mock[Event]))
      given(audit.auditCaseReleased(refEq(caseUpdated))(any[HeaderCarrier])).willReturn(Future.successful())

      // When Then
      await(service.releaseCase(originalCase, queue, operator)) shouldBe caseUpdated

      verify(audit).auditCaseReleased(refEq(caseUpdated))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating()
      caseUpdating.status shouldBe CaseStatus.OPEN
      caseUpdating.queueId shouldBe Some("queue_id")

      val eventCreated = theEventCreatedFor(caseUpdated)
      eventCreated.userId shouldBe "operator-id"
      eventCreated.details shouldBe CaseStatusChange(CaseStatus.NEW, CaseStatus.OPEN)
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(status = CaseStatus.NEW)

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.releaseCase(originalCase, queue, operator))
      }

      verify(audit, never()).auditCaseReleased(any[Case])(any[HeaderCarrier])
      verify(connector, never()).createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(status = CaseStatus.NEW)
      val caseUpdated = aCase.copy(status = CaseStatus.OPEN, queueId = Some("queue_id"))

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(caseUpdated))
      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])).willReturn(Future.failed(new RuntimeException()))
      given(audit.auditCaseReleased(refEq(caseUpdated))(any[HeaderCarrier])).willReturn(Future.successful())

      // When Then
      await(service.releaseCase(originalCase, queue, operator)) shouldBe caseUpdated

      verify(audit).auditCaseReleased(refEq(caseUpdated))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating()
      caseUpdating.status shouldBe CaseStatus.OPEN
      caseUpdating.queueId shouldBe Some("queue_id")
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

}
