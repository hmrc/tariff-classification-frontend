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

import java.time.ZonedDateTime

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest

import scala.concurrent.Future

class CasesServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val manyCases = mock[Seq[Case]]
  private val oneCase = mock[Option[Case]]
  private val queue = mock[Queue]
  private val connector = mock[BindingTariffClassificationConnector]

  private val service = new CasesService(connector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
  }

  "Get Cases 'By Queue'" should {
    "retrieve connector cases" in {
      given(connector.findCasesByQueue(queue)) willReturn Future.successful(manyCases)

      await(service.getCasesByQueue(queue)) shouldBe manyCases
    }
  }

  "Get Cases 'By Assignee'" should {
    "retrieve connector cases" in {
      given(connector.findCasesByAssignee("assignee")) willReturn Future.successful(manyCases)

      await(service.getCasesByAssignee("assignee")) shouldBe manyCases
    }
  }

  "Get One Case 'By Reference'" should {
    "retrieve connector case" in {
      given(connector.findCase("reference")) willReturn Future.successful(oneCase)

      await(service.getOne("reference")) shouldBe oneCase
    }
  }

  "Release Case" should {
    "update case queue_id and status to NEW" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = Case("ref", CaseStatus.NEW, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)
      val caseUpdated = Case("ref", CaseStatus.OPEN, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(caseUpdated))
      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])).willReturn(Future.successful(mock[Event]))

      // When Then
      await(service.releaseCase(originalCase, queue, operator)) shouldBe caseUpdated

      val caseUpdating = theCaseUpdating()
      caseUpdating.status shouldBe CaseStatus.OPEN
      caseUpdating.queueId shouldBe Some("queue_id")

      val eventCreated = theEventCreatedFor(caseUpdated)
      eventCreated.userId shouldBe "operator-id"
      eventCreated.details shouldBe CaseStatusChange(CaseStatus.NEW, CaseStatus.OPEN)
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalCase = Case("ref", CaseStatus.NEW, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.releaseCase(originalCase, queue, operator))
      }

      verify(connector, never()).createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = Case("ref", CaseStatus.NEW, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)
      val caseUpdated = Case("ref", CaseStatus.OPEN, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(caseUpdated))
      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])).willReturn(Future.failed(new RuntimeException()))

      // When Then
      await(service.releaseCase(originalCase, queue, operator)) shouldBe caseUpdated

      val caseUpdating = theCaseUpdating()
      caseUpdating.status shouldBe CaseStatus.OPEN
      caseUpdating.queueId shouldBe Some("queue_id")
    }
  }

  "Complete Case" should {
    "update case status to COMPLETED" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = Case("ref", CaseStatus.OPEN, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)
      val caseUpdated = Case("ref", CaseStatus.COMPLETED, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(caseUpdated))
      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])).willReturn(Future.successful(mock[Event]))

      // When Then
      await(service.completeCase(originalCase, operator)) shouldBe caseUpdated

      val caseUpdating = theCaseUpdating()
      caseUpdating.status shouldBe CaseStatus.COMPLETED

      val eventCreated = theEventCreatedFor(caseUpdated)
      eventCreated.userId shouldBe "operator-id"
      eventCreated.details shouldBe CaseStatusChange(CaseStatus.OPEN, CaseStatus.COMPLETED)
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalCase = Case("ref", CaseStatus.OPEN, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.completeCase(originalCase, operator))
      }

      verify(connector, never()).createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = Case("ref", CaseStatus.OPEN, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)
      val caseUpdated = Case("ref", CaseStatus.COMPLETED, ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, mock[Application], None, Seq.empty)

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(caseUpdated))
      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])).willReturn(Future.failed(new RuntimeException()))

      // When Then
      await(service.completeCase(originalCase, operator)) shouldBe caseUpdated

      val caseUpdating = theCaseUpdating()
      caseUpdating.status shouldBe CaseStatus.COMPLETED
    }
  }

  "Update Case" should {
    val oldCase = mock[Case]
    val updatedCase = mock[Case]

    "delegate to connector" in {
      given(connector.updateCase(refEq(oldCase))(any[HeaderCarrier])) willReturn Future.successful(updatedCase)

      await(service.updateCase(oldCase)) shouldBe updatedCase
    }
  }

  def theEventCreatedFor(c: Case): NewEventRequest = {
    val captor: ArgumentCaptor[NewEventRequest] = ArgumentCaptor.forClass(classOf[NewEventRequest])
    verify(connector).createEvent(refEq(c), captor.capture())(any[HeaderCarrier])
    captor.getValue
  }

  def theCaseUpdating(): Case = {
    val captor: ArgumentCaptor[Case] = ArgumentCaptor.forClass(classOf[Case])
    verify(connector).updateCase(captor.capture())(any[HeaderCarrier])
    captor.getValue
  }

}
