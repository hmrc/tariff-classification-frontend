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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.apache.http.HttpStatus
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.CasesConnector
import unit.uk.gov.hmrc.tariffclassificationfrontend.connector.Payloads.caseExample

class CasesConnectorSpec extends UnitSpec with WiremockTestServer with MockitoSugar with WithFakeApplication {

  private val configuration = mock[AppConfig]

  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val client = new DefaultHttpClient(fakeApplication.configuration, auditConnector, wsClient)
  private implicit val hc = HeaderCarrier()

  private val connector = new CasesConnector(configuration, client)

  "Connector" should {

    val url = "/cases?queue_id=none&assignee_id=none&sort-by=elapsed-days"

    "get empty cases" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.getGatewayCases) shouldBe Seq()
    }

    "get non-empty cases" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(Payloads.gatewayCases))
      )

      await(connector.getGatewayCases) shouldBe Seq(caseExample)
    }

  }

}
