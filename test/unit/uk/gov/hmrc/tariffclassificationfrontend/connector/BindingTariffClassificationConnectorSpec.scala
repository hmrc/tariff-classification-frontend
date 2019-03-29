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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.apache.http.HttpStatus
import play.api.libs.json.Json
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.tariffclassificationfrontend.utils._

class BindingTariffClassificationConnectorSpec extends ConnectorTest {

  import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.{caseFormat, eventFormat, newEventRequestFormat}

  private val gatewayQueue = Queue("1", "gateway", "Gateway", "gateway")
  private val otherQueue = Queue("2", "other", "Other", "other")
  private val pagination = SearchPagination(1, 2)
  private val currentTime = LocalDate.of(2019,1,1).atStartOfDay().toInstant(ZoneOffset.UTC)
  private implicit val clock: Clock = Clock.fixed(currentTime, ZoneOffset.UTC)

  private val connector = new BindingTariffClassificationConnector(appConfig, authenticatedHttpClient)

  "Connector 'Get Cases By Queue'" should {

    "get empty cases in 'gateway' queue" in {
      val url = "/cases?application_type=BTI&queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedEmpty))
      )

      await(connector.findCasesByQueue(gatewayQueue, pagination)) shouldBe Paged.empty[Case]

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "get cases in 'gateway' queue" in {
      val url = "/cases?application_type=BTI&queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      await(connector.findCasesByQueue(gatewayQueue, pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "get empty cases in 'other' queue" in {
      val url = "/cases?application_type=BTI&queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedEmpty))
      )

      await(connector.findCasesByQueue(otherQueue, pagination)) shouldBe Paged.empty[Case]

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "get cases in 'other' queue" in {
      val url = "/cases?application_type=BTI&queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      await(connector.findCasesByQueue(otherQueue, pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

  "Connector 'Get One'" should {

    "get an unknown case" in {
      stubFor(get(urlEqualTo("/cases/id"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_NOT_FOUND))
      )

      await(connector.findCase("id")) shouldBe None

      verify(
        getRequestedFor(urlEqualTo("/cases/id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "get a case" in {
      stubFor(get(urlEqualTo("/cases/id"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.btiCase))
      )

      await(connector.findCase("id")) shouldBe Some(Cases.btiCaseExample)

      verify(
        getRequestedFor(urlEqualTo("/cases/id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

  }

  "Connector 'Get Cases By Assignee'" should {

    "get empty cases" in {
      val url = "/cases?application_type=BTI&assignee_id=assignee&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedEmpty))
      )

      await(connector.findCasesByAssignee(Operator("assignee"), pagination)) shouldBe Paged.empty[Case]

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "get cases" in {
      val url = "/cases?application_type=BTI&assignee_id=assignee&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      await(connector.findCasesByAssignee(Operator("assignee"), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

  "Connector 'Search'" should {

    def encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    "handle no filters" in {
      val url = "/cases?application_type=BTI&sort_direction=asc&sort_by=commodity-code&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedEmpty))
      )

      await(connector.search(Search(), Sort(), pagination)) shouldBe Paged.empty[Case]

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "filter by all" in {
      val url = s"/cases" +
        s"?application_type=BTI" +
        s"&sort_direction=asc" +
        s"&sort_by=commodity-code" +
        s"&trader_name=trader" +
        s"&commodity_code=comm-code" +
        s"&decision_details=decision-details" +
        s"&min_decision_end=${encode("2019-01-01T00:00:00Z")}" +
        s"&status=COMPLETED" +
        s"&keyword=K1" +
        s"&keyword=K2" +
        s"&page=1" +
        s"&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      val search = Search(
        traderName = Some("trader"),
        commodityCode = Some("comm-code"),
        decisionDetails = Some("decision-details"),
        liveRulingsOnly = Some(true),
        keywords = Some(Set("K1", "K2"))
      )

      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "filter by 'trader name'" in {
      val url = "/cases?application_type=BTI&sort_direction=asc&sort_by=commodity-code&trader_name=trader&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      val search = Search(traderName = Some("trader"))

      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "filter by 'commodity code'" in {
      val url = "/cases?application_type=BTI&sort_direction=asc&sort_by=commodity-code&commodity_code=comm-code&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      val search = Search(
        commodityCode = Some("comm-code")
      )

      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "filter by 'decision_details'" in {
      val url = s"/cases?application_type=BTI&sort_direction=asc&sort_by=commodity-code&decision_details=decision-details&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      val search = Search(
        decisionDetails = Some("decision-details")
      )

      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "filter by 'keyword'" in {
      val url = "/cases?application_type=BTI&sort_direction=asc&sort_by=commodity-code&keyword=K1&keyword=K2&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      val search = Search(
        keywords = Some(Set("K1", "K2"))
      )

      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "get cases 'live only' = false" in {
      val url = "/cases?application_type=BTI&sort_direction=asc&sort_by=commodity-code&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      val search = Search(
        liveRulingsOnly = Some(false)
      )

      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "get cases 'live only' = none" in {
      val url = "/cases?application_type=BTI&sort_direction=asc&sort_by=commodity-code&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      val search = Search(
        liveRulingsOnly = None
      )

      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "get cases 'live only' = true" in {
      val url = s"/cases?application_type=BTI&sort_direction=asc&sort_by=commodity-code&min_decision_end=${encode("2019-01-01T00:00:00Z")}&status=COMPLETED&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      val search = Search(
        liveRulingsOnly = Some(true)
      )

      await(connector.search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
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

      verify(
        putRequestedFor(urlEqualTo(s"/cases/$ref"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
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

      verify(
        putRequestedFor(urlEqualTo(s"/cases/$unknownRef"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
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

      verify(
        postRequestedFor(urlEqualTo(s"/cases/$ref/events"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
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

      verify(
        postRequestedFor(urlEqualTo(s"/cases/$ref/events"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

  }

  "Connector find events" should {
    val ref = "id"

    "return a list of Events for this case" in {

      stubFor(get(urlEqualTo(s"/cases/$ref/events?page=1&page_size=2"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(EventPayloads.pagedEvents))
      )

      await(connector.findEvents(ref, pagination)) shouldBe Paged(Events.events)

      verify(
        getRequestedFor(urlEqualTo(s"/cases/$ref/events?page=1&page_size=2"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "returns empty list when case ref not found" in {
      stubFor(get(urlEqualTo(s"/cases/$ref/events?page=1&page_size=2"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(EventPayloads.pagedEmpty))
      )

      await(connector.findEvents(ref, pagination)) shouldBe Paged.empty[Event]

      verify(
        getRequestedFor(urlEqualTo(s"/cases/$ref/events?page=1&page_size=2"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

  "Connector 'Get Assigned Cases'" should {

    "get assigned cases " in {
      val url = "/cases?application_type=BTI&assignee_id=some&status=OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=2"

      stubFor(get(urlEqualTo(url))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      await(connector.findAssignedCases(pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

  }


}
