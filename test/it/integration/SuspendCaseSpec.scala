package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import models.{CaseStatus, Operator, Role}
import utils.{CasePayloads, Cases, EventPayloads}
import utils.JsonFormatters._

class SuspendCaseSpec extends IntegrationTest with MockitoSugar {

  val owner              = Some(Operator("111", role                            = Role.CLASSIFICATION_OFFICER))
  val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.OPEN, assignee = owner))

  "Case Suspend" should {

    "return status 200 for manager" in {
      givenAuthSuccess("manager")
      shouldSucceed
    }

    "return status 200 for case owner" in {
      givenAuthSuccess("team")
      shouldSucceed
    }

    "redirect on auth failure" in {
      givenAuthFailed()
      shouldFail
    }

    "redirect on for non-case owner" in {
      givenAuthSuccess("another team member")
      shouldFail
    }

    def shouldFail = {
      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/suspend").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

    def shouldSucceed = {
      // When
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusOPEN)
          )
      )
      stubFor(
        post(urlEqualTo("/cases/1/events"))
          .willReturn(
            aResponse()
              .withStatus(CREATED)
              .withBody(EventPayloads.event)
          )
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/suspend").get())

      // Then
      response.status shouldBe OK
      response.body   should include("Change case status to: Suspended")
    }
  }

}
