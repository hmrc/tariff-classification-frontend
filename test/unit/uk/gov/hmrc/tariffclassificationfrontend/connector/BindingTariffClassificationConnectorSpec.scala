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
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Queue}
import uk.gov.hmrc.tariffclassificationfrontend.utils.{CaseExamples, CasePayloads}

class BindingTariffClassificationConnectorSpec extends UnitSpec with WiremockTestServer with MockitoSugar with WithFakeApplication {

  import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.caseFormat

  private val configuration = mock[AppConfig]

  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val client = new DefaultHttpClient(fakeApplication.configuration, auditConnector, wsClient)
  private val gatewayQueue = Queue(1, "gateway", "Gateway")
  private val otherQueue = Queue(2, "other", "Other")
  private implicit val hc = HeaderCarrier()

  given(configuration.bindingTariffClassificationUrl).willReturn("http://localhost:20001")

  private val connector = new BindingTariffClassificationConnector(configuration, client)

  "Connector 'Get Cases By Queue'" should {

    "get empty cases in 'gateway' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.getCasesByQueue(gatewayQueue)) shouldBe Seq()
    }

    "get cases in 'gateway' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.getCasesByQueue(gatewayQueue)) shouldBe Seq(CaseExamples.btiCaseExample)
    }

    "get empty cases in 'other' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.getCasesByQueue(otherQueue)) shouldBe Seq()
    }

    "get cases in 'other' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.getCasesByQueue(otherQueue)) shouldBe Seq(CaseExamples.btiCaseExample)
    }
  }

  "Connector 'Get One'" should {

    "get an unknown case" in {
      stubFor(get(urlEqualTo("/cases/id"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_NOT_FOUND))
      )

      await(connector.getOneCase("id")) shouldBe None
    }

    "get a case" in {
      stubFor(get(urlEqualTo("/cases/id"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.btiCase))
      )

      await(connector.getOneCase("id")) shouldBe Some(CaseExamples.btiCaseExample)
    }

  }

  "Connector 'Get Cases By Assignee'" should {

    "get empty cases" in {
      stubFor(get(urlEqualTo("/cases?assignee_id=assignee&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.getCasesByAssignee("assignee")) shouldBe Seq()
    }

    "get cases" in {
      stubFor(get(urlEqualTo("/cases?assignee_id=assignee&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.getCasesByAssignee("assignee")) shouldBe Seq(CaseExamples.btiCaseExample)
    }
  }

  "Connector 'Update Case'" should {

    val validRef = "case-reference"
    val validCase = CaseExamples.btiCaseExample.copy(reference = validRef)
    val json = Json.toJson(validCase).toString()

    "update valid case" in {
      stubFor(put(urlEqualTo(s"/cases/$validRef"))
        .withRequestBody(equalToJson(json))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(json)
        )
      )

      val result = await(connector.updateCase(validCase))

      result shouldBe validCase
    }
  }

}
