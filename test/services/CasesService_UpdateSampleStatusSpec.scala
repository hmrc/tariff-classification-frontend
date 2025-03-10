/*
 * Copyright 2025 HM Revenue & Customs
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

import models._
import models.request.NewEventRequest
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify, verifyNoMoreInteractions}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future.{failed, successful}

class CasesService_UpdateSampleStatusSpec extends CasesServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val aCase = Cases.btiCaseExample

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
    reset(audit)
  }

  "Update Sample Status" should {

    "update case sample status to None" in {

      val operator: Operator = Operator("operator-id", None)
      val originalCase       = aCase.copy(sample = aCase.sample.copy(status = Some(SampleStatus.MOVED_TO_ACT)))
      val caseUpdated        = aCase.copy(sample = aCase.sample.copy(status = None))

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(successful(mock[Event]))

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

      val operator: Operator = Operator("operator-id", None)
      val originalCase       = aCase.copy(sample = aCase.sample.copy(status = None))
      val caseUpdated        = aCase.copy(sample = aCase.sample.copy(status = Some(SampleStatus.DESTROYED)))

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(successful(mock[Event]))

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

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.updateSampleStatus(originalCase, None, operator))
      }

      verifyNoMoreInteractions(audit)
      verify(connector, never()).createEvent(refEq(aCase), any[NewEventRequest])(any[HeaderCarrier])
    }

    "succeed on event create failure" in {

      val operator: Operator = Operator("operator-id")
      val originalCase       = aCase.copy(sample = Sample())
      val caseUpdated        = aCase.copy(sample = Sample())

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(failed(new RuntimeException()))

      await(service.updateSampleStatus(originalCase, None, operator)) shouldBe caseUpdated

      verify(audit).auditSampleStatusChange(refEq(originalCase), refEq(caseUpdated), refEq(operator))(
        any[HeaderCarrier]
      )

      val caseUpdating = theCaseUpdating(connector)
      caseUpdating.sample.status shouldBe None
    }
  }

}
