/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import com.kenshoo.play.metrics.Metrics
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.Helpers.{OK, UNAUTHORIZED}
import utils.{ResourceFiles, TestMetrics, UnitSpec, WiremockTestServer}

trait IntegrationTest
    extends UnitSpec
    with GuiceOneServerPerSuite
    with ResourceFiles
    with WiremockTestServer
    with MockSessionCookie {

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
//          "platform-url.host" -> s"http://localhost:$port",
          // "play.filters.https.redirectEnabled" -> "false"
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

    val userInfoResource = role match {
      case "manager"   => "user-info-manager.json"
      case "team"      => "user-info-team-member.json"
      case "read-only" => "user-info-read-only.json"
      case _           => "user-info-another-team-member.json"
    }

    stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(fromResource(resource))
        )
    )

    stubFor(
      post(urlEqualTo("/users"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(fromResource(userInfoResource))
        )
    )

    stubFor(
      get(urlMatching("/users/\\d+"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(fromResource(userInfoResource))
        )
    )

    stubFor(
      put(urlMatching("/users/\\d+"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(fromResource(userInfoResource))
        )
    )
  }

  def request(path: String, sessionId: String = "123"): WSRequest =
    ws.url(s"$baseUrl$path")
      .withHttpHeaders(
        "X-Session-ID"  -> sessionId,
        "Authorization" -> "Bearer 121"
      )

  def requestWithSession(path: String, sessionId: String = "sessionId"): WSRequest =
    request(path, sessionId)
      .withCookies(mockSessionCookie(sessionId))

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

    val response = await(requestWithSession(path).get())

    response.status shouldBe OK
    response.body   should include(messages("not_authorised.paragraph1"))
  }

}
