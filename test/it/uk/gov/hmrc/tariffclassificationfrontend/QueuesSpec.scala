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

class QueuesSpec extends UnitSpec with WiremockTestServer with MockitoSugar with GuiceOneServerPerSuite {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Map("microservice.services.binding-tariff-classification.port" -> wirePort))
    .build()

  private val ws = fakeApplication().injector.instanceOf[WSClient]

  "My Cases" should {

    "return status 200" in {
      // Given
      stubFor(get(urlEqualTo("/cases?assignee_id=0&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/queues").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">My Cases</h1>")
    }
  }

  "Gateway Cases" should {

    "return status 200" in {
      // Given
      stubFor(get(urlEqualTo("/cases?queue_id=none&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
        )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">Gateway Cases</h1>")
    }
  }

  "ACT Cases" should {

    "return status 200" in {
      // Given
      stubFor(get(urlEqualTo("/cases?queue_id=2&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/queues/act").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">ACT Cases</h1>")
    }
  }

  "CAP Cases" should {

    "return status 200" in {
      // Given
      stubFor(get(urlEqualTo("/cases?queue_id=3&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/queues/cap").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">CAP Cases</h1>")
    }
  }

  "Cars Cases" should {

    "return status 200" in {
      // Given
      stubFor(get(urlEqualTo("/cases?queue_id=4&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/queues/cars").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">Cars Cases</h1>")
    }
  }

  "ELM Cases" should {

    "return status 200" in {
      // Given
      stubFor(get(urlEqualTo("/cases?queue_id=5&assignee_id=none&status=NEW,OPEN,REFERRED,SUSPENDED&sort-by=elapsed-days"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.gatewayCases))
      )

      // When
      val response = await(ws.url(s"http://localhost:$port/tariff-classification/queues/elm").get())

      // Then
      response.status shouldBe OK
      response.body should include("<h1 id=\"queue-name\" class=\"heading-large\">ELM Cases</h1>")
    }
  }

}
