/*
 * Copyright 2023 HM Revenue & Customs
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

class CasesService_UpdateSampleStatusSpec extends ServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

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
    new CasesService(
      audit,
      emailService,
      fileStoreService,
      countriesService,
      reportingService,
      pdfService,
      connector,
      rulingConnector
    )(global, realAppConfig)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit)
  }

  "Update Sample Status" should {

    "update case sample status to None" in {
      // Given
      val operator: Operator = Operator("operator-id", None)
      val originalCase       = aCase.copy(sample = aCase.sample.copy(status = Some(SampleStatus.MOVED_TO_ACT)))
      val caseUpdated        = aCase.copy(sample = aCase.sample.copy(status = None))

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))

      // When Then
      await(service.updateSampleStatus(originalCase, None, operator)) shouldBe caseUpdated

      verify(audit).auditSampleStatusChange(refEq(originalCase), refEq(caseUpdated), refEq(operator))(
        any[HeaderCarrier]
      )

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.sample.status shouldBe None

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id")
      eventCreated.details  shouldBe SampleStatusChange(Some(SampleStatus.MOVED_TO_ACT), None)
    }

    "update case sample status from None" in {
      // Given
      val operator: Operator = Operator("operator-id", None)
      val originalCase       = aCase.copy(sample = aCase.sample.copy(status = None))
      val caseUpdated        = aCase.copy(sample = aCase.sample.copy(status = Some(SampleStatus.DESTROYED)))

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))

      // When Then
      await(service.updateSampleStatus(originalCase, Some(SampleStatus.DESTROYED), operator)) shouldBe caseUpdated

      verify(audit).auditSampleStatusChange(refEq(originalCase), refEq(caseUpdated), refEq(operator))(
        any[HeaderCarrier]
      )

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.sample.status shouldBe Some(SampleStatus.DESTROYED)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id")
      eventCreated.details  shouldBe SampleStatusChange(None, Some(SampleStatus.DESTROYED))
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalCase       = aCase.copy(sample = aCase.sample.copy(status = Some(SampleStatus.MOVED_TO_ELM)))

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.updateSampleStatus(originalCase, None, operator))
      }

      verifyNoMoreInteractions(audit)
      verify(connector, never()).createEvent(refEq(aCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase       = aCase.copy(sample = Sample())
      val caseUpdated        = aCase.copy(sample = Sample())

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(failed(new RuntimeException()))

      // When Then
      await(service.updateSampleStatus(originalCase, None, operator)) shouldBe caseUpdated

      verify(audit).auditSampleStatusChange(refEq(originalCase), refEq(caseUpdated), refEq(operator))(
        any[HeaderCarrier]
      )

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.sample.status shouldBe None
    }
  }

}
