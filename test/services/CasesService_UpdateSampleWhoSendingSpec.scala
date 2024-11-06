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
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.templates.cover_letter_template

import scala.concurrent.Future.successful

class CasesService_UpdateSampleWhoSendingSpec extends ServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val connector             = mock[BindingTariffClassificationConnector]
  private val rulingConnector       = mock[RulingConnector]
  private val emailService          = mock[EmailService]
  private val fileStoreService      = mock[FileStoreService]
  private val countriesService      = mock[CountriesService]
  private val reportingService      = mock[ReportingService]
  private val pdfService            = mock[PdfService]
  private val audit                 = mock[AuditService]
  private val cover_letter_template = mock[cover_letter_template]
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
      cover_letter_template
    )(executionContext, realAppConfig)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
    reset(audit)
  }

  "Update Sample Who Sending" should {

    "update case who is sending the sample" in {

      val operator: Operator = Operator("operator-id", None)
      val originalCase       = aCase.copy(sample = aCase.sample.copy(whoIsSending = Some(SampleSend.TRADER)))
      val caseUpdated        = aCase.copy(sample = aCase.sample.copy(whoIsSending = Some(SampleSend.AGENT)))

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))

      await(service.updateWhoSendSample(originalCase, Some(SampleSend.AGENT), operator)) shouldBe caseUpdated

      verify(audit).auditSampleSendChange(refEq(originalCase), refEq(caseUpdated), refEq(operator))(
        any[HeaderCarrier]
      )

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.sample.whoIsSending shouldBe Some(SampleSend.AGENT)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id")
      eventCreated.details  shouldBe SampleSendChange(Some(SampleSend.TRADER), Some(SampleSend.AGENT))
    }

  }

}
