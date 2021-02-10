package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import models.NoPagination
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.OK
import utils.{CasePayloads, CaseQueueBuilder}

class AssignedCasesSpec extends IntegrationTest with MockitoSugar with CaseQueueBuilder {

  private val testCasesServiceUrl =
    buildQueryUrl(withStatuses = "OPEN,REFERRED,SUSPENDED", assigneeId = "some", pag = NoPagination())

  private val testReportServiceUrl =
    "/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id%2Capplication-type&report_field=active-days-elapsed"

  private val testMyCasesServiceUrl =
    buildQueryUrl(withStatuses = "SUSPENDED,COMPLETED,NEW,OPEN,REFERRED", assigneeId = "123", pag = NoPagination())

  "Assigned Cases" should {

    "return status 200 for get all assigned cases" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlEqualTo(testCasesServiceUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedEmpty)
          )
      )
      stubFor(
        get(urlEqualTo(testMyCasesServiceUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedEmpty)
          )
      )
      stubFor(
        get(urlEqualTo(testReportServiceUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.reportEmpty)
          )
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/queues/assigned").get())

      // Then
      response.status shouldBe OK
      response.body   should include("There are no cases assigned.")
    }

    "return status 200 for get assigned cases for operator" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlEqualTo(testCasesServiceUrl))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.pagedAssignedCases)
          )
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/queues/assigned/1/0").get())

      // Then
      response.status shouldBe OK
      response.body   should include("<h2 class=\"heading-small\">Assigned to Test User</h2>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/queues/assigned").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

}
