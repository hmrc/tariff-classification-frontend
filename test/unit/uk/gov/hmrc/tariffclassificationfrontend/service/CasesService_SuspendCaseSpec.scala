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

package uk.gov.hmrc.tariffclassificationfrontend.service

import org.mockito.ArgumentMatchers.{refEq, _}
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify, verifyZeroInteractions}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditService
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.{BindingTariffClassificationConnector, RulingConnector}
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.{failed, successful}

class CasesService_SuspendCaseSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with ConnectorCaptor {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val manyCases = mock[Seq[Case]]
  private val oneCase = mock[Option[Case]]
  private val connector = mock[BindingTariffClassificationConnector]
  private val rulingConnector = mock[RulingConnector]
  private val emailService = mock[EmailService]
  private val fileStoreService = mock[FileStoreService]
  private val reportingService = mock[ReportingService]
  private val audit = mock[AuditService]
  private val config = mock[AppConfig]
  private val aCase = Cases.btiCaseExample

  private val service = new CasesService(config, audit, emailService, fileStoreService, reportingService, connector, rulingConnector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit, oneCase, manyCases, config)
  }

  "Suspend a Case" should {
    "update case status to SUSPENDED" in {
      // Given
      val existingAttachment = mock[Attachment]

      val fileUpload = mock[FileUpload]
      val fileUploaded = FileStoreAttachment("id", "email", "application/pdf", 0)
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalCase = aCase.copy(status = CaseStatus.NEW, attachments = Seq(existingAttachment))
      val caseUpdated = aCase.copy(status = CaseStatus.SUSPENDED)

      given(fileStoreService.upload(fileUpload)).willReturn(successful(fileUploaded))
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(successful(mock[Event]))

      // When Then
      await(service.suspendCase(originalCase, fileUpload, "note", operator)) shouldBe caseUpdated

      verify(audit).auditCaseSuspended(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.SUSPENDED
      caseUpdating.attachments should have(size(2))

      val attachmentUpdating = caseUpdating.attachments.find(_.id == "id")
      attachmentUpdating.map(_.id) shouldBe Some("id")
      attachmentUpdating.map(_.public) shouldBe Some(false)
      attachmentUpdating.flatMap(_.operator) shouldBe Some(operator)

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id", Some("Billy Bobbins"))
      eventCreated.details shouldBe CaseStatusChange(CaseStatus.NEW, CaseStatus.SUSPENDED, Some("note"), Some("id"))
    }

    "fail to update on attachment upload failure" in {
      // Given
      val fileUpload = mock[FileUpload]
      val operator: Operator = Operator("operator-id", Some("Billy Bobbins"))
      val originalCase = aCase.copy(status = CaseStatus.NEW)

      given(fileStoreService.upload(fileUpload)).willReturn(failed(new RuntimeException("Error")))

      // When Then
      intercept[RuntimeException] {
        await(service.suspendCase(originalCase, fileUpload, "note", operator))
      }.getMessage shouldBe "Error"
    }

    "not create event on update failure" in {
      val fileUpload = mock[FileUpload]
      val fileUploaded = FileStoreAttachment("id", "email", "application/pdf", 0)
      val operator: Operator = Operator("operator-id")
      val caseUpdated = aCase.copy(status = CaseStatus.SUSPENDED)

      given(fileStoreService.upload(fileUpload)).willReturn(successful(fileUploaded))
      given(connector.updateCase(refEq(caseUpdated))(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.suspendCase(caseUpdated, fileUpload, "note", operator))
      }

      verifyZeroInteractions(audit)
      verify(connector, never()).createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val fileUpload = mock[FileUpload]
      val fileUploaded = FileStoreAttachment("id", "email", "application/pdf", 0)
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase.copy(status = CaseStatus.NEW)
      val caseUpdated = aCase.copy(status = CaseStatus.SUSPENDED)

      given(fileStoreService.upload(fileUpload)).willReturn(successful(fileUploaded))
      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      // When Then
      await(service.suspendCase(originalCase, fileUpload, "note", operator)) shouldBe caseUpdated

      verify(audit).auditCaseSuspended(refEq(originalCase), refEq(caseUpdated), refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.status shouldBe CaseStatus.SUSPENDED
    }
  }

}
