package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Decision}
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases, EventPayloads}


class CompleteCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Complete with decision" should {

    val completeDecision = Decision(bindingCommodityCode = "040900", justification = "justification-content",
      goodsDescription = "goods-description", methodSearch = Some("method-to-search"))
    val inCompleteDecision = Decision(bindingCommodityCode = "", justification = "", goodsDescription = "")
    val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.OPEN, decision = Some(completeDecision)))
    val caseIncompleteWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.OPEN, decision = Some(inCompleteDecision)))
    val event = EventPayloads.event

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatusOPEN))
      )

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/complete").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-large mt-0\">Complete this case</h3>")
      response.body should not include ("disabled=disabled")
    }

    "return disabled complete button when no complete deicision" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseIncompleteWithStatusOPEN))
      )

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/complete").get())

      // Then
      response.status shouldBe OK

      response.body should include("id=\"complete-case-button\"")
      response.body should include("disabled=\"disabled\"")
      response.body should include("Complete case")

    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/complete").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }
  }

  //TODO DIT-291 This returns 403s due to CSRF issues
//  "Case Complete Confirm" should {
//    val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.OPEN))
//    val event = EventPayloads.event
//
//    val csrfProvider = app.injector.instanceOf[TokenProvider]
//
//    "return status 200" in {
//      // Given
//      givenAuthSuccess()
//      stubFor(get(urlEqualTo("/cases/1"))
//        .willReturn(aResponse()
//          .withStatus(OK)
//          .withBody(caseWithStatusOPEN))
//      )
//      stubFor(post(urlEqualTo("/cases/1/events"))
//        .willReturn(aResponse()
//          .withStatus(CREATED)
//          .withBody(event))
//      )
//      stubFor(post(urlEqualTo("/hmrc/email"))
//        .willReturn(aResponse()
//          .withStatus(ACCEPTED))
//      )
//      stubFor(post(urlEqualTo(s"/templates/${EmailType.COMPLETE}"))
//        .withRequestBody(new EqualToJsonPattern(fromResource("parameters_email-request.json"), true, false))
//        .willReturn(aResponse()
//          .withBody(fromResource("email_template-response.json"))
//          .withStatus(HttpStatus.SC_OK))
//      )
//
//      // When
//      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/complete").post(Map("csrfToken" -> Seq(csrfProvider.generateToken))))
//
//      // Then
//      response.status shouldBe OK
//      response.body should include("<h3 class=\"heading-large mt-0\">Complete this case</h3>")
//    }
//
//    "redirect on auth failure" in {
//      // Given
//      givenAuthFailed()
//
//      // When
//      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/complete").post(Map("csrfToken" -> Seq(csrfProvider.generateToken))))
//
//      // Then
//      response.status shouldBe OK
//      response.body should include("You are not authorised to access this page.")
//    }
//  }

}
