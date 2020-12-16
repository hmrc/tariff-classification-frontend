package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{CREATED, OK}
import models.{CaseStatus, Operator, Role}
import utils.{CasePayloads, Cases, EventPayloads}
import utils.JsonFormatters._

class ReAssignCaseSpec extends IntegrationTest with MockitoSugar {

  private val owner = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
  private val caseAssignedToOwner = CasePayloads.jsonOf(
    Cases.btiCaseExample
      .copy(
        status   = CaseStatus.OPEN,
        assignee = owner
      )
  )
  private val caseUnassigned = CasePayloads.jsonOf(
    Cases.btiCaseExample
      .copy(
        status = CaseStatus.OPEN
      )
  )
  private val event = EventPayloads.event

  "Re-Assign Assigned Case" should {

    "return status 200 for manager" in {
      givenAuthSuccess("manager")
      whenCaseExists(caseAssignedToOwner)
      shouldSucceed
    }

    "return status 200 for case owner" in {
      givenAuthSuccess("team")
      whenCaseExists(caseAssignedToOwner)
      shouldSucceed
    }

    "redirect for non case owner" in {
      givenAuthSuccess("another team member")
      whenCaseExists(caseAssignedToOwner)
      shouldFail
    }

    "redirect on auth failure" in {
      givenAuthFailed()
      whenCaseExists(caseAssignedToOwner)
      shouldFail
    }
  }

  "Re-Assign Unassigned Case" should {

    "return status 200 for manager" in {
      givenAuthSuccess("manager")
      whenCaseExists(caseUnassigned)
      shouldSucceed
    }

    "redirect for team member" in {
      givenAuthSuccess("team")
      whenCaseExists(caseUnassigned)
      shouldFail
    }

    "redirect on auth failure" in {
      givenAuthFailed()
      whenCaseExists(caseUnassigned)
      shouldFail
    }
  }

  private def whenCaseExists(caseJson: String) = {
    stubFor(
      get(urlEqualTo("/cases/1"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(caseJson)
        )
    )
    stubFor(
      post(urlEqualTo("/cases/1/events"))
        .willReturn(
          aResponse()
            .withStatus(CREATED)
            .withBody(event)
        )
    )
  }

  private def shouldSucceed = {
    // When
    val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/reassign-case?origin=/").get())

    // Then
    response.status shouldBe OK
    response.body   should include("Move this case back to a queue")
  }

  private def shouldFail = {
    // When
    val response: WSResponse = await(ws.url(s"$baseUrl/cases/1/reassign-case?origin=/").get())

    // Then
    response.status shouldBe OK
    response.body   should include(messages("not_authorised.paragraph1"))
  }
}
