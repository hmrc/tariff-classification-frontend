package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Operator, Role}
import uk.gov.tariffclassificationfrontend.utils.Cases.{aCase, withDecision}
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases}


class AppealCaseSpec extends IntegrationTest with MockitoSugar {

  val owner = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
  val caseWithStatusCOMPLETE = CasePayloads.jsonOf(aCase(withDecision()).copy(assignee = owner, status = CaseStatus.COMPLETED))

  "Case Appeal" should {

    "return status 200 for manager" in {
      // Given
      givenAuthSuccess("manager")
      shouldSucceed
    }

    "return status 200 for team member" in {
      givenAuthSuccess("team")
      shouldSucceed
    }

    "return status 200 for another team member" in {
      givenAuthSuccess("another team member")
      shouldSucceed
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()
      shouldFail
    }

    def shouldFail = {
      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/appeal").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }

    def shouldSucceed = {
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatusCOMPLETE))
      )

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/appeal").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"appeal-heading\"")
    }
  }

  "Case Appeal Change" should {

    "return status 200 for manager" in {
      // Given
      givenAuthSuccess("manager")
      shouldSucceed
    }

    "return status 200 for team member" in {
      givenAuthSuccess("team")
      shouldSucceed
    }

    "return status 200 for another team member" in {
      givenAuthSuccess("another team member")
      shouldSucceed
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()
      shouldFail
    }

    def shouldSucceed = {
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatusCOMPLETE))
      )

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/new-appeal").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"appeal_choose_type-heading\"")
    }

    def shouldFail = {
      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/new-appeal").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }


  }

}
