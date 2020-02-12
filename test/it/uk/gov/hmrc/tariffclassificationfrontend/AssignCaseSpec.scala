package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases, EventPayloads}


class AssignCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Assign" should {
    val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample.copy(queueId = Some("1"), assignee = None))
    val event = EventPayloads.event

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatusOPEN))
      )
      stubFor(post(urlEqualTo("/cases/1/events"))
        .willReturn(aResponse()
          .withStatus(CREATED)
          .withBody(event))
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/assign").get())

      // Then
      response.status shouldBe OK
      response.body should include("assign_case-heading")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/assign").get())

      // Then
      response.status shouldBe OK
      response.body should include(messages("not_authorised.paragraph1"))
    }
  }

}
