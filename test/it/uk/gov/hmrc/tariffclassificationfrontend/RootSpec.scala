package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.Pagination
import uk.gov.tariffclassificationfrontend.utils.CasePayloads


class RootSpec extends IntegrationTest with MockitoSugar {

  "Root" should {

    "return status 200 and redirect to My Cases" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/cases?application_type=BTI&assignee_id=123&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=${Pagination.unlimited}"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedGatewayCases))
      )

      // When
      val response = await(ws.url(s"$frontendRoot").get())

      // Then
      response.status shouldBe OK
      response.body should include("Cases for Forename Surname")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$frontendRoot").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }

    "redirect to error handler for unknown path" in {
      // When
      val response = await(ws.url(s"$frontendRoot/rubbish").get())

      // Then
      response.status shouldBe NOT_FOUND
      response.body should include("Please check that you have entered the correct web address.")
      response.body should include("This page can’t be found")
    }

  }

}
