package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.tariffclassificationfrontend.utils.CasePayloads
import uk.gov.tariffclassificationfrontend.utils.Cases._


class ExtendedUseCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Extended Use Change" should {
    val c = aCase(withReference("1"), withStatus(CaseStatus.CANCELLED), withDecision(applicationForExtendedUse = true))
    val caseWithStatusCOMPLETED = CasePayloads.jsonOf(c)

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatusCOMPLETED))
      )

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/extended-use/status").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"change_extended_use_status-heading\"")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/extended-use/status").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
