package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.OK
import uk.gov.hmrc.tariffclassificationfrontend.models.Pagination
import uk.gov.tariffclassificationfrontend.utils.CasePayloads

class AssignedCasesSpec extends IntegrationTest with MockitoSugar {

  private val testCasesServiceUrl = "/cases?application_type=BTI&assignee_id=some&status=OPEN,REFERRED,SUSPENDED" +
    s"&sort_by=days-elapsed&sort_direction=desc&page=1&page_size=${Pagination.unlimited}"

  "Assigned Cases" should {

    "return status 200 for get all assigned cases" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(testCasesServiceUrl))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedEmpty))
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/queues/assigned").get())

      // Then
      response.status shouldBe OK
      response.body should include("<p id=\"assignees_list-empty\">There are no cases assigned.</p>")
    }

    "return status 200 for get assigned cases for operator" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo(testCasesServiceUrl))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.pagedAssignedCases))
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/queues/assigned?operatorId=1").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h2 class=\"heading-small\">Assigned to Test User</h2>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/queues/assigned").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }


}
