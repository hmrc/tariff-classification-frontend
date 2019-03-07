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
import play.api.libs.Files.TemporaryFile
import uk.gov.hmrc.tariffclassificationfrontend.models.response.{FileMetadata, ScanStatus}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Attachment, FileUpload}

class FileStoreConnectorSpec extends ConnectorTest {

  private val connector = new FileStoreConnector(appConfig, authenticatedHttpClient, wsClient)

  "Connector 'GET' one" should {
    "handle 404" in {
      val att = mock[Attachment]
      given(att.id) willReturn "id"

      stubFor(
        get("/file/id")
          .willReturn(aResponse().withStatus(Status.NOT_FOUND))
      )

      await(connector.get(att)) shouldBe None

      verify(
        getRequestedFor(urlEqualTo("/file/id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "handle response with mandatory fields only" in {
      val att = mock[Attachment]
      given(att.id) willReturn "id"

      stubFor(
        get("/file/id")
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
          scanStatus = None
        )
      )

      verify(
        getRequestedFor(urlEqualTo("/file/id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "handle response with optional fields" in {
      val att = mock[Attachment]
      given(att.id) willReturn "id"

      stubFor(
        get("/file/id")
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
          scanStatus = Some(ScanStatus.READY)
        )
      )

      verify(
        getRequestedFor(urlEqualTo("/file/id"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
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
        get("/file?id=id1&id=id2")
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
          scanStatus = None
        )
      )

      verify(
        getRequestedFor(urlEqualTo("/file?id=id1&id=id2"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }

    "handle response with optional fields" in {
      val att1 = mock[Attachment]
      given(att1.id) willReturn "id1"
      val att2 = mock[Attachment]
      given(att2.id) willReturn "id2"

      stubFor(
        get("/file?id=id1&id=id2")
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
          scanStatus = Some(ScanStatus.READY)
        )
      )

      verify(
        getRequestedFor(urlEqualTo("/file?id=id1&id=id2"))
          .withHeader("X-Api-Token", equalTo(realConfig.apiToken))
      )
    }
  }

  "Upload" in {
    stubFor(
      post("/file")
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withBody(fromResource("filestore/binding-tariff-filestore_upload-response.json"))
        )
    )

    val file = FileUpload(TemporaryFile("example-file.txt"), "file.txt", "text/plain")
    val result = await(connector.upload(file))

    verify(
      postRequestedFor(
        urlEqualTo("/file"))
        .withHeader("X-Api-Token", equalTo(fakeAuthToken))
        .withRequestBody(containing("file"))
        .withRequestBody(containing("publish"))
    )

    result shouldBe FileMetadata(
      id = "id",
      fileName = "file-name.txt",
      mimeType = "text/plain"
    )
  }

}
