package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import models.SampleStatus
import utils.CasePayloads

class SampleStatusSpec extends IntegrationTest with MockitoSugar {

  "Sample Status'" should {

    "Return all options for BTI Case" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.simpleBtiCase)
          )
      )

      // When
      val response: WSResponse = await(requestWithSession("/cases/1/sample/status").get())

      // Then
      response.status shouldBe OK
      SampleStatus.values.foreach(s => response.body should include(s">${SampleStatus.format(Some(s))}<"))
    }

    "Return limited options for Liability Case" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.simpleLiabilityCase)
          )
      )

      // When
      val response: WSResponse = await(requestWithSession("/cases/1/sample/status?options=liability").get())

      // Then
      response.status shouldBe OK
      response.body   should include(">Yes<")
      response.body   should include(">No<")
      response.body   should include("AWAITING")

      response.body shouldNot include(s">${SampleStatus.format(Some(SampleStatus.RETURNED_APPLICANT))}<")
    }
  }

}
