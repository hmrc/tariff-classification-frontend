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

package unit.uk.gov.hmrc.tariffclassificationfrontend.connector

import java.util.concurrent.TimeUnit

import com.github.tomakehurst.wiremock.client.WireMock._
import org.assertj.core.api.Assertions._
import org.scalatest.FlatSpec
import uk.gov.hmrc.tariffclassificationfrontend.connector.{CasesConnector, ConnectorHttpClient}
import uk.gov.hmrc.tariffclassificationfrontend.models.Case

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CasesConnectorSpec extends FlatSpec with WiremockTestServer {

  private val client = new ConnectorHttpClient(None, StandaloneWSClient.client)

  "Connector" should "get all cases" in {
    stubFor(get(urlEqualTo("/binding-tariff-classification/cases"))
      .willReturn(aResponse()
        .withStatus(200)
        .withBody("[]"))
    )

    val connector = new CasesConnector("http://localhost:20001", client)
    val response = connector.getAllCases()

    val cases: Seq[Case] = Await.result(response, Duration(2, TimeUnit.SECONDS))
    assertThat(cases.size).isZero
  }

}
