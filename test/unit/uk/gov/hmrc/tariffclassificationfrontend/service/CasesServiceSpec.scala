/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Queue}

import scala.concurrent.Future

class CasesServiceSpec extends UnitSpec with MockitoSugar {

  private implicit val hc = HeaderCarrier()
  private val manyCases = mock[Seq[Case]]
  private val oneCase = mock[Option[Case]]
  private val queue = mock[Queue]
  private val connector = mock[BindingTariffClassificationConnector]

  private val service = new CasesService(connector)

  "Get Cases 'By Queue'" should {

    "retrieve connector cases" in {
      given(connector.getCasesByQueue(queue)) willReturn Future.successful(manyCases)

      await(service.getCasesByQueue(queue)) shouldBe manyCases
    }
  }

  "Get Cases 'By Assignee'" should {

    "retrieve connector cases" in {
      given(connector.getCasesByAssignee("assignee")) willReturn Future.successful(manyCases)

      await(service.getCasesByAssignee("assignee")) shouldBe manyCases
    }
  }

  "Get One Case 'By Reference'" should {

    "retrieve connector case" in {
      given(connector.getOneCase("reference")) willReturn Future.successful(oneCase)

      await(service.getOne("reference")) shouldBe oneCase
    }
  }

  "Release Case" should {
    val oldCase = mock[Case]
    val updatedCase = mock[Case]
    val updatedPersistedCase = mock[Case]

    "update case queue_id and status to NEW" in {
      given(queue.id).willReturn("queue_id")
      given(oldCase.copy(status= "OPEN", queueId = Some("queue_id"))).willReturn(updatedCase)
      given(connector.updateCase(updatedCase)).willReturn(Future.successful(updatedPersistedCase))

      await(service.releaseCase(oldCase, queue)) shouldBe updatedPersistedCase
    }
  }

}
