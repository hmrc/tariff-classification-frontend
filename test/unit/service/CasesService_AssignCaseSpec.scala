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

import audit.AuditService
import config.AppConfig
import connector.{BindingTariffClassificationConnector, RulingConnector}
import models._
import models.request.NewEventRequest
import org.mockito.ArgumentMatchers.{refEq, _}
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify, verifyZeroInteractions}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases

import scala.concurrent.Future.{failed, successful}

class CasesService_AssignCaseSpec extends ServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val manyCases = mock[Seq[Case]]
  private val oneCase = mock[Option[Case]]
  private val connector = mock[BindingTariffClassificationConnector]
  private val rulingConnector = mock[RulingConnector]
  private val emailService = mock[EmailService]
  private val reportingService = mock[ReportingService]
  private val fileStoreService = mock[FileStoreService]
  private val audit = mock[AuditService]
  private val aCase = Cases.btiCaseExample

  private val service = new CasesService(realAppConfig, audit, emailService, fileStoreService,reportingService, connector, rulingConnector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit, oneCase, manyCases)
  }

  "Assign a Case" should {
    "update case status to REFERRED" in {
      // Given
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalCase = aCase.copy(assignee = None)
      val caseUpdated = aCase.copy(assignee = Some(operator))

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(successful(mock[Event]))

      // When
      await(service.assignCase(originalCase, operator)) shouldBe caseUpdated

      // Then
      verify(audit).auditOperatorAssigned(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.assignee shouldBe Some(operator)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))
      eventCreated.details shouldBe AssignmentChange(None, Some(operator))
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(assignee = None)

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.assignCase(originalCase, operator))
      }

      verifyZeroInteractions(audit)
      verify(connector, never()).createEvent(refEq(aCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(assignee = None)
      val caseUpdated = aCase.copy(assignee = Some(operator))

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      // When Then
      await(service.assignCase(originalCase, operator)) shouldBe caseUpdated

      verify(audit).auditOperatorAssigned(refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.assignee shouldBe Some(operator)
    }

    "do not create an event when there is no assignee on either of the cases" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(assignee = None)
      val caseUpdated = aCase.copy(assignee = None)

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))

      // When
      await(service.assignCase(originalCase, operator))

      // Then
      verify(connector, never()).createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])
    }
  }

}
