package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Pagination}
import uk.gov.tariffclassificationfrontend.utils.Cases._
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, EventPayloads}

class LiabilitySpec extends IntegrationTest with MockitoSugar {

  private val liabilityCase: Case = aCase(withReference("1"), withLiabilityApplication())

  "Liability Summary" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.jsonOf(liabilityCase)))
      )

      // When
      val response = await(ws.url(s"$baseUrl/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"liability-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1")
    }
  }

  "Liability Details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.jsonOf(liabilityCase)))
      )

      // When
      val response = await(ws.url(s"$baseUrl/cases/1/liability").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"liability-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1/application")
    }
  }



  "Liability Activity details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.jsonOf(liabilityCase)))
      )
      givenAuthSuccess()
      stubFor(get(urlEqualTo(s"/events?case_reference=1&type=QUEUE_CHANGE&type=APPEAL_ADDED&type=APPEAL_STATUS_CHANGE&type=EXTENDED_USE_STATUS_CHANGE&type=CASE_STATUS_CHANGE&type=CASE_REFERRAL&type=NOTE&type=CASE_COMPLETED&type=CASE_CANCELLATION&type=ASSIGNMENT_CHANGE&page=1&page_size=${Pagination.unlimited}"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(EventPayloads.pagedEvents))
      )
      // When
      val response = await(ws.url(s"$baseUrl/cases/1/activity").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"activity-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1/activity")
    }
  }


  "Liability Attachments details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.jsonOf(liabilityCase)))
      )
      stubFor(post(urlEqualTo("/file?id="))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("[]")
        )
      )

      // When
      val response = await(ws.url(s"$baseUrl/cases/1/attachments").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"attachments-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("cases/1/attachments")
    }
  }

}
