package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{CREATED, OK}
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Operator, Role}
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases, EventPayloads}

class ReAssignCaseSpec extends IntegrationTest with MockitoSugar {

  "Re-Assign Case" should {
    val owner = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
    val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample
      .copy(
        status = CaseStatus.OPEN,
        assignee = owner
      )
    )
    val event = EventPayloads.event

    "return status 200 for manager" in {
      givenAuthSuccess("manager")
      shouldSucceed
    }

    "return status 200 for case owner" in {
      givenAuthSuccess("team")
      shouldSucceed
    }

    "redirect for non case owner" in {
      givenAuthSuccess("another team member")
      shouldFail
    }

    "redirect on auth failure" in {
      givenAuthFailed()
      shouldFail
    }

    def shouldSucceed = {
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

    def shouldFail = {
      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/reassign-case?origin=/").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

}
