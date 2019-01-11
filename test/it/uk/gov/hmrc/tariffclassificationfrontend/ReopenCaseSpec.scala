package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases, EventPayloads}


class ReopenCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Refer" should {
    val caseWithStatusSuspended = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.SUSPENDED))
    val caseWithStatusReferred = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.REFERRED))
    val event = EventPayloads.event

    def reopenSuccessfullyWhen(caseWithStatus: String) = {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatus))
      )
      stubFor(post(urlEqualTo("/cases/1/events"))
        .willReturn(aResponse()
          .withStatus(CREATED)
          .withBody(event))
      )

      // When
      val response: WSResponse = await(ws.url(s"$backendRoot/cases/1/reopen").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-large mt-0\">Reopen this case</h3>")
    }

    "return status 200 when status is suspended" in {
      reopenSuccessfullyWhen(caseWithStatusSuspended)
    }

    "return status 200 when status is referred" in {
      reopenSuccessfullyWhen(caseWithStatusReferred)
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"$backendRoot/cases/1/reopen").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
