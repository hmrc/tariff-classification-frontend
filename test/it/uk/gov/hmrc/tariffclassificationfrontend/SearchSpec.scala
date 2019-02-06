package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.tariffclassificationfrontend.utils.CasePayloads

class SearchSpec extends IntegrationTest with MockitoSugar {

  "Search" should {

    "Filter by 'Case Reference'" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.simpleBtiCase))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?reference=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("trader-heading")
    }

    "Filter by 'Trader Name'" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlMatching("/cases\\?.*trader_name=1.*"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?trader_name=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search-results_and_filters")
    }

    "Filter by 'Commodity Code'" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlMatching("/cases\\?.*commodity_code=1.*"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?commodity_code=11").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search-results_and_filters")
    }

    "Filter by 'Include In Progress' = true" in {
      // Given
      givenAuthSuccess()
      val dateRegex = "\\d{4}-\\d{2}-\\d{2}T\\d{2}%3A\\d{2}%3A\\d{2}(\\.\\d{3})\\\\?Z"
      stubFor(get(urlMatching(s"/cases\\?.*min_decision_end=$dateRegex"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?include_in_progress=true").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search-results_and_filters")
    }

    "Filter by 'Include In Progress' = false" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlMatching(s"/cases\\?.*status=COMPLETED"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?include_in_progress=false").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search-results_and_filters")
    }

    "Filter by 'Include In Progress' blank using default" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlMatching(s"/cases\\?.*status=COMPLETED"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?include_in_progress=").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search-results_and_filters")
    }

    "Not default 'Include In Progress' when not present" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlMatching(s"/cases\\?((?!status=COMPLETED).)*"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?trader_name=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search-results_and_filters")
    }

    "Sort by default" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlMatching("/cases\\?.*sort_direction=desc&sort_by=commodity-code.*"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?trader_name=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search-results_and_filters")
    }

    "Sort by 'Commodity Code'" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlMatching("/cases\\?.*sort_direction=desc&sort_by=commodity-code.*"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?sort_by=commodity-code&sort_direction=desc&trader_name=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search-results_and_filters")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("search")
    }
  }

}
