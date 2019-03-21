package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{CREATED, OK}
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Operator}
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases, EventPayloads}

class ReAssignCaseSpec extends IntegrationTest with MockitoSugar {

  "Re-Assign Case" should {
    val operator = Operator("1", Some("arthur"))
    val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample
      .copy(
        status = CaseStatus.OPEN,
        assignee = Some(operator)
      )
    )
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
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/reassign-case?origin=/").get())

      // Then
      response.status shouldBe OK
      response.body should include("Move this case back to a queue")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/reassign-case?origin=/").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
