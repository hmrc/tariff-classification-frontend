package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases, EventPayloads}


class CompleteCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Complete" should {
    val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.OPEN))
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
      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/complete").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-large mt-0\">Complete this case</h3>")
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
