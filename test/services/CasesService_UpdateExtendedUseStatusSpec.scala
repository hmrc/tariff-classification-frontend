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
import models._
import models.request.NewEventRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify, verifyNoMoreInteractions}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases._
import views.html.templates.cover_letter_template

import scala.concurrent.Future.{failed, successful}

class CasesService_UpdateExtendedUseStatusSpec extends ServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val connector        = mock[BindingTariffClassificationConnector]
  private val rulingConnector  = mock[RulingConnector]
  private val emailService     = mock[EmailService]
  private val fileStoreService = mock[FileStoreService]
  private val countriesService = mock[CountriesService]
  private val reportingService = mock[ReportingService]
  private val pdfService       = mock[PdfService]
  private val audit            = mock[AuditService]
  private val cover_letter_template = mock[cover_letter_template]

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
      cover_letter_template
    )(executionContext, realAppConfig)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
    reset(audit)
  }

  "Update Extended Use Status" should {
    "update case 'extended use' status" in {

      val operator: Operator = Operator("operator-id", None)
      val originalCase = aCase(
        withDecision(cancellation =
          Some(Cancellation(reason = CancelReason.ANNULLED, applicationForExtendedUse = true))
        )
      )
      val caseUpdated = aCase(
        withDecision(cancellation = Some(Cancellation(reason = CancelReason.ANNULLED)))
      )

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))

      await(service.updateExtendedUseStatus(originalCase, status = false, operator)) shouldBe caseUpdated

      verify(audit).auditCaseExtendedUseChange(refEq(originalCase), refEq(caseUpdated), refEq(operator))(
        any[HeaderCarrier]
      )

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.decision.flatMap(_.cancellation).map(_.applicationForExtendedUse) shouldBe Some(false)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id")
      eventCreated.details  shouldBe ExtendedUseStatusChange(from = true, to = false)
    }

    "throw exception on missing decision" in {
      val operator: Operator = Operator("operator-id")
      val originalCase       = aCase(withoutDecision())

      intercept[RuntimeException] {
        await(service.updateExtendedUseStatus(originalCase, status = false, operator))
      }

      verifyNoMoreInteractions(audit)
      verifyNoMoreInteractions(connector)
    }

    "throw exception on missing cancellation" in {
      val operator: Operator = Operator("operator-id")
      val originalCase       = aCase(withDecision(cancellation = None))

      intercept[RuntimeException] {
        await(service.updateExtendedUseStatus(originalCase, status = false, operator))
      }

      verifyNoMoreInteractions(audit)
      verifyNoMoreInteractions(connector)
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase(
        withDecision(cancellation =
          Some(Cancellation(reason = CancelReason.ANNULLED, applicationForExtendedUse = true))
        )
      )

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.updateExtendedUseStatus(originalCase, status = false, operator))
      }

      verifyNoMoreInteractions(audit)
      verify(connector, never()).createEvent(refEq(originalCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {

      val operator: Operator = Operator("operator-id")
      val originalCase = aCase(
        withDecision(cancellation = Some(Cancellation(reason = CancelReason.ANNULLED)))
      )
      val caseUpdated = aCase(
        withDecision(cancellation =
          Some(Cancellation(reason = CancelReason.ANNULLED, applicationForExtendedUse = true))
        )
      )

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(failed(new RuntimeException()))

      await(service.updateExtendedUseStatus(originalCase, status = true, operator)) shouldBe caseUpdated

      verify(audit).auditCaseExtendedUseChange(refEq(originalCase), refEq(caseUpdated), refEq(operator))(
        any[HeaderCarrier]
      )

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.decision.flatMap(_.cancellation).map(_.applicationForExtendedUse) shouldBe Some(true)
    }
  }

}
