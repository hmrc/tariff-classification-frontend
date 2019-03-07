/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.connector

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.http.Status
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.tariffclassificationfrontend.utils.{ResourceFiles, WiremockTestServer}

class RulingConnectorTest extends UnitSpec with WithFakeApplication with WiremockTestServer with MockitoSugar with ResourceFiles {

  private val config = mock[AppConfig]
  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val actorSystem = ActorSystem.create("test")
  private val realConfig: AppConfig = fakeApplication.injector.instanceOf[AppConfig]
  private val hmrcWsClient = new AuthenticatedHttpClient(realConfig, auditConnector, wsClient, actorSystem)
  private implicit val headers: HeaderCarrier = HeaderCarrier()

  private val connector = new RulingConnector(config, hmrcWsClient)

  "Connector Publish" should {

    "POST to the Ruling Store" in {
      given(config.rulingUrl).willReturn(wireMockUrl)

      stubFor(
        post("/binding-tariff-rulings/ruling/id")
          .willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
          )
      )

      await(connector.notify("id"))

      verify(
        postRequestedFor(urlEqualTo("/binding-tariff-rulings/ruling/id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

}
