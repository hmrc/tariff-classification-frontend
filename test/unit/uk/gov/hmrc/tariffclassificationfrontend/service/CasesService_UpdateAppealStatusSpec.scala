/*
 * Copyright 2019 HM Revenue & Customs
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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
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
import uk.gov.tariffclassificationfrontend.utils.Cases._

import scala.concurrent.Future.{failed, successful}

class CasesService_UpdateAppealStatusSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with ConnectorCaptor {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val connector = mock[BindingTariffClassificationConnector]
  private val rulingConnector = mock[RulingConnector]
  private val emailService = mock[EmailService]
  private val fileStoreService = mock[FileStoreService]
  private val audit = mock[AuditService]
  private val config = mock[AppConfig]

  private val service = new CasesService(config, audit, emailService, fileStoreService, connector, rulingConnector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit, config)
  }

  "Add Appeal" should {
    "throw exception on missing decision" in {
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase(withoutDecision())


      intercept[RuntimeException] {
        await(service.addAppeal(originalCase, AppealType.REVIEW, AppealStatus.ALLOWED, operator))
      }

      verifyZeroInteractions(audit)
      verifyZeroInteractions(connector)
    }

    "update case with new appeal" in {
      // Given
      val existingAppeal = mock[Appeal]
      val operator: Operator = Operator("operator-id", None)
      val originalCase = aCase(withDecision(appeal = Seq(existingAppeal)))
      val caseUpdated = mock[Case]

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(successful(mock[Event]))

      // When Then
      await(service.addAppeal(originalCase, AppealType.REVIEW, AppealStatus.ALLOWED, operator)) shouldBe caseUpdated

      verify(audit).auditCaseAppealAdded(refEq(caseUpdated), any[Appeal], refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      val appealsUpdated = caseUpdating.decision.map(_.appeal).getOrElse(Seq.empty)
      appealsUpdated should have(size(2))
      appealsUpdated should contain(existingAppeal)
      appealsUpdated.exists(a => a.status == AppealStatus.ALLOWED && a.`type` ==  AppealType.REVIEW) shouldBe true

      val eventCreated = theEventCreatedFor(connector, caseUpdated)
      eventCreated.operator shouldBe Operator("operator-id")
      eventCreated.details shouldBe AppealAdded(appealType = AppealType.REVIEW, appealStatus = AppealStatus.ALLOWED)

      val appealAudited = theAppealAudited()
      appealAudited.`type` shouldBe AppealType.REVIEW
      appealAudited.status shouldBe AppealStatus.ALLOWED
    }

    "not create event on update failure" in {
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase(withDecision())

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.addAppeal(originalCase, AppealType.APPEAL_TIER_1, AppealStatus.ALLOWED, operator))
      }

      verifyZeroInteractions(audit)
      verify(connector, never()).createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {
      // Given
      val operator: Operator = Operator("operator-id")
      val originalCase = aCase(withDecision())
      val caseUpdated = mock[Case]

      given(connector.updateCase(any[Case])(any[HeaderCarrier])).willReturn(successful(caseUpdated))
      given(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      // When Then
      await(service.addAppeal(originalCase, AppealType.APPEAL_TIER_1, AppealStatus.DISMISSED, operator)) shouldBe caseUpdated

      verify(audit).auditCaseAppealAdded(refEq(caseUpdated), any[Appeal], refEq(operator))(any[HeaderCarrier])

      val caseUpdating = theCaseUpdating(connector)
      val appealsUpdated = caseUpdating.decision.map(_.appeal).getOrElse(Seq.empty)
      appealsUpdated should have(size(1))
      appealsUpdated.exists(a => a.status == AppealStatus.DISMISSED && a.`type` ==  AppealType.APPEAL_TIER_1) shouldBe true

      val appealAudited = theAppealAudited()
      appealAudited.`type` shouldBe AppealType.APPEAL_TIER_1
      appealAudited.status shouldBe AppealStatus.DISMISSED
    }
  }

  private def theAppealAudited(): Appeal = {
    val captor: ArgumentCaptor[Appeal] = ArgumentCaptor.forClass(classOf[Appeal])
    verify(audit).auditCaseAppealAdded(any[Case], captor.capture(), any[Operator])(any[HeaderCarrier])
    captor.getValue
  }

}
