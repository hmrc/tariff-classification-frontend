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

package uk.gov.hmrc.tariffclassificationfrontend.connector

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
import uk.gov.hmrc.tariffclassificationfrontend.models.Queue
import uk.gov.hmrc.tariffclassificationfrontend.utils.{CaseExamples, CasePayloads}

class BindingTariffClassificationConnectorSpec extends UnitSpec with WiremockTestServer with MockitoSugar with WithFakeApplication {

  private val configuration = mock[AppConfig]

  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val client = new DefaultHttpClient(fakeApplication.configuration, auditConnector, wsClient)
  private val gatewayQueue = Queue(1, "gateway", "Gateway")
  private val otherQueue = Queue(2, "other", "Other")
  private implicit val hc = HeaderCarrier()

  private val connector = new BindingTariffClassificationConnector(configuration, client)

  "Connector 'Get Cases By Queue'" should {

    "get empty cases in 'gateway' queue" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.getCasesByQueue(gatewayQueue)) shouldBe Seq()
    }

    "get cases in 'gateway' queue" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.getCasesByQueue(gatewayQueue)) shouldBe Seq(CaseExamples.btiCaseExample)
    }

    "get empty cases in 'other' queue" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.getCasesByQueue(otherQueue)) shouldBe Seq()
    }

    "get cases in 'other' queue" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.getCasesByQueue(otherQueue)) shouldBe Seq(CaseExamples.btiCaseExample)
    }
  }

  "Connector 'Get One'" should {

    "get an unknown case" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo("/cases/id"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_NOT_FOUND))
      )

      await(connector.getOneCase("id")) shouldBe None
    }

    "get a case" in {
      given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

      stubFor(get(urlEqualTo("/cases/id"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.btiCase))
      )

      await(connector.getOneCase("id")) shouldBe Some(CaseExamples.btiCaseExample)
    }

  }

}
