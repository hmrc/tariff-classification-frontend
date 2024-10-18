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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status.{ACCEPTED, BAD_GATEWAY}
import uk.gov.hmrc.http.UpstreamErrorResponse

class RulingConnectorSpec extends ConnectorTest {

  private val connector: RulingConnector = new RulingConnector(mockAppConfig, httpClient, metrics)

  "Connector Publish" should {

    "POST to the Ruling Store" in {
      stubFor(
        post("/search-for-advance-tariff-rulings/ruling/id")
          .willReturn(
            aResponse()
              .withStatus(ACCEPTED)
          )
      )

      await(connector.notify("id"))

      verify(
        postRequestedFor(urlEqualTo("/search-for-advance-tariff-rulings/ruling/id"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }

    "propagate errors" in {
      stubFor(
        post("/search-for-advance-tariff-rulings/ruling/id")
          .willReturn(
            aResponse()
              .withStatus(BAD_GATEWAY)
          )
      )

      intercept[UpstreamErrorResponse] {
        await(connector.notify("id"))
      }

      verify(
        postRequestedFor(urlEqualTo("/search-for-advance-tariff-rulings/ruling/id"))
          .withHeader("X-Api-Token", equalTo(fakeAuthToken))
      )
    }
  }

}
