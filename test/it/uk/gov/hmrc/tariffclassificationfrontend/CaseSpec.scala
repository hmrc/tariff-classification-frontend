package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.connector.WiremockTestServer
import uk.gov.hmrc.tariffclassificationfrontend.utils.CasePayloads

class CaseSpec extends UnitSpec with WiremockTestServer with MockitoSugar with GuiceOneServerPerSuite {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Map("microservice.services.binding-tariff-classification.port" -> 20001))
    .build()

  private val ws = fakeApplication().injector.instanceOf[WSClient]

  "Unknown Case" should {

    "return status 200 with Case Not Found" in {
      // Given
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(NOT_FOUND))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("Case not found")
    }
  }

  "Case Summary" should {

    "return status 200" in {
      // Given
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-medium mt-0\">Summary</h3>")
    }
  }

  "Case Application Details" should {

    "return status 200" in {
      // Given
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/application").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-medium mt-0\">Application Details</h3>")
    }
  }

  "Case Ruling Details" should {

    "return status 200" in {
      // Given
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/cases/1/ruling").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h3 class=\"heading-medium mt-0\">Ruling</h3>")
    }
  }

}
