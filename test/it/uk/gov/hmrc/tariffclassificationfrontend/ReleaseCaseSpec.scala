package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import util.{CasePayloads, WiremockTestServer, oCase}

class ReleaseCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Release" should {
    val caseWithStatusNEW = CasePayloads.jsonOf(oCase.btiCaseExample.copy(status = CaseStatus.NEW))

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(caseWithStatusNEW))
      )

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/release").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-medium mt-0\">Release this Case for Classification</h3>")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/release").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorized to access this page.")
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
//      val response: WSResponse = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/release").post(Map("queue" -> Seq("cars"), "csrfToken" -> Seq(token))))
//
//      // Then
//      response.status shouldBe OK
//      response.body should include("<h3 class=\"heading-medium mt-0\">This case has been released to the Cars queue</h3>")
//    }
//  }

}
