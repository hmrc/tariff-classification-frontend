/*
 * Copyright 2024 HM Revenue & Customs
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

import audit.AuditService
import connectors.{BindingTariffClassificationConnector, RulingConnector}
import models.CaseStatus.CaseStatus
import models._
import models.request.NewEventRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify, verifyNoMoreInteractions}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.templates.{cover_letter_template, ruling_template}

import scala.concurrent.Future.{failed, successful}

class CasesService_ReopenCaseSpec extends ServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val connector             = mock[BindingTariffClassificationConnector]
  private val emailService          = mock[EmailService]
  private val fileStoreService      = mock[FileStoreService]
  private val rulingConnector       = mock[RulingConnector]
  private val audit                 = mock[AuditService]
  private val countriesService      = mock[CountriesService]
  private val reportingService      = mock[ReportingService]
  private val pdfService            = mock[PdfService]
  private val cover_letter_template = mock[cover_letter_template]
  private val ruling_template       = mock[ruling_template]
  private val aCase                 = Cases.btiCaseExample

  private val service =
    new CasesService(
      audit,
      emailService,
      fileStoreService,
      countriesService,
      reportingService,
      pdfService,
      connector,
      rulingConnector,
      cover_letter_template,
      ruling_template
    )(executionContext, realAppConfig)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
    reset(audit)
  }

  "Reopen a Case" should {

    "update case status to OPEN when REFERRED" in {
      updateCaseShould(CaseStatus.REFERRED, CaseStatus.OPEN)
    }

    "update case status to OPEN when SUSPENDED" in {
      updateCaseShould(CaseStatus.SUSPENDED, CaseStatus.OPEN)
    }

    "not create event on update failure when status is SUSPENDED" in {
      eventUpdateFailure(CaseStatus.SUSPENDED)
    }

    "not create event on update failure when status is REFERRED" in {
      eventUpdateFailure(CaseStatus.REFERRED)
    }

    "succeed on event create failure from status REFERRED" in {

      succeededOnCreateFailure(CaseStatus.REFERRED, CaseStatus.OPEN)
    }

    "succeed on event create failure from status SUSPENDED" in {

      succeededOnCreateFailure(CaseStatus.SUSPENDED, CaseStatus.OPEN)
    }

    def updateCaseShould(originalStatus: CaseStatus, updatedStatus: CaseStatus) = {
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalCase       = aCase.copy(status = originalStatus)
      val caseUpdated        = aCase.copy(status = updatedStatus)

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))

      await(service.reopenCase(originalCase, operator)) shouldBe caseUpdated

      verify(audit).auditCaseReOpened(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe updatedStatus

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))
      eventCreated.details  shouldBe CaseStatusChange(originalStatus, updatedStatus)
    }

    def eventUpdateFailure(originalStatus: CaseStatus) = {
      val operator: Operator = Operator("operator-id")
      val originalCase       = aCase.copy(status = originalStatus)

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.reopenCase(originalCase, operator))
      }

      verifyNoMoreInteractions(audit)
      verify(connector, never()).createEvent(refEq(aCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    def succeededOnCreateFailure(originalStatus: CaseStatus, updatedStatus: CaseStatus) = {
      val operator: Operator = Operator("operator-id")
      val originalCase       = aCase.copy(status = originalStatus)
      val caseUpdated        = aCase.copy(status = updatedStatus)

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(failed(new RuntimeException()))

      await(service.reopenCase(originalCase, operator)) shouldBe caseUpdated

      verify(audit).auditCaseReOpened(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe updatedStatus
    }

  }

}
