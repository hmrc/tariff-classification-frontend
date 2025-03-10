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
import org.mockito.Mockito.{reset, verify}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future.successful

class CasesService_UpdateSampleReturnSpec extends CasesServiceSpecBase with BeforeAndAfterEach with ConnectorCaptor {

  private val aCase = Cases.btiCaseExample

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
    reset(audit)
  }

  "Update Sample Return" should {

    "update case sample return" in {

      val operator: Operator = Operator("operator-id", None)
      val originalCase       = aCase.copy(sample = aCase.sample.copy(returnStatus = Some(SampleReturn.TO_BE_CONFIRMED)))
      val caseUpdated        = aCase.copy(sample = aCase.sample.copy(returnStatus = Some(SampleReturn.YES)))

      when(connector.updateCase(any[Case])(any[HeaderCarrier])).thenReturn(successful(caseUpdated))
      when(connector.createEvent(refEq(caseUpdated), any[NewEventRequest])(any[HeaderCarrier]))
        .thenReturn(successful(mock[Event]))

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
