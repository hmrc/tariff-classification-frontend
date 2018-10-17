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

package unit.uk.gov.hmrc.tariffclassificationfrontend.service

import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.connector.CasesConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService

import scala.concurrent.Future

class CasesServiceSpec extends UnitSpec with MockitoSugar {

  private implicit val hc = HeaderCarrier()

  private val c = mock[Case]
  private val connector = mock[CasesConnector]

  private val service = new CasesService(connector)

  "Get All Cases" should {

    "retrieve connector cases" in {
      given(connector.getGatewayCases).willReturn(Future.successful(Seq(c)))
      await(service.getAllCases) shouldBe Seq(c)
    }
  }

}
