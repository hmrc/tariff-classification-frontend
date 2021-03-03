package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{CaseStatus, Operator, Pagination, Role}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.{CasePayloads, EventPayloads, KeywordsPayloads}
import utils.Cases.{aCase, withDecision}
import utils.JsonFormatters._

class AppealCaseSpec extends IntegrationTest with MockitoSugar {

  val owner = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
  val caseWithStatusCOMPLETE =
    CasePayloads.jsonOf(aCase(withDecision()).copy(assignee = owner, status = CaseStatus.COMPLETED))

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
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/appeal").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

    def shouldSucceed = {
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusCOMPLETE)
          )
      )
      stubFor(
        get(
          urlEqualTo(
            "/events?case_reference=1" +
              "&type=SAMPLE_STATUS_CHANGE&type=SAMPLE_RETURN_CHANGE" +
              s"&page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(EventPayloads.pagedSampleEvents)
          )
      )
      stubFor(
        get(
          urlEqualTo(
            "/events?case_reference=1" +
              "&type=EXPERT_ADVICE_RECEIVED&type=CASE_REJECTED&type=QUEUE_CHANGE&type=APPEAL_ADDED" +
              "&type=APPEAL_STATUS_CHANGE&type=EXTENDED_USE_STATUS_CHANGE" +
              "&type=CASE_STATUS_CHANGE&type=CASE_REFERRAL&type=NOTE&type=CASE_COMPLETED" +
              "&type=CASE_CANCELLATION&type=CASE_CREATED&type=ASSIGNMENT_CHANGE" +
              s"&page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(EventPayloads.pagedEvents)
          )
      )
      stubFor(
        get(
          urlEqualTo(
            s"/keywords?page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(KeywordsPayloads.pagedKeywords)
        )
      )

      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/appeal").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"appeal-heading\"")
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
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusCOMPLETE)
          )
      )

      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/new-appeal").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"appeal_choose_type-heading\"")
    }

    def shouldFail = {
      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/new-appeal").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

  }

}
