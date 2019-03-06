package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases, EventPayloads}


class CancelRulingSpec extends IntegrationTest with MockitoSugar {

  "Cancel Ruling" should {
    val caseWithStatusCOMPLETE = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED))
    val event = EventPayloads.event

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatusCOMPLETE))
      )
      stubFor(post(urlEqualTo("/cases/1/events"))
        .willReturn(aResponse()
          .withStatus(CREATED)
          .withBody(event))
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/ruling/cancel").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-large mt-0\">Cancel the ruling</h3>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/ruling/cancel").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
