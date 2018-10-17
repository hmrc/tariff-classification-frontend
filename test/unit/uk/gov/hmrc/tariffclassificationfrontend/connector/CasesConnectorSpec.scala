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
import org.apache.http.HttpStatus
import org.assertj.core.api.Assertions._
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
import uk.gov.hmrc.tariffclassificationfrontend.models.Case

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CasesConnectorSpec extends UnitSpec with WiremockTestServer with MockitoSugar with WithFakeApplication {

  private val configuration = mock[AppConfig]

  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val client = new DefaultHttpClient(fakeApplication.configuration, auditConnector, wsClient)
  private implicit val hc = HeaderCarrier()

  private val connector = new CasesConnector(configuration, client)

  "Connector" should {

    "get empty cases" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      val response = connector.getGatewayCases

      val cases: Seq[Case] = Await.result(response, Duration(1, TimeUnit.SECONDS))
      assertThat(cases.size).isZero
    }

    "get non-empty cases" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(Payloads.gatewayCases))
      )

      val response = connector.getGatewayCases

      val cases: Seq[Case] = Await.result(response, Duration(1, TimeUnit.SECONDS))
      assertThat(cases.size).isOne
    }

  }

}
