package integration

import com.github.tomakehurst.wiremock.client.WireMock.{stubFor, _}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import models.Pagination
import utils.{CasePayloads, EventPayloads, KeywordsPayloads}

class SearchSpec extends IntegrationTest with MockitoSugar {

  "Search by 'Case Reference'" should {

    "Filter by 'Case Reference'" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.simpleBtiCase)
          )
      )
      stubFor(
        get(
          urlEqualTo(
            "/events?case_reference=1" +
              "&type=SAMPLE_STATUS_CHANGE&type=SAMPLE_RETURN_CHANGE" +
              s"&type=SAMPLE_SEND_CHANGE&page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(EventPayloads.pagedSampleEvents)
        )
      )
      stubFor(
        get(
          urlEqualTo(
            "/events?case_reference=1" +
              "&type=EXPERT_ADVICE_RECEIVED&type=CASE_REJECTED&type=QUEUE_CHANGE&type=APPEAL_ADDED" +
              "&type=APPEAL_STATUS_CHANGE&type=EXTENDED_USE_STATUS_CHANGE" +
              "&type=CASE_STATUS_CHANGE&type=CASE_REFERRAL&type=NOTE&type=CASE_COMPLETED" +
              "&type=CASE_CANCELLATION&type=CASE_CREATED&type=ASSIGNMENT_CHANGE" +
              s"&page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(EventPayloads.pagedEvents)
        )
      )
      stubFor(
        get(
          urlEqualTo(
            s"/keywords?page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(KeywordsPayloads.pagedKeywords)
        )
      )
      stubFor(
        post(urlEqualTo("/file/initiate"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(fromResource("filestore/binding-tariff-filestore_initiate-response.json"))
          )
      )

      // When
      val response1 = await(requestWithSession("/search?reference=1").withFollowRedirects(false).get())
      response1.header("Location") shouldBe (Some("/manage-tariff-classifications/cases/1"))
      response1.status             shouldBe SEE_OTHER

      val response2 = await(requestWithSession("/cases/1").withFollowRedirects(false).get())
      response2.header("Location") shouldBe (Some("/manage-tariff-classifications/cases/v2/1/atar"))
      response2.status             shouldBe SEE_OTHER

      val response3 = await(requestWithSession("/cases/v2/1/atar").withFollowRedirects(false).get())

      // Then
      response3.status shouldBe OK
      response3.body   should include("trader-heading")
    }
  }

  "Search by 'Trader Name'" should {

    "Do nothing when empty" in {
      // Given
      givenAuthSuccess()

      // When
      val response = await(requestWithSession("/search?case_source=").get())

      // Then
      response.status shouldBe OK
      response.body shouldNot include("id=\"advanced_search-results_and_filters\"")
    }

    "Filter by 'Trader Name'" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlMatching("/cases\\?.*case_source=1.*"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      // When
      val response = await(requestWithSession("/search?case_source=1").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"advanced_search-results_and_filters\"")
    }
  }

  "Search by 'Commodity Code'" should {

    "Do nothing when empty" in {
      // Given
      givenAuthSuccess()

      // When
      val response = await(requestWithSession("/search?commodity_code=").get())

      // Then
      response.status shouldBe OK
      response.body shouldNot include("id=\"advanced_search-results_and_filters\"")
    }

    "Filter by 'Commodity Code'" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlMatching("/cases\\?.*commodity_code=1.*"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      // When
      val response = await(requestWithSession("/search?commodity_code=11").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"advanced_search-results_and_filters\"")
    }
  }

  "Search by 'Good Description'" should {

    "Do nothing when empty" in {
      // Given
      givenAuthSuccess()

      // When
      val response = await(requestWithSession("/search?decision_details=").get())

      // Then
      response.status shouldBe OK
      response.body shouldNot include("id=\"advanced_search-results_and_filters\"")
    }

    "Filter by 'Good Description'" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlMatching("/cases\\?.*decision_details=1.*"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      // When
      val response = await(requestWithSession("/search?decision_details=1").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"advanced_search-results_and_filters\"")
    }
  }

  "Search by 'Keyword'" should {

    "Do nothing when empty" in {
      // Given
      givenAuthSuccess()

      // When
      val response = await(requestWithSession("/search?keyword[0]=").get())

      // Then
      response.status shouldBe OK
      response.body shouldNot include("id=\"advanced_search-results_and_filters\"")
    }

    "Filter by 'Keyword'" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlMatching("/cases\\?.*keyword=k1&keyword=k2.*"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      // When
      val response = await(requestWithSession("/search?keyword[0]=k1&keyword[1]=k2").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"advanced_search-results_and_filters\"")
    }
  }

  "Search by 'Live Rulings Only'" should {
    val dateRegex                         = "\\d{4}-\\d{2}-\\d{2}T\\d{2}%3A\\d{2}%3A\\d{2}(\\.\\d{3})\\\\?Z"
    def excluding(value: String*): String = s"(${value.map(v => s"(?!$v)").mkString}.)*"

    "Do nothing when empty" in {
      // Given
      givenAuthSuccess()

      // When
      val response = await(requestWithSession("/search?live_rulings_only=").get())

      // Then
      response.status shouldBe OK
      response.body shouldNot include("id=\"advanced_search-results_and_filters\"")
    }

    "Do nothing when 'Live Rulings Only' is the only parameter" in {
      // Given
      givenAuthSuccess()

      // When
      val response = await(requestWithSession("/search?live_rulings_only=true").get())

      // Then
      response.status shouldBe OK
      response.body shouldNot include("id=\"advanced_search-results_and_filters\"")
    }

    // Note the UI actually calls search WITHOUT the live_rulings_only flag when unchecked (see similar test below)
    "Filter Live Rulings Only when 'true'" in {
      // Given
      givenAuthSuccess()

      stubFor(
        get(urlMatching(s"/cases\\?.*min_decision_end=$dateRegex&status=COMPLETED"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      // When
      val response = await(requestWithSession("/search?case_source=1&live_rulings_only=true").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"advanced_search-results_and_filters\"")
    }

    // Note the UI actually calls search WITHOUT the live_rulings_only flag when unchecked
    "Filter Live Rulings Only when not present" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlMatching(s"/cases\\?${excluding("status=", "min_decision_end=")}"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      // When
      val response = await(requestWithSession("/search?case_source=1").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"advanced_search-results_and_filters\"")
    }

    "Allow All Cases when 'false'" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlMatching(s"/cases\\?${excluding("status=", "min_decision_end=")}"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      // When
      val response = await(requestWithSession("/search?case_source=1&live_rulings_only=false").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"advanced_search-results_and_filters\"")
    }
  }

  "Search" should {

    "Sort by default" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlMatching("/cases\\?.*sort_direction=asc&sort_by=commodity-code.*"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      // When
      val response = await(requestWithSession("/search?case_source=1").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"advanced_search-results_and_filters\"")
    }

    "Sort by 'Commodity Code'" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlMatching("/cases\\?.*sort_direction=desc&sort_by=commodity-code.*"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedGatewayCases)
          )
      )

      // When
      val response = await(requestWithSession("/search?sort_by=commodity-code&sort_direction=desc&case_source=1").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"advanced_search-results_and_filters\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("/search")
    }
  }

}
