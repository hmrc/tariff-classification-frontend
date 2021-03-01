package integration

import com.github.tomakehurst.wiremock.client.WireMock.{stubFor, _}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import models.{Case, Pagination}
import utils.Cases._
import utils.{CasePayloads, EventPayloads, KeywordsPayloads}
import utils.JsonFormatters._

class LiabilitySpec extends IntegrationTest with MockitoSugar {

  private val liabilityCase: Case = aCase(withReference("1"), withLiabilityApplication())

  "Liability Summary" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.jsonOf(liabilityCase))
          )
      )
      givenAuthSuccess()
      stubFor(
        post(urlEqualTo("/file?id="))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("[]")
          )
      )
      givenAuthSuccess()
      stubFor(
        get(
          urlEqualTo(
            s"/events?case_reference=1&type=EXPERT_ADVICE_RECEIVED&type=QUEUE_CHANGE&type=APPEAL_ADDED&type=APPEAL_STATUS_CHANGE&type=EXTENDED_USE_STATUS_CHANGE&type=CASE_STATUS_CHANGE&type=CASE_REFERRAL&type=NOTE&type=CASE_COMPLETED&type=CASE_CANCELLATION&type=CASE_CREATED&type=ASSIGNMENT_CHANGE&page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(EventPayloads.pagedEvents)
        )
      )
      givenAuthSuccess()
      stubFor(
        get(
          urlEqualTo(
            s"/events?case_reference=1&type=SAMPLE_STATUS_CHANGE&type=SAMPLE_RETURN_CHANGE&page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(EventPayloads.pagedEmpty)
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
      val response = await(ws.url(s"$baseUrl/cases/v2/1/liability").get())

      // Then
      response.status shouldBe OK
      response.body   should include("id=\"liability-entry-number\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/v2/1/liability")
    }
  }
}
