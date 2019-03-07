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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.BDDMockito.given
import play.api.http.Status
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig


class RulingConnectorTest extends ConnectorTest {

  private val config = mock[AppConfig]

  private val connector = new RulingConnector(config, authenticatedWSClient)

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
