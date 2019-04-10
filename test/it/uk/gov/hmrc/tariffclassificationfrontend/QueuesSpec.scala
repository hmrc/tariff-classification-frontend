package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.Pagination
import uk.gov.tariffclassificationfrontend.utils.CasePayloads

class QueuesSpec extends IntegrationTest with MockitoSugar {

  "My Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/cases?application_type=BTI&assignee_id=123&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=${Pagination.unlimited}"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues").get())

      // Then
      response.status shouldBe OK
      response.body should include("Cases for Forename Surname")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "Gateway Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/cases?application_type=BTI&queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=${Pagination.unlimited}"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
        )

      // When
      val response = await(ws.url(s"$baseUrl/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body should include("Gateway cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "ACT Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/cases?application_type=BTI&queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=${Pagination.unlimited}"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues/act").get())

      // Then
      response.status shouldBe OK
      response.body should include("ACT cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "CAP Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/cases?application_type=BTI&queue_id=3&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=${Pagination.unlimited}"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues/cap").get())

      // Then
      response.status shouldBe OK
      response.body should include("CAP cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "Cars Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/cases?application_type=BTI&queue_id=4&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=${Pagination.unlimited}"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues/cars").get())

      // Then
      response.status shouldBe OK
      response.body should include("Cars cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/cars").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "ELM Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/cases?application_type=BTI&queue_id=5&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=${Pagination.unlimited}"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues/elm").get())

      // Then
      response.status shouldBe OK
      response.body should include("ELM cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/elm").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
