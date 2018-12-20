package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.tariffclassificationfrontend.utils.CasePayloads


class RootSpec extends IntegrationTest with MockitoSugar {

  "Root" should {

    "return status 200 and redirect to My Cases" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases?assignee_id=123&status=NEW,OPEN,REFERRED,SUSPENDED&sort_by=days-elapsed&sort_direction=descending"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"$appRoot").get())

      // Then
      response.status shouldBe OK
      response.body should include("Cases for Forename Surname")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$appRoot").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
