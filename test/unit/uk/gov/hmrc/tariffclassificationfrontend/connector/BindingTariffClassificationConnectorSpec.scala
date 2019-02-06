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

import java.net.URLEncoder
import java.time.{Clock, LocalDate, ZoneOffset}

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
import uk.gov.hmrc.tariffclassificationfrontend.models._
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
  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val currentTime = LocalDate.of(2019,1,1).atStartOfDay().toInstant(ZoneOffset.UTC)
  private implicit val clock: Clock = Clock.fixed(currentTime, ZoneOffset.UTC)

  private val connector = new BindingTariffClassificationConnector(configuration, client)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    given(configuration.bindingTariffClassificationUrl).willReturn(getUrl)
  }

  "Connector 'Get Cases By Queue'" should {

    "get empty cases in 'gateway' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.findCasesByQueue(gatewayQueue)) shouldBe Seq()
    }

    "get cases in 'gateway' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.findCasesByQueue(gatewayQueue)) shouldBe Seq(Cases.btiCaseExample)
    }

    "get empty cases in 'other' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.findCasesByQueue(otherQueue)) shouldBe Seq()
    }

    "get cases in 'other' queue" in {
      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc"))
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
      stubFor(get(urlEqualTo("/cases?assignee_id=assignee&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.findCasesByAssignee(Operator("assignee"))) shouldBe Seq()
    }

    "get cases" in {
      stubFor(get(urlEqualTo("/cases?assignee_id=assignee&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      await(connector.findCasesByAssignee(Operator("assignee"))) shouldBe Seq(Cases.btiCaseExample)
    }
  }

  "Connector 'Search'" should {

    def encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    "handle no filters" in {
      stubFor(get(urlEqualTo("/cases?sort_direction=desc&sort_by=commodity-code"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody("[]"))
      )

      await(connector.search(Search(), Sort())) shouldBe Seq()
    }

    "filter by all" in {
      val url = s"/cases" +
        s"?sort_direction=asc" +
        s"&sort_by=commodity-code" +
        s"&trader_name=trader" +
        s"&commodity_code=comm-code" +
        s"&min_decision_end=${encode("2019-01-01T00:00:00Z")}" +
        s"&status=COMPLETED"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      val search = Search(
        traderName = Some("trader"),
        commodityCode = Some("comm-code"),
        liveRulingsOnly = Some(true)
      )
      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE))) shouldBe Seq(Cases.btiCaseExample)
    }

    "filter by 'trader name'" in {
      stubFor(get(urlEqualTo(s"/cases?sort_direction=asc&sort_by=commodity-code&trader_name=trader"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      val search = Search(traderName = Some("trader"))
      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE))) shouldBe Seq(Cases.btiCaseExample)
    }

    "filter by 'commodity code'" in {
      stubFor(get(urlEqualTo(s"/cases?sort_direction=asc&sort_by=commodity-code&commodity_code=comm-code"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      val search = Search(
        commodityCode = Some("comm-code")
      )
      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE))) shouldBe Seq(Cases.btiCaseExample)
    }

    "get cases 'live only' = false" in {
      stubFor(get(urlEqualTo(s"/cases?sort_direction=asc&sort_by=commodity-code"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      val search = Search(
        liveRulingsOnly = Some(false)
      )
      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE))) shouldBe Seq(Cases.btiCaseExample)
    }

    "get cases 'live only' = none" in {
      stubFor(get(urlEqualTo(s"/cases?sort_direction=asc&sort_by=commodity-code"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      val search = Search(
        liveRulingsOnly = None
      )
      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE))) shouldBe Seq(Cases.btiCaseExample)
    }

    "get cases 'live only' = true" in {
      stubFor(get(urlEqualTo(s"/cases?sort_direction=asc&sort_by=commodity-code&min_decision_end=${encode("2019-01-01T00:00:00Z")}&status=COMPLETED"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.gatewayCases))
      )

      val search = Search(
        liveRulingsOnly = Some(true)
      )
      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE))) shouldBe Seq(Cases.btiCaseExample)
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
