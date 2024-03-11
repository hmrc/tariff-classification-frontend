/*
 * Copyright 2024 HM Revenue & Customs
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

package connector

import cats.data.NonEmptySeq
import com.github.tomakehurst.wiremock.client.WireMock._
import models._
import models.reporting._
import org.apache.http.HttpStatus
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils._

class BindingTariffClassificationConnectorSpec extends ConnectorTest with CaseQueueBuilder {

  import utils.JsonFormatters._

  private val pagination = SearchPagination(1, 2)

  private val connector = new BindingTariffClassificationConnector(mockAppConfig, authenticatedHttpClient, metrics)

  "Connector 'Get Cases By Queue'" should {

    "get empty cases in 'gateway' queue" in {
      val url = buildQueryUrl(
        types        = ApplicationType.values.toSeq,
        withStatuses = "SUSPENDED,COMPLETED,NEW,OPEN,REFERRED",
        queueId      = "none",
        assigneeId   = "none",
        pag          = TestPagination()
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedEmpty)
          )
      )

      await(connector.findCasesByQueue(Queues.gateway, pagination)) shouldBe Paged.empty[Case]

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "get cases in 'gateway' queue" in {
      val url = buildQueryUrl(
        types        = ApplicationType.values.toSeq,
        withStatuses = "SUSPENDED,COMPLETED,NEW,OPEN,REFERRED",
        queueId      = "none",
        assigneeId   = "none",
        pag          = TestPagination()
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      await(connector.findCasesByQueue(Queues.gateway, pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "get empty cases in 'act' queue" in {
      val url = buildQueryUrl(
        types        = ApplicationType.values.toSeq,
        withStatuses = "SUSPENDED,COMPLETED,NEW,OPEN,REFERRED",
        queueId      = "2",
        assigneeId   = "none",
        pag          = TestPagination()
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedEmpty)
          )
      )

      await(connector.findCasesByQueue(Queues.act, pagination)) shouldBe Paged.empty[Case]

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "get cases in 'act' queue" in {
      val url = buildQueryUrl(
        types        = ApplicationType.values.toSeq,
        withStatuses = "SUSPENDED,COMPLETED,NEW,OPEN,REFERRED",
        queueId      = "2",
        assigneeId   = "none",
        pag          = TestPagination()
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      await(connector.findCasesByQueue(Queues.act, pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "get cases for liability version of 'act' queue" in {
      val url = buildQueryUrl(
        types        = Seq(ApplicationType.LIABILITY),
        withStatuses = "SUSPENDED,COMPLETED,NEW,OPEN,REFERRED",
        queueId      = "2",
        assigneeId   = "none",
        pag          = TestPagination()
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      await(connector.findCasesByQueue(Queues.act, pagination, Set(ApplicationType.LIABILITY))) shouldBe Paged(
        Seq(Cases.btiCaseExample)
      )

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Get Cases By All Queue'" should {

    "get cases in all queues" in {
      val url = buildQueryUrlAllQueues(
        types      = ApplicationType.values.toSeq,
        statuses   = "OPEN,REFERRED,SUSPENDED",
        assigneeId = "none",
        queueIds   = Queues.allQueues.map(_.id),
        pagination = pagination
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      await(connector.findCasesByAllQueues(Queues.allQueues, pagination, assignee = "none")) shouldBe Paged(
        Seq(Cases.btiCaseExample)
      )

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Get One'" should {

    "get an unknown case" in {
      stubFor(
        get(urlEqualTo("/cases/id"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_NOT_FOUND)
          )
      )

      await(connector.findCase("id")) shouldBe None

      verify(
        getRequestedFor(urlEqualTo("/cases/id"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "get a case" in {
      stubFor(
        get(urlEqualTo("/cases/id"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.btiCase)
          )
      )

      await(connector.findCase("id")) shouldBe Some(Cases.btiCaseExample)

      verify(
        getRequestedFor(urlEqualTo("/cases/id"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

  }

  "Connector 'Get Cases By Assignee'" should {

    "get empty cases" in {
      val url =
        buildQueryUrl(
          withStatuses = "SUSPENDED,COMPLETED,NEW,OPEN,REFERRED",
          assigneeId   = "assignee",
          pag          = TestPagination()
        )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedEmpty)
          )
      )

      await(connector.findCasesByAssignee(Operator("assignee"), pagination)) shouldBe Paged.empty[Case]

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "get cases" in {
      val url =
        buildQueryUrl(
          withStatuses = "SUSPENDED,COMPLETED,NEW,OPEN,REFERRED",
          assigneeId   = "assignee",
          pag          = TestPagination()
        )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      await(connector.findCasesByAssignee(Operator("assignee"), pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Search'" should {

    "handle no filters" in {
      val url = "/cases?sort_direction=asc&sort_by=commodity-code&page=1&page_size=2"

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedEmpty)
          )
      )

      await(connector.search(Search(), Sort(), pagination)) shouldBe Paged.empty[Case]

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "filter by all" in {
      val url = s"/cases" +
        s"?sort_direction=asc" +
        s"&sort_by=commodity-code" +
        s"&case_source=trader" +
        s"&commodity_code=comm-code" +
        s"&decision_details=decision-details" +
        s"&status=OPEN" +
        s"&status=LIVE" +
        s"&application_type=BTI" +
        s"&application_type=LIABILITY_ORDER" +
        s"&application_type=CORRESPONDENCE" +
        s"&application_type=MISCELLANEOUS" +
        s"&keyword=K1" +
        s"&keyword=K2" +
        s"&page=1" +
        s"&page_size=2"

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      val search = Search(
        caseSource      = Some("trader"),
        commodityCode   = Some("comm-code"),
        decisionDetails = Some("decision-details"),
        status          = Some(Set(PseudoCaseStatus.OPEN, PseudoCaseStatus.LIVE)),
        applicationType = Some(
          Set(
            ApplicationType.ATAR,
            ApplicationType.LIABILITY,
            ApplicationType.CORRESPONDENCE,
            ApplicationType.MISCELLANEOUS
          )
        ),
        keywords = Some(Set("K1", "K2"))
      )

      await(
        connector
          .search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)
      ) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "filter by 'trader name'" in {
      val url = "/cases?sort_direction=asc&sort_by=commodity-code&case_source=trader&page=1&page_size=2"

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      val search = Search(caseSource = Some("trader"))

      await(
        connector
          .search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)
      ) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "filter by 'commodity code'" in {
      val url = "/cases?sort_direction=asc&sort_by=commodity-code&commodity_code=comm-code&page=1&page_size=2"

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      val search = Search(
        commodityCode = Some("comm-code")
      )

      await(
        connector
          .search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)
      ) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "filter by 'decision_details'" in {
      val url = s"/cases?sort_direction=asc&sort_by=commodity-code&decision_details=decision-details&page=1&page_size=2"

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      val search = Search(
        decisionDetails = Some("decision-details")
      )

      await(
        connector
          .search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)
      ) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "filter by 'keyword'" in {
      val url = "/cases?sort_direction=asc&sort_by=commodity-code&keyword=K1&keyword=K2&page=1&page_size=2"

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      val search = Search(
        keywords = Some(Set("K1", "K2"))
      )

      await(
        connector
          .search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)
      ) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "get cases by 'status'" in {
      val url = s"/cases?sort_direction=asc&sort_by=commodity-code&status=OPEN&status=LIVE&page=1&page_size=2"

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      val search = Search(
        status = Some(Set(PseudoCaseStatus.OPEN, PseudoCaseStatus.LIVE))
      )

      await(
        connector
          .search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)
      ) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "get cases by 'application type'" in {
      val url =
        s"/cases?sort_direction=asc&sort_by=commodity-code&application_type=BTI&" +
          s"application_type=LIABILITY_ORDER&application_type=CORRESPONDENCE&application_type=MISCELLANEOUS&page=1&page_size=2"

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      val search = Search(
        applicationType = Some(
          ApplicationType.values
        )
      )

      await(
        connector
          .search(search, Sort(direction = SortDirection.ASCENDING, field = SortField.COMMODITY_CODE), pagination)
      ) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Update Case'" should {

    "update valid case" in {
      val ref       = "case-reference"
      val validCase = Cases.btiCaseExample.copy(reference = ref)
      val json      = Json.toJson(validCase).toString()

      stubFor(
        put(urlEqualTo(s"/cases/$ref"))
          .withRequestBody(equalToJson(json))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(json)
          )
      )

      await(connector.updateCase(validCase)) shouldBe validCase

      verify(
        putRequestedFor(urlEqualTo(s"/cases/$ref"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "update with an unknown case reference" in {
      val unknownRef  = "unknownRef"
      val unknownCase = Cases.btiCaseExample.copy(reference = unknownRef)
      val json        = Json.toJson(unknownCase).toString()

      stubFor(
        put(urlEqualTo(s"/cases/$unknownRef"))
          .withRequestBody(equalToJson(json))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_NOT_FOUND)
          )
      )

      intercept[UpstreamErrorResponse] {
        await(connector.updateCase(unknownCase))
      }

      verify(
        putRequestedFor(urlEqualTo(s"/cases/$unknownRef"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Create Case'" should {

    "create valid case" in {
      val application = Cases.btiApplicationExample
      val validCase   = Cases.btiCaseExample
      val request     = Json.toJson(NewCaseRequest(application)).toString()
      val response    = Json.toJson(validCase).toString()

      stubFor(
        post(urlEqualTo(s"/cases"))
          .withRequestBody(equalToJson(request))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_CREATED)
              .withBody(response)
          )
      )

      await(connector.createCase(application)) shouldBe validCase

      verify(
        postRequestedFor(urlEqualTo(s"/cases"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Create Event'" should {

    "create event" in {
      val ref               = "case-reference"
      val validCase         = Cases.btiCaseExample.copy(reference = ref)
      val validEventRequest = Events.eventRequest
      val validEvent        = Events.event.copy(caseReference = ref)
      val requestJson       = Json.toJson(validEventRequest).toString()
      val responseJson      = Json.toJson(validEvent).toString()

      stubFor(
        post(urlEqualTo(s"/cases/$ref/events"))
          .withRequestBody(equalToJson(requestJson))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(responseJson)
          )
      )

      await(connector.createEvent(validCase, validEventRequest)) shouldBe validEvent

      verify(
        postRequestedFor(urlEqualTo(s"/cases/$ref/events"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "create event with an unknown case reference" in {
      val ref               = "unknown-reference"
      val validCase         = Cases.btiCaseExample.copy(reference = ref)
      val validEventRequest = Events.eventRequest
      val requestJson       = Json.toJson(validEventRequest).toString()

      stubFor(
        post(urlEqualTo(s"/cases/$ref/events"))
          .withRequestBody(equalToJson(requestJson))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_NOT_FOUND)
          )
      )

      intercept[UpstreamErrorResponse] {
        await(connector.createEvent(validCase, validEventRequest))
      }

      verify(
        postRequestedFor(urlEqualTo(s"/cases/$ref/events"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

  }

  "Connector find events" should {
    val ref = "id"

    "return a list of Events for this case" in {

      stubFor(
        get(urlEqualTo(s"/events?case_reference=$ref&page=1&page_size=2"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(EventPayloads.pagedEvents)
          )
      )

      await(connector.findEvents(ref, pagination)) shouldBe Paged(Events.events)

      verify(
        getRequestedFor(urlEqualTo(s"/events?case_reference=$ref&page=1&page_size=2"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "return a filtered list of Events for this case" in {

      stubFor(
        get(urlEqualTo(s"/events?case_reference=$ref&type=SAMPLE_STATUS_CHANGE&page=1&page_size=2"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(EventPayloads.pagedEvents)
          )
      )

      await(connector.findFilteredEvents(ref, pagination, Set(EventType.SAMPLE_STATUS_CHANGE))) shouldBe Paged(
        Events.events
      )

      verify(
        getRequestedFor(urlEqualTo(s"/events?case_reference=$ref&type=SAMPLE_STATUS_CHANGE&page=1&page_size=2"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "returns empty list when case ref not found" in {
      stubFor(
        get(urlEqualTo(s"/events?case_reference=$ref&page=1&page_size=2"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(EventPayloads.pagedEmpty)
          )
      )

      await(connector.findEvents(ref, pagination)) shouldBe Paged.empty[Event]

      verify(
        getRequestedFor(urlEqualTo(s"/events?case_reference=$ref&page=1&page_size=2"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Find Completion Events'" should {
    val ref = "id"

    "return a list of events for the given case references" in {
      stubFor(
        get(urlEqualTo(s"/events?case_reference=$ref&type=CASE_COMPLETED&page=1&page_size=2147483647"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(EventPayloads.completionEvents)
          )
      )

      await(connector.findCompletionEvents(Set(ref))) shouldBe Events.completionEventsById

      verify(
        getRequestedFor(urlEqualTo(s"/events?case_reference=$ref&type=CASE_COMPLETED&page=1&page_size=2147483647"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "return empty list when case ref not found" in {
      stubFor(
        get(urlEqualTo(s"/events?case_reference=$ref&type=CASE_COMPLETED&page=1&page_size=2147483647"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(EventPayloads.pagedEmpty)
          )
      )

      await(connector.findCompletionEvents(Set(ref))) shouldBe Map.empty[String, Event]

      verify(
        getRequestedFor(urlEqualTo(s"/events?case_reference=$ref&type=CASE_COMPLETED&page=1&page_size=2147483647"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Find Referral Events'" should {
    val ref = "id"

    "return a list of events for the given case references" in {
      stubFor(
        get(urlEqualTo(s"/events?case_reference=$ref&type=CASE_REFERRAL&page=1&page_size=2147483647"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(EventPayloads.referralEvents)
          )
      )

      await(connector.findReferralEvents(Set(ref))) shouldBe Events.referralEventsById

      verify(
        getRequestedFor(urlEqualTo(s"/events?case_reference=$ref&type=CASE_REFERRAL&page=1&page_size=2147483647"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "return empty list when case ref not found" in {
      stubFor(
        get(urlEqualTo(s"/events?case_reference=$ref&type=CASE_REFERRAL&page=1&page_size=2147483647"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(EventPayloads.pagedEmpty)
          )
      )

      await(connector.findReferralEvents(Set(ref))) shouldBe Map.empty[String, Event]

      verify(
        getRequestedFor(urlEqualTo(s"/events?case_reference=$ref&type=CASE_REFERRAL&page=1&page_size=2147483647"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Get Assigned Cases'" should {

    "get assigned cases " in {
      val url = buildQueryUrl(withStatuses = "OPEN,REFERRED,SUSPENDED", assigneeId = "some", pag = TestPagination())

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      await(connector.findAssignedCases(pagination)) shouldBe Paged(Seq(Cases.btiCaseExample))

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

  }

  "Connector 'getAllUsers'" should {

    "get all users" in {
      val ref           = "PID1"
      val validOperator = Cases.operatorWithPermissions.copy(id = ref)
      val json          = Json.toJson(validOperator).toString()

      stubFor(
        put(urlEqualTo(s"/users/$ref"))
          .withRequestBody(equalToJson(json))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(json)
          )
      )

      await(connector.updateUser(validOperator)) shouldBe validOperator

      verify(
        putRequestedFor(urlEqualTo(s"/users/$ref"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "get cases in all queues" in {
      val url = buildQueryUrlAllQueues(
        types      = ApplicationType.values.toSeq,
        statuses   = "OPEN,REFERRED,SUSPENDED",
        assigneeId = "none",
        queueIds   = Queues.allQueues.map(_.id),
        pagination = pagination
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      await(connector.findCasesByAllQueues(Queues.allQueues, pagination, assignee = "none")) shouldBe Paged(
        Seq(Cases.btiCaseExample)
      )

      verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

  }

  "Connector 'Update User'" should {

    "update valid user" in {
      val ref           = "PID1"
      val validOperator = Cases.operatorWithPermissions.copy(id = ref)
      val json          = Json.toJson(validOperator).toString()

      stubFor(
        put(urlEqualTo(s"/users/$ref"))
          .withRequestBody(equalToJson(json))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(json)
          )
      )

      await(connector.updateUser(validOperator)) shouldBe validOperator

      verify(
        putRequestedFor(urlEqualTo(s"/users/$ref"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "update user with an unknown id" in {
      val unknownId       = "unknownId"
      val unknownOperator = Cases.operatorWithPermissions.copy(id = unknownId)
      val json            = Json.toJson(unknownOperator).toString()

      stubFor(
        put(urlEqualTo(s"/users/$unknownId"))
          .withRequestBody(equalToJson(json))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_NOT_FOUND)
          )
      )

      intercept[UpstreamErrorResponse] {
        await(connector.updateUser(unknownOperator))
      }

      verify(
        putRequestedFor(urlEqualTo(s"/users/$unknownId"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "create new user" in {
    val operator = Operator("1")
    val request  = Json.toJson(NewUserRequest(operator)).toString()
    val response = Json.toJson(operator).toString()

    stubFor(
      post(urlEqualTo(s"/users"))
        .withRequestBody(equalToJson(request))
        .willReturn(
          aResponse()
            .withStatus(HttpStatus.SC_CREATED)
            .withBody(response)
        )
    )

    await(connector.createUser(operator)) shouldBe operator

    verify(
      postRequestedFor(urlEqualTo(s"/users"))
        .withHeader("X-Api-Token", equalTo(fakeAuthToken))
    )
  }

  "update user" in {
    val operator = Operator("1")
    val request  = Json.toJson(NewUserRequest(operator)).toString()
    val response = Json.toJson(operator).toString()

    stubFor(
      post(urlEqualTo(s"/users/user:1"))
        .withRequestBody(equalToJson(request))
        .willReturn(
          aResponse()
            .withStatus(HttpStatus.SC_CREATED)
            .withBody(response)
        )
    )

    await(connector.createUser(operator)) shouldBe operator

    verify(
      postRequestedFor(urlEqualTo(s"/users"))
        .withHeader("X-Api-Token", equalTo(fakeAuthToken))
    )
  }

  "Connector 'delete User'" should {

    "delete valid user" in {
      val ref           = "PID1"
      val validOperator = Cases.operatorWithPermissions.copy(id = ref)
      val json          = Json.toJson(validOperator).toString()

      stubFor(
        put(urlEqualTo(s"/mark-deleted/users/$ref"))
          .withRequestBody(equalToJson(json))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(json)
          )
      )

      await(connector.markDeleted(validOperator)) shouldBe validOperator

      verify(
        putRequestedFor(urlEqualTo(s"/mark-deleted/users/$ref"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "delete user with an unknown id" in {
      val unknownId       = "unknownId"
      val unknownOperator = Cases.operatorWithPermissions.copy(id = unknownId)
      val json            = Json.toJson(unknownOperator).toString()

      stubFor(
        put(urlEqualTo(s"/mark-deleted/users/$unknownId"))
          .withRequestBody(equalToJson(json))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_NOT_FOUND)
          )
      )

      intercept[UpstreamErrorResponse] {
        await(connector.markDeleted(unknownOperator))
      }

      verify(
        putRequestedFor(urlEqualTo(s"/mark-deleted/users/$unknownId"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Summary Report'" should {
    "fetch summary report" in {
      val expectedResults = Paged(
        Seq(
          SimpleResultGroup(
            count     = 1,
            groupKey  = NonEmptySeq.one(StringResultField(ReportField.Chapter.fieldName, Some("85"))),
            maxFields = List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(4)))
          ),
          SimpleResultGroup(
            count     = 2,
            groupKey  = NonEmptySeq.one(StringResultField(ReportField.Chapter.fieldName, None)),
            maxFields = List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(7)))
          ),
          SimpleResultGroup(
            count     = 3,
            groupKey  = NonEmptySeq.one(StringResultField(ReportField.Chapter.fieldName, Some("95"))),
            maxFields = List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(4)))
          )
        )
      )
      val resultsJson = Json.toJson(expectedResults)

      stubFor(
        get(urlPathEqualTo("/report/summary"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(resultsJson.toString())
          )
      )

      val actualResults = await(
        connector.summaryReport(
          SummaryReport(
            "Cases by commodity code chapter",
            groupBy = NonEmptySeq.one(ReportField.Chapter),
            sortBy  = ReportField.Count
          ),
          SearchPagination()
        )
      )

      actualResults shouldBe expectedResults

      verify(
        getRequestedFor(urlPathEqualTo("/report/summary"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Case Report'" should {
    "fetch cases report" in {
      val expectedResults: Paged[Map[String, ReportResultField[_]]] = Paged(
        Seq(
          Map(
            ReportField.Reference.fieldName  -> StringResultField(ReportField.Reference.fieldName, Some("1")),
            ReportField.GoodsName.fieldName  -> StringResultField(ReportField.GoodsName.fieldName, Some("Fireworks")),
            ReportField.TraderName.fieldName -> StringResultField(ReportField.TraderName.fieldName, Some("Gandalf"))
          )
        )
      )
      val resultsJson = Json.toJson(expectedResults)

      stubFor(
        get(urlPathEqualTo("/report/cases"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(resultsJson.toString())
          )
      )

      val actualResults = await(
        connector.caseReport(
          CaseReport(
            "ATaR Summary Report",
            fields = NonEmptySeq.of(ReportField.Reference, ReportField.GoodsName, ReportField.TraderName)
          ),
          SearchPagination()
        )
      )

      actualResults shouldBe expectedResults

      verify(
        getRequestedFor(urlPathEqualTo("/report/cases"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'Queue Report'" should {
    "fetch queue report" in {
      val expectedResults = Paged(
        Seq(
          QueueResultGroup(4, None, ApplicationType.ATAR),
          QueueResultGroup(2, None, ApplicationType.LIABILITY),
          QueueResultGroup(7, Some("2"), ApplicationType.ATAR),
          QueueResultGroup(6, Some("3"), ApplicationType.LIABILITY)
        )
      )
      val resultsJson = Json.toJson(expectedResults)

      stubFor(
        get(urlPathEqualTo("/report/queues"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(resultsJson.toString())
          )
      )

      val actualResults = await(connector.queueReport(QueueReport(), SearchPagination()))

      actualResults shouldBe expectedResults

      verify(
        getRequestedFor(urlPathEqualTo("/report/queues"))
      )
    }
  }

  "Connector 'findAllKeywords'" should {

    "return all keywords" in {
      val keyword  = Keyword("AKeyword", approved = true)
      val response = Json.toJson(Paged(Seq(keyword))).toString()

      val url = s"/keywords?page=${pagination.page}&page_size=${pagination.pageSize}"

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(response)
          )
      )

      await(connector.findAllKeywords(pagination)) shouldBe Paged(Seq(keyword))

      verify(
        getRequestedFor(urlEqualTo(url))
      )
    }
  }

  "Connector 'create Keyword'" should {

    "create new keyword" in {
      val keyword  = Keyword("AKeyword".toUpperCase, approved = true)
      val request  = Json.toJson(NewKeywordRequest(keyword)).toString()
      val response = Json.toJson(keyword).toString()

      stubFor(
        post(urlEqualTo(s"/keyword"))
          .withRequestBody(equalToJson(request))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_CREATED)
              .withBody(response)
          )
      )

      await(connector.createKeyword(keyword)) shouldBe keyword

      verify(
        postRequestedFor(urlEqualTo(s"/keyword"))
      )
    }
  }

  "Connector 'getCaseKeywords'" should {

    "return case keywords" in {
      val keyword     = Keyword("AKeyword", approved = true)
      val caseHeader  = CaseHeader("ref", None, None, None, ApplicationType.ATAR, CaseStatus.REFERRED, 0, None)
      val caseKeyword = CaseKeyword(keyword, List(caseHeader))

      val response = Json.toJson(Paged(Seq(caseKeyword))).toString()

      stubFor(
        get(urlEqualTo("/case-keywords"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(response)
          )
      )

      await(connector.getCaseKeywords()) shouldBe Paged(Seq(caseKeyword))

      verify(
        getRequestedFor(urlEqualTo("/case-keywords"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

  "Connector 'delete Keyword'" should {

    "delete the keyword given" in {
      val keyword = Keyword("AKeyword", approved = true)
      stubFor(
        delete(s"/keyword/${keyword.name}")
          .willReturn(
            aResponse()
              .withStatus(Status.NO_CONTENT)
          )
      )

      await(connector.deleteKeyword(keyword))

      verify(
        deleteRequestedFor(urlEqualTo(s"/keyword/${keyword.name}"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }
}
