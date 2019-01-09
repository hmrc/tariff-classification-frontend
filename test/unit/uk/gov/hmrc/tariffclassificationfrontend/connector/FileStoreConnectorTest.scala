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

import java.time.{LocalDate, ZoneOffset}

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.BDDMockito.given
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.http.Status
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Attachment
import uk.gov.hmrc.tariffclassificationfrontend.models.response.{FileMetadata, ScanStatus}
import uk.gov.tariffclassificationfrontend.utils.{ResourceFiles, WiremockTestServer}

class FileStoreConnectorTest extends UnitSpec with WiremockTestServer with MockitoSugar with WithFakeApplication with BeforeAndAfterEach with ResourceFiles {

  private val config = mock[AppConfig]
  private implicit val headers: HeaderCarrier = HeaderCarrier()

  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val HMRCWSClient = new DefaultHttpClient(fakeApplication.configuration, auditConnector, wsClient, ActorSystem("test"))

  private val connector = new FileStoreConnector(config, HMRCWSClient)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    given(config.fileStoreUrl) willReturn wireMockUrl
  }

  "Connector 'GET' one" should {
    "handle 404" in {
      val att = mock[Attachment]
      given(att.id) willReturn "id"

      stubFor(
        get("/binding-tariff-filestore/file/id")
          .willReturn(aResponse().withStatus(Status.NOT_FOUND))
      )

      await(connector.get(att)) shouldBe None
    }

    "handle response with mandatory fields only" in {
      val att = mock[Attachment]
      given(att.id) willReturn "id"

      stubFor(
        get("/binding-tariff-filestore/file/id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(fromResource("filestore/single_file_with_mandatory_fields-response.json"))
          )
      )

      await(connector.get(att)) shouldBe Some(
        FileMetadata(
          id = "id",
          fileName = "name",
          mimeType = "text/plain",
          url = None,
          scanStatus = None,
          lastUpdated = LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
        )
      )
    }

    "handle response with optional fields" in {
      val att = mock[Attachment]
      given(att.id) willReturn "id"

      stubFor(
        get("/binding-tariff-filestore/file/id")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(fromResource("filestore/single_file_with_optional_fields-response.json"))
          )
      )

      await(connector.get(att)) shouldBe Some(
        FileMetadata(
          id = "id",
          fileName = "name",
          mimeType = "text/plain",
          url = Some("url"),
          scanStatus = Some(ScanStatus.READY),
          lastUpdated = LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
        )
      )
    }
  }

  "Connector 'GET' many" should {
    "GET with No IDs" in {
      // Should not make any requests
      await(connector.get(Seq.empty)) shouldBe Seq.empty
    }

    "handle response with mandatory fields" in {
      val att1 = mock[Attachment]
      given(att1.id) willReturn "id1"
      val att2 = mock[Attachment]
      given(att2.id) willReturn "id2"

      stubFor(
        get("/binding-tariff-filestore/file?id=id1&id=id2")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(fromResource("filestore/multi_file_with_mandatory_fields-response.json"))
          )
      )

      await(connector.get(Seq(att1, att2))) shouldBe Seq(
        FileMetadata(
          id = "id",
          fileName = "name",
          mimeType = "text/plain",
          url = None,
          scanStatus = None,
          lastUpdated = LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
        )
      )
    }

    "handle response with optional fields" in {
      val att1 = mock[Attachment]
      given(att1.id) willReturn "id1"
      val att2 = mock[Attachment]
      given(att2.id) willReturn "id2"

      stubFor(
        get("/binding-tariff-filestore/file?id=id1&id=id2")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(fromResource("filestore/multi_file_with_optional_fields-response.json"))
          )
      )

      await(connector.get(Seq(att1, att2))) shouldBe Seq(
        FileMetadata(
          id = "id",
          fileName = "name",
          mimeType = "text/plain",
          url = Some("url"),
          scanStatus = Some(ScanStatus.READY),
          lastUpdated = LocalDate.of(2019, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
        )
      )
    }
  }

}
