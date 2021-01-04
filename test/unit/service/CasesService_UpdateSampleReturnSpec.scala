/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

class CasesService_UpdateSampleReturnSpec extends ServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

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
    new CasesService(realAppConfig, audit, emailService, fileStoreService, countriesService, reportingService, pdfService, connector, rulingConnector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit)
  }

  "Update Sample Return" should {

    "update case sample return" in {
      // Given
      val operator: Operator = Operator("operator-id", None)
      val originalCase       = aCase.copy(sample = aCase.sample.copy(returnStatus = Some(SampleReturn.TO_BE_CONFIRMED)))
      val caseUpdated        = aCase.copy(sample = aCase.sample.copy(returnStatus = Some(SampleReturn.YES)))

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))

      // When Then
      await(service.updateSampleReturn(originalCase, Some(SampleReturn.YES), operator)) shouldBe caseUpdated

      verify(audit).auditSampleReturnChange(refEq(originalCase), refEq(caseUpdated), refEq(operator))(
        any[HeaderCarrier]
      )

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.sample.returnStatus shouldBe Some(SampleReturn.YES)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id")
      eventCreated.details  shouldBe SampleReturnChange(Some(SampleReturn.TO_BE_CONFIRMED), Some(SampleReturn.YES))
    }

  }

}
