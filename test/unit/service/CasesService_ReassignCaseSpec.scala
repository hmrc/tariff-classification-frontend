/*
 * Copyright 2022 HM Revenue & Customs
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

import audit.AuditService
import connector.{BindingTariffClassificationConnector, RulingConnector}
import models._
import models.request.NewEventRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify, verifyNoMoreInteractions}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future.{failed, successful}
import scala.concurrent.ExecutionContext.Implicits.global

class CasesService_ReassignCaseSpec extends ServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val manyCases        = mock[Seq[Case]]
  private val oneCase          = mock[Option[Case]]
  private val queue            = mock[Queue]
  private val connector        = mock[BindingTariffClassificationConnector]
  private val rulingConnector  = mock[RulingConnector]
  private val emailService     = mock[EmailService]
  private val fileStoreService = mock[FileStoreService]
  private val countriesService = mock[CountriesService]
  private val reportingService = mock[ReportingService]
  private val pdfService       = mock[PdfService]
  private val audit            = mock[AuditService]
  private val aCase            = Cases.btiCaseExample

  private val service =
    new CasesService(audit, emailService, fileStoreService, countriesService, reportingService, pdfService, connector, rulingConnector)(global, realAppConfig)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit, queue, oneCase, manyCases)
  }

  "Reassign Case" should {
    "update case queue_id and status to OPEN" in {
      // Given
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalCase       = aCase.copy(status = CaseStatus.OPEN)
      val caseUpdated        = aCase.copy(queueId = Some("queue_id"))

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))

      // When Then
      await(service.reassignCase(originalCase, queue, operator)) shouldBe caseUpdated

      verify(audit).auditQueueReassigned(refEq(caseUpdated), refEq(operator), refEq(queue))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status  shouldBe CaseStatus.OPEN
      caseUpdating.queueId shouldBe Some("queue_id")
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalCase       = aCase.copy(status = CaseStatus.NEW)

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.releaseCase(originalCase, queue, operator))
      }

      verifyNoMoreInteractions(audit)
      verify(connector, never()).createEvent(refEq(aCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase       = aCase.copy(status = CaseStatus.NEW)
      val caseUpdated        = aCase.copy(status = CaseStatus.OPEN, queueId = Some("queue_id"))

      given(queue.id).willReturn("queue_id")
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(failed(new RuntimeException()))

      // When Then
      await(service.releaseCase(originalCase, queue, operator)) shouldBe caseUpdated

      verify(audit).auditCaseReleased(refEq(originalCase), refEq(caseUpdated), refEq(queue), refEq(operator))(
        any[HeaderCarrier]
      )

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status  shouldBe CaseStatus.OPEN
      caseUpdating.queueId shouldBe Some("queue_id")
    }
  }

}
