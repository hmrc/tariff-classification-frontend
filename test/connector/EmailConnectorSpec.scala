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

package connector

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import models._
import org.apache.http.HttpStatus
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.JsonFormatters.{emailCompleteParamsFormat, emailFormat}

class EmailConnectorSpec extends ConnectorTest {

  private val email =
    CaseCompletedEmail(
      Seq("user@domain.com"),
      CaseCompletedEmailParameters(
        recipientName_line1 = "name",
        reference = "case-ref",
        goodsName = "item-name",
        officerName = "officer",
        dateSubmitted = "01 Jan 2021"
      )
    )

  private val connector = new EmailConnector(mockAppConfig, httpClient, metrics)

  "Connector 'Send'" should {

    "POST Email payload" in {
      stubFor(
        post(urlEqualTo("/hmrc/email"))
          .withRequestBody(new EqualToJsonPattern(fromResource("completion_email-request.json"), true, false))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_ACCEPTED)
          )
      )

      await(connector.send(email))

      verify(
        postRequestedFor(urlEqualTo("/hmrc/email"))
          .withoutHeader("X-Api-Token")
      )
    }

    "propagate errors" in {
      stubFor(
        post(urlEqualTo("/hmrc/email"))
          .withRequestBody(new EqualToJsonPattern(fromResource("completion_email-request.json"), true, false))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_BAD_GATEWAY)
          )
      )

      intercept[UpstreamErrorResponse] {
        await(connector.send(email))
      }

      verify(
        postRequestedFor(urlEqualTo("/hmrc/email"))
          .withoutHeader("X-Api-Token")
      )
    }

  }

  "Connector 'Generate'" should {

    "POST Email parameters" in {
      stubFor(
        post(urlEqualTo(s"/templates/${EmailType.COMPLETE}"))
          .withRequestBody(new EqualToJsonPattern(fromResource("parameters_email-request.json"), true, false))
          .willReturn(
            aResponse()
              .withBody(fromResource("email_template-response.json"))
              .withStatus(HttpStatus.SC_OK)
          )
      )

      await(connector.generate(email)) shouldBe EmailTemplate("text", "html", "from", "subject", "service")

      verify(
        postRequestedFor(urlEqualTo(s"/templates/${EmailType.COMPLETE}"))
          .withoutHeader("X-Api-Token")
      )
    }
  }

}
