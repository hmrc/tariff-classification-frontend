/*
 * Copyright 2018 HM Revenue & Customs
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
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import org.apache.http.HttpStatus
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.json.{Format, OFormat}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters
import uk.gov.tariffclassificationfrontend.utils.{ResourceFiles, WiremockTestServer}

class EmailConnectorSpec extends UnitSpec
  with WiremockTestServer with MockitoSugar with WithFakeApplication with ResourceFiles {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val configuration = mock[AppConfig]
  private val actorSystem: ActorSystem = ActorSystem("test")
  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val client = new DefaultHttpClient(fakeApplication.configuration, auditConnector, wsClient, actorSystem)
  private val email = CaseCompletedEmail(Seq("user@domain.com"), CaseCompletedEmailParameters("name", "case-ref", "item-name"))

  private val connector = new EmailConnector(configuration, client)

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    given(configuration.emailUrl).willReturn(getUrl)
    given(configuration.emailRendererUrl).willReturn(getUrl)
  }

  "Connector 'Send'" should {
    implicit val format: Format[Email[_]] = JsonFormatters.emailFormat

    "POST Email payload" in {
      stubFor(post(urlEqualTo("/hmrc/email"))
          .withRequestBody(new EqualToJsonPattern(fromResource("completion_email-request.json"), true, false))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_ACCEPTED))
      )

      await(connector.send(email))
    }
  }

  "Connector 'Generate'" should {
    implicit val format: OFormat[CaseCompletedEmailParameters] = JsonFormatters.emailCompleteParamsFormat

    "POST Email parameters" in {
      stubFor(post(urlEqualTo(s"/templates/${EmailType.COMPLETE}"))
        .withRequestBody(new EqualToJsonPattern(fromResource("parameters_email-request.json"), true, false))
        .willReturn(aResponse()
            .withBody(fromResource("email_template-response.json"))
          .withStatus(HttpStatus.SC_OK))
      )

      await(connector.generate(email)) shouldBe EmailTemplate("text", "html", "from", "subject", "service")
    }
  }

}
