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
          .withBody(CasePayloads.btiCase))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?reference=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("Summary")
    }

    "Filter by 'Trader Name'" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases?traderName=1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/search?traderName=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("Advanced Search")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("search")
    }
  }

}
