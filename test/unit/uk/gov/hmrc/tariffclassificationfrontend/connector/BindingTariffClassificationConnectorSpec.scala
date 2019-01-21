/*
 * Copyright 2019 HM Revenue & Customs
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

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.client.WireMock._
import org.apache.http.HttpStatus
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Queue
import uk.gov.tariffclassificationfrontend.utils._

class BindingTariffClassificationConnectorSpec extends UnitSpec
  with WiremockTestServer with MockitoSugar with WithFakeApplication {

  import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.{caseFormat, eventFormat, newEventRequestFormat}

  private val configuration = mock[AppConfig]

  private val actorSystem = ActorSystem("test")
  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val client = new DefaultHttpClient(fakeApplication.configuration, auditConnector, wsClient, actorSystem)
  private val gatewayQueue = Queue("1", "gateway", "Gateway")
  private val otherQueue = Queue("2", "other", "Other")
  private implicit val hc = HeaderCarrier()

  private val connector = new BindingTariffClassificationConnector(configuration, client)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    given(configuration.bindingTariffClassificationUrl).willReturn(getUrl)
  }

  "Connector 'Get Cases By Queue'" should {

    "get empty cases in 'gateway' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.findCasesByQueue(gatewayQueue)) shouldBe Seq()
    }

    "get cases in 'gateway' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.findCasesByQueue(gatewayQueue)) shouldBe Seq(Cases.btiCaseExample)
    }

    "get empty cases in 'other' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.findCasesByQueue(otherQueue)) shouldBe Seq()
    }

    "get cases in 'other' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.findCasesByQueue(otherQueue)) shouldBe Seq(Cases.btiCaseExample)
    }
  }

  "Connector 'Get One'" should {

    "get an unknown case" in {
      stubFor(get(urlEqualTo("/cases/id"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_NOT_FOUND))
      )

      await(connector.findCase("id")) shouldBe None
    }

    "get a case" in {
      stubFor(get(urlEqualTo("/cases/id"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.btiCase))
      )

      await(connector.findCase("id")) shouldBe Some(Cases.btiCaseExample)
    }

  }

  "Connector 'Get Cases By Assignee'" should {

    "get empty cases" in {
      stubFor(get(urlEqualTo("/cases?assignee_id=assignee&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.findCasesByAssignee("assignee")) shouldBe Seq()
    }

    "get cases" in {
      stubFor(get(urlEqualTo("/cases?assignee_id=assignee&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.findCasesByAssignee("assignee")) shouldBe Seq(Cases.btiCaseExample)
    }
  }

  "Connector 'Update Case'" should {

    "update valid case" in {
      val ref = "case-reference"
      val validCase = Cases.btiCaseExample.copy(reference = ref)
      val json = Json.toJson(validCase).toString()

      stubFor(put(urlEqualTo(s"/cases/$ref"))
        .withRequestBody(equalToJson(json))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(json)
        )
      )

      await(connector.updateCase(validCase)) shouldBe validCase
    }

    "update with an unknown case reference" in {
      val unknownRef = "unknownRef"
      val unknownCase = Cases.btiCaseExample.copy(reference = unknownRef)
      val json = Json.toJson(unknownCase).toString()

      stubFor(put(urlEqualTo(s"/cases/$unknownRef"))
        .withRequestBody(equalToJson(json))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_NOT_FOUND)
        )
      )

      intercept[NotFoundException] {
        await(connector.updateCase(unknownCase))
      }
    }
  }

  "Connector 'Create Event'" should {

    "create event" in {
      val ref = "case-reference"
      val validCase = Cases.btiCaseExample.copy(reference = ref)
      val validEventRequest = Events.eventRequest
      val validEvent = Events.event.copy(caseReference = ref)
      val requestJson = Json.toJson(validEventRequest).toString()
      val responseJson = Json.toJson(validEvent).toString()

      stubFor(post(urlEqualTo(s"/cases/$ref/events"))
        .withRequestBody(equalToJson(requestJson))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(responseJson)
        )
      )

      await(connector.createEvent(validCase, validEventRequest)) shouldBe validEvent
    }

    "create event with an unknown case reference" in {
      val ref = "unknown-reference"
      val validCase = Cases.btiCaseExample.copy(reference = ref)
      val validEventRequest = Events.eventRequest
      val requestJson = Json.toJson(validEventRequest).toString()

      stubFor(post(urlEqualTo(s"/cases/$ref/events"))
        .withRequestBody(equalToJson(requestJson))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_NOT_FOUND)
        )
      )

      intercept[NotFoundException] {
        await(connector.createEvent(validCase, validEventRequest))
      }
    }

  }

  "Connector find events" should {
    val ref = "id"

    "return a list of Events for this case" in {

      stubFor(get(urlEqualTo(s"/cases/$ref/events"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(EventPayloads.events))
      )

      await(connector.findEvents(ref)) shouldBe Events.events
    }

    "returns empty list when case ref not found" in {
      stubFor(get(urlEqualTo(s"/cases/$ref/events"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.findEvents(ref)) shouldBe Seq()
    }
  }

}
