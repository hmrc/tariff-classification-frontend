package integration

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlEqualTo}
import com.kenshoo.play.metrics.Metrics
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.Helpers.{OK, UNAUTHORIZED}
import utils.{ResourceFiles, UnitSpec, WiremockTestServer}
import utils.TestMetrics

trait IntegrationTest extends UnitSpec with GuiceOneServerPerSuite with ResourceFiles with WiremockTestServer {

  val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq(Lang.defaultLang))

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .overrides(bind[Metrics].toInstance(new TestMetrics))
      .configure(
        Map(
          "microservice.services.binding-tariff-classification.port" -> wirePort,
          "microservice.services.binding-tariff-filestore.port"      -> wirePort,
          "microservice.services.auth.port"                          -> wirePort,
          "microservice.services.pdf-generator-service.port"         -> wirePort
        )
      )
      .build()

  protected val ws: WSClient = app.injector.instanceOf[WSClient]

  protected val baseUrl = s"http://localhost:$port/manage-tariff-classifications"

  protected def givenAuthSuccess(role: String = "manager"): Unit = {

    val resource = role match {
      case "manager"   => "auth-success-manager.json"
      case "team"      => "auth-success-team-member.json"
      case "read-only" => "auth-success-read-only.json"
      case _           => "auth-success-another-team-member.json"
    }

    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(fromResource(resource))
        )
    )
  }

  protected def givenAuthFailed(): Unit =
    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(UNAUTHORIZED)
        )
    )

  protected def verifyNotAuthorisedFor(path: String): Unit = {
    givenAuthFailed()

    val response = await(ws.url(s"$baseUrl/$path").get())

    response.status shouldBe OK
    response.body   should include(messages("not_authorised.paragraph1"))
  }

}
