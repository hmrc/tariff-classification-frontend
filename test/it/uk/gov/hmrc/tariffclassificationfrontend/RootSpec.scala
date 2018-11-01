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

class RootSpec extends UnitSpec with WiremockTestServer with MockitoSugar with GuiceOneServerPerSuite {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Map("microservice.services.binding-tariff-classification.port" -> wirePort))
    .build()

  private val ws = fakeApplication().injector.instanceOf[WSClient]

  "Root" should {

    "return status 200 and redirect to My Cases" in {
      // Given
      stubFor(get(urlEqualTo("/cases?assignee_id=0&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 class=\"heading-large\">My Cases</h1>")
    }
  }

}
