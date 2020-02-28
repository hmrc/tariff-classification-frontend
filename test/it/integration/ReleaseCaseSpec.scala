package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import models.CaseStatus
import utils.{CasePayloads, Cases, EventPayloads}


class ReleaseCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Release" should {
    val caseWithStatusNEW = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.NEW,
      application = Cases.btiApplicationExample.copy(envisagedCommodityCode = Some("01234567890"))))
    val event = EventPayloads.event

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatusNEW))
      )
      stubFor(post(urlEqualTo("/cases/1/events"))
        .willReturn(aResponse()
          .withStatus(CREATED)
          .withBody(event))
      )

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/release").get())

      // Then
      response.status shouldBe OK
      response.body should include("Release this case for classification")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/release").get())

      // Then
      response.status shouldBe OK
      response.body should include(messages("not_authorised.paragraph1"))
    }
  }

  // TODO: DIT-246 - fix this test
//  "Case Release To Queue" should {
//    val caseWithStatusNEW = CasePayloads.jsonOf(CaseExamples.btiCaseExample.copy(status = CaseStatus.NEW))
//    val caseWithStatusOPEN = CasePayloads.jsonOf(CaseExamples.btiCaseExample.copy(status = CaseStatus.OPEN))
//    val token = fakeApplication().injector.instanceOf[TokenProvider].generateToken
//
//    "return status 200" in {
//      // Given
//      stubFor(get(urlEqualTo("/cases/1"))
//        .willReturn(aResponse()
//          .withStatus(OK)
//          .withBody(caseWithStatusNEW))
//      )
//      stubFor(put(urlEqualTo("/cases/1"))
//        .willReturn(aResponse()
//          .withStatus(OK)
//          .withBody(caseWithStatusOPEN))
//      )
//
//      // When
//      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/cases/1/release").post(Map("queue" -> Seq("cars"), "csrfToken" -> Seq(token))))
//
//      // Then
//      response.status shouldBe OK
//      response.body should include("<h3 class=\"heading-medium mt-0\">This case has been released to the Cars queue</h3>")
//    }
//  }

}
