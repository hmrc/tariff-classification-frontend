package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Operator, Role}
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases, EventPayloads}


class ReferCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Refer" should {
    val owner = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
    val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.OPEN, assignee = owner))
    val event = EventPayloads.event

    "return status 200 for manager" in {
      // Given
      givenAuthSuccess("manager")
      shouldSucceed
    }

    "return status 200 for case owner" in {
      // Given
      givenAuthSuccess("team")
      shouldSucceed
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()
      shouldFail
    }

    "redirect for non case owner" in {
      // Given
      givenAuthSuccess("another team member")
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
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/refer").get())

      // Then
      response.status shouldBe OK
      response.body should include("Change case status to: Referred")
    }

    def shouldFail = {
      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/refer").get())

      // Then
      response.status shouldBe OK
      response.body should include(messages("not_authorised.paragraph1"))
    }


  }

}
