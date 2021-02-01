package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import models.NoPagination
import utils.{CasePayloads, CaseQueueBuilder}

class RootSpec extends IntegrationTest with MockitoSugar with CaseQueueBuilder {

  "Root" should {

    "return status 200 and redirect to Dashboard" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(
          urlEqualTo(
            buildQueryUrl(withStatuses = "SUSPENDED,COMPLETED,NEW,OPEN,REFERRED", assigneeId = "123", pag = NoPagination())
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.pagedGatewayCases)
        )
      )

      stubFor(
        get(
          urlEqualTo(
            "/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id%2Capplication-type&report_field=active-days-elapsed"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.report)
        )
      )

      // When
      val response = await(ws.url(s"$baseUrl").get())

      // Then
      response.status shouldBe OK
      response.body   should include("My cases")
      response.body   should include("All open cases")
      response.body   should include("Gateway cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

    "redirect to error handler for unknown path" in {
      // When
      val response = await(ws.url(s"$baseUrl/rubbish").get())

      // Then
      response.status shouldBe NOT_FOUND
      response.body   should include("Please check that you have entered the correct web address.")
      response.body   should include("This page can’t be found")
    }

  }

}
