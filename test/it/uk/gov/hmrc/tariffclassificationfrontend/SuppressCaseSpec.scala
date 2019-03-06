package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases, EventPayloads}


class SuppressCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Suppress" should {
    val caseWithStatusNEW = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.NEW))
    val event = EventPayloads.event

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatusNEW))
      )
      stubFor(post(urlEqualTo("/cases/1/events"))
        .willReturn(aResponse()
          .withStatus(CREATED)
          .withBody(event))
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/suppress").get())

      // Then
      response.status shouldBe OK
      response.body should include("Suppress this case")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/suppress").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
