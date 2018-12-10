package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlEqualTo}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.Helpers.{OK, UNAUTHORIZED}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.tariffclassificationfrontend.utils.WiremockTestServer

trait IntegrationTest extends UnitSpec with GuiceOneServerPerSuite with ResourceFiles with WiremockTestServer {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Map(
      "microservice.services.binding-tariff-classification.port" -> wirePort,
      "microservice.services.auth.port" -> wirePort
    ))
    .build()

  protected val ws = fakeApplication().injector.instanceOf[WSClient]
  protected val appRoot = s"http://localhost:$port/tariff-classification"

  protected def givenAuthSuccess(): Unit = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(fromFile("test/it/resources/auth-success.json"))
      )
    )
  }

  protected def givenAuthFailed(): Unit = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(
        aResponse()
          .withStatus(UNAUTHORIZED)
      )
    )
  }

}
