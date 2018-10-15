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

import org.assertj.core.api.Assertions._
import org.mockito.BDDMockito._
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.tariffclassificationfrontend.connector.CasesConnector

class CasesServiceSpec extends FlatSpec with MockitoSugar {

  val cases = Seq()
  val connector = mock[CasesConnector]

  "Get All Cases" should "retrieve connector cases" in {
    given(connector.getAllCases()).willReturn(cases)

    val service = new CasesService(connector)
    val response = service.getAllCases()

    assertThat(response) isEqualTo cases
  }

}
