package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.tariffclassificationfrontend.utils.CasePayloads


class CaseSpec extends IntegrationTest with MockitoSugar {

  "Unknown Case" should {

    "return status 200 with Case Not Found" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(NOT_FOUND))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("Case not found")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "Case Summary" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-medium mt-0\">Summary</h3>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "Case Application Details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )
      stubFor(post(urlEqualTo("/file?id="))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("[]")
        )
      )

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1/application").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-medium mt-0\">Application details</h3>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1/application").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "Case Ruling Details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )
      stubFor(post(urlEqualTo("/file?id="))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("[]")
        )
      )

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1/ruling").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-medium mt-0\">Ruling</h3>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1/ruling").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
