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
      stubFor(get(urlMatching("/cases?.*trader_name=1.*"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?trader_name=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search_results")
    }

    "Sort by default" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlMatching("/cases?.*sort_direction=desc&sort_by=commodityCode.*"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?trader_name=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search_results")
    }

    "Sort by 'Commodity Code'" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlMatching("/cases?.*sort_direction=desc&sort_by=commodityCode.*"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?sort_by=commodityCode&sort_direction=desc&trader_name=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("advanced_search_results")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("search")
    }
  }

}
