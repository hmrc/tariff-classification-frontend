package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.NoPagination
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, CaseQueueBuilder}


class RootSpec extends IntegrationTest with MockitoSugar with CaseQueueBuilder {

  "Root" should {

    "return status 200 and redirect to My Cases" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(buildQueryUrl(withStatuses = "NEW,OPEN,REFERRED,SUSPENDED", assigneeId = "123", pag = NoPagination())))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      stubFor(get(urlEqualTo("/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id%2Capplication-type&report_field=active-days-elapsed"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.report))
      )

      // When
      val response = await(ws.url(s"$baseUrl").get())

      // Then
      response.status shouldBe OK
      response.body should include("Cases for Forename Surname")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl").get())

      // Then
      response.status shouldBe OK
      response.body should include(messages("not_authorised.paragraph1"))
    }

    "redirect to error handler for unknown path" in {
      // When
      val response = await(ws.url(s"$baseUrl/rubbish").get())

      // Then
      response.status shouldBe NOT_FOUND
      response.body should include("Please check that you have entered the correct web address.")
      response.body should include("This page can’t be found")
    }

  }

}
