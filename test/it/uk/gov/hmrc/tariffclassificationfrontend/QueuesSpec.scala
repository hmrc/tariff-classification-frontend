package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.NoPagination
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, CaseQueueBuilder}

class QueuesSpec extends IntegrationTest with MockitoSugar with CaseQueueBuilder {

  "My Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(buildQueryUrl(withStatuses = "NEW,OPEN,REFERRED,SUSPENDED", assigneeId = "123", pag = NoPagination())))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      stubFor(get(urlEqualTo("/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id&report_field=active-days-elapsed"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.report))
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


      stubFor(get(urlEqualTo(buildQueryUrl(withStatuses = "NEW,OPEN,REFERRED,SUSPENDED", queueId = "none", assigneeId = "none", pag = NoPagination())))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
        )

      stubFor(get(urlEqualTo("/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id&report_field=active-days-elapsed"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.report))
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
      stubFor(get(urlEqualTo(buildQueryUrl(withStatuses = "NEW,OPEN,REFERRED,SUSPENDED", queueId = "2", assigneeId = "none", pag = NoPagination())))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      stubFor(get(urlEqualTo("/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id&report_field=active-days-elapsed"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.report))
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
      stubFor(get(urlEqualTo(buildQueryUrl(withStatuses = "NEW,OPEN,REFERRED,SUSPENDED", queueId = "3", assigneeId = "none", pag = NoPagination())))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      stubFor(get(urlEqualTo("/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id&report_field=active-days-elapsed"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.report))
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
      stubFor(get(urlEqualTo(buildQueryUrl(withStatuses = "NEW,OPEN,REFERRED,SUSPENDED", queueId = "4", assigneeId = "none", pag = NoPagination())))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      stubFor(get(urlEqualTo("/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id&report_field=active-days-elapsed"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.report))
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
      stubFor(get(urlEqualTo(buildQueryUrl(withStatuses = "NEW,OPEN,REFERRED,SUSPENDED", queueId = "5", assigneeId = "none", pag = NoPagination())))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      stubFor(get(urlEqualTo("/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id&report_field=active-days-elapsed"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.report))
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
