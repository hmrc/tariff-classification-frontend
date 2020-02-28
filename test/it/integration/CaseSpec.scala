package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import models.{Case, Pagination}
import utils.Cases._
import utils.{CasePayloads, EventPayloads}

class CaseSpec extends IntegrationTest with MockitoSugar {

  private val c: Case = aCase(withReference("1"), withoutAgent())

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
      response.status shouldBe NOT_FOUND
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
          .withBody(CasePayloads.jsonOf(c)))
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
          .withBody(CasePayloads.jsonOf(c)))
      )
      stubFor(post(urlEqualTo("/file?id="))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("[]")
        )
      )

      // When
      val response = await(ws.url(s"$baseUrl/cases/1/item").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"application-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1/item")
    }
  }

  "Case Ruling Details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.jsonOf(c)))
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
          .withBody(CasePayloads.jsonOf(c)))
      )
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/events?case_reference=1&type=QUEUE_CHANGE&type=APPEAL_ADDED&type=APPEAL_STATUS_CHANGE&type=EXTENDED_USE_STATUS_CHANGE&type=CASE_STATUS_CHANGE&type=CASE_REFERRAL&type=NOTE&type=CASE_COMPLETED&type=CASE_CANCELLATION&type=ASSIGNMENT_CHANGE&page=1&page_size=${Pagination.unlimited}"))
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
          .withBody(CasePayloads.jsonOf(c)))
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
