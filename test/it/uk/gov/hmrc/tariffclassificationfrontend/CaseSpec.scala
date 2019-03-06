package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.Pagination
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, EventPayloads}

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
      val response = await(ws.url(s"$baseUrl/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("Case not found")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1")
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
      val response = await(ws.url(s"$baseUrl/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"trader-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1")
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
      val response = await(ws.url(s"$baseUrl/cases/1/application").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"application-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1/application")
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
      val response = await(ws.url(s"$baseUrl/cases/1/ruling").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"ruling-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1/ruling")
    }
  }

  "Case activity details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/cases/1/events?page=1&page_size=${Pagination.unlimited}"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(EventPayloads.pagedEvents))
      )
      // When
      val response = await(ws.url(s"$baseUrl/cases/1/activity").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"activity-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1/activity")
    }
  }


  "Case attachments details" should {

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
      val response = await(ws.url(s"$baseUrl/cases/1/attachments").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"attachments-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1/attachments")
    }
  }

}
