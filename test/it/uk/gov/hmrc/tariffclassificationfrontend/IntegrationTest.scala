package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlEqualTo}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.Helpers.{OK, UNAUTHORIZED}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.tariffclassificationfrontend.utils.{ResourceFiles, WiremockTestServer}

trait IntegrationTest extends UnitSpec with GuiceOneServerPerSuite with ResourceFiles with WiremockTestServer {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure(Map(
      "microservice.services.binding-tariff-classification.port" -> wirePort,
      "microservice.services.binding-tariff-filestore.port" -> wirePort,
      "microservice.services.auth.port" -> wirePort
    ))
    .build()

  protected val ws = fakeApplication().injector.instanceOf[WSClient]
  protected val frontendRoot = s"http://localhost:$port/tariff-classification"
  protected val filestoreRoot = s"http://localhost:$port"

  protected def givenAuthSuccess(): Unit = {
    stubFor(post(urlEqualTo("/auth/authorise"))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(fromResource("auth-success.json"))
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

  protected def verifyNotAuthorisedFor(path : String) = {

    givenAuthFailed()

    val response = await(ws.url(s"$frontendRoot/$path").get())

    response.status shouldBe OK
    response.body should include("You are not authorised to access this page.")
  }

}
