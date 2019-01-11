package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.tariffclassificationfrontend.utils.CasePayloads

class QueuesSpec extends IntegrationTest with MockitoSugar {

  "My Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases?assignee_id=123&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/queues").get())

      // Then
      response.status shouldBe OK
      response.body should include("Cases for Forename Surname")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/queues").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "Gateway Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
        )

      // When
      val response = await(ws.url(s"$frontendRoot/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">Gateway Cases</h1>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "ACT Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/queues/act").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">ACT Cases</h1>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "CAP Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases?queue_id=3&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/queues/cap").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">CAP Cases</h1>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "Cars Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases?queue_id=4&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/queues/cars").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">Cars Cases</h1>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/queues/cars").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  "ELM Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases?queue_id=5&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/queues/elm").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">ELM Cases</h1>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot/queues/elm").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
