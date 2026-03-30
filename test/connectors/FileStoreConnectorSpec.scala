/*
 * Copyright 2025 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import config.AppConfig
import models.request.FileStoreInitiateRequest
import models.response.*
import models.{Attachment, FileUpload}
import org.mockito.Mockito.*
import play.api.http.Status.*
import play.api.libs.Files.SingletonTemporaryFileCreator
import uk.gov.hmrc.http.UpstreamErrorResponse

class FileStoreConnectorSpec extends ConnectorTest {

  private val config = fakeApplication().injector.instanceOf[AppConfig]

  when(mockAppConfig.maxUriLength).thenReturn(2048L)
  when(mockAppConfig.fileStoreUrl).thenReturn(wireMockUrl)

  private val attachmentId = "id"
  private val connector    = new FileStoreConnector(mockAppConfig, httpClient, metrics)

  "Connector 'GET' one" should {
    "handle 404" in {
      when(mockAppConfig.apiToken).thenReturn(config.apiToken)
      val att = mock[Attachment]
      when(att.id).thenReturn(attachmentId)

      stubFor(
        get("/file/id")
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      await(connector.get(attachmentId)) shouldBe None

      WireMock.verify(
        getRequestedFor(urlEqualTo(s"/file/$attachmentId"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
      )
    }

    "handle response with mandatory fields only" in {
      val att = mock[Attachment]
      when(att.id).thenReturn(attachmentId)

      stubFor(
        get(s"/file/$attachmentId")
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(fromResource("filestore/single_file_with_mandatory_fields-response.json"))
          )
      )

      await(connector.get(attachmentId)) shouldBe Some(
        FileMetadata(
          id = attachmentId,
          fileName = Some("name"),
          mimeType = Some("text/plain"),
          url = None,
          scanStatus = None
        )
      )

      WireMock.verify(
        getRequestedFor(urlEqualTo(s"/file/$attachmentId"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
      )
    }

    "handle response with optional fields" in {
      val att = mock[Attachment]
      when(att.id).thenReturn(attachmentId)

      stubFor(
        get(s"/file/$attachmentId")
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(fromResource("filestore/single_file_with_optional_fields-response.json"))
          )
      )

      await(connector.get(attachmentId)) shouldBe Some(
        FileMetadata(
          id = attachmentId,
          fileName = Some("name"),
          mimeType = Some("text/plain"),
          url = Some("url"),
          scanStatus = Some(ScanStatus.READY)
        )
      )

      WireMock.verify(
        getRequestedFor(urlEqualTo(s"/file/$attachmentId"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
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
      when(att1.id).thenReturn("id1")
      val att2 = mock[Attachment]
      when(att2.id).thenReturn("id2")

      stubFor(
        get("/file?id=id1&id=id2")
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(fromResource("filestore/multi_file_with_mandatory_fields-response.json"))
          )
      )

      await(connector.get(Seq(att1, att2))) shouldBe Seq(
        FileMetadata(
          id = "id",
          fileName = Some("name"),
          mimeType = Some("text/plain"),
          url = None,
          scanStatus = None
        )
      )

      WireMock.verify(
        getRequestedFor(urlEqualTo("/file?id=id1&id=id2"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
      )
    }

    "handle response with optional fields" in {
      val att1 = mock[Attachment]
      when(att1.id).thenReturn("id1")
      val att2 = mock[Attachment]
      when(att2.id).thenReturn("id2")

      stubFor(
        get("/file?id=id1&id=id2")
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(fromResource("filestore/multi_file_with_optional_fields-response.json"))
          )
      )

      await(connector.get(Seq(att1, att2))) shouldBe Seq(
        FileMetadata(
          id = "id",
          fileName = Some("name"),
          mimeType = Some("text/plain"),
          url = Some("url"),
          scanStatus = Some(ScanStatus.READY)
        )
      )

      WireMock.verify(
        getRequestedFor(urlEqualTo("/file?id=id1&id=id2"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
      )
    }
  }

  "Initiate" in {
    stubFor(
      post("/file/initiate")
        .willReturn(
          aResponse()
            .withStatus(ACCEPTED)
            .withBody(fromResource("filestore/binding-tariff-filestore_initiate-response.json"))
        )
    )

    val initiateRequest = FileStoreInitiateRequest(maxFileSize = 0)

    await(connector.initiate(initiateRequest)) shouldBe FileStoreInitiateResponse(
      id = "id",
      upscanReference = "ref",
      uploadRequest = UpscanFormTemplate(
        "http://localhost:20001/upscan/upload",
        Map("key" -> "value")
      )
    )

    WireMock.verify(
      postRequestedFor(urlEqualTo("/file/initiate"))
        .withHeader("X-Api-Token", equalTo(config.apiToken))
    )
  }

  "Upload" in {
    stubFor(
      post("/file")
        .willReturn(
          aResponse()
            .withStatus(ACCEPTED)
            .withBody(fromResource("filestore/binding-tariff-filestore_upload-response.json"))
        )
    )

    val file   = FileUpload(SingletonTemporaryFileCreator.create("example-file.txt"), "file.txt", "text/plain")
    val result = await(connector.upload(file))

    WireMock.verify(
      postRequestedFor(urlEqualTo("/file"))
        .withHeader("X-Api-Token", equalTo(config.apiToken))
        .withRequestBody(containing("file"))
        .withRequestBody(containing("publish"))
    )

    result shouldBe FileMetadata(
      id = "id",
      fileName = Some("file-name.txt"),
      mimeType = Some("text/plain")
    )
  }

  "delete" should {
    "Delete from the File Store" in {
      stubFor(
        delete("/file/fileId")
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      await(connector.delete("fileId"))

      WireMock.verify(
        deleteRequestedFor(urlEqualTo("/file/fileId"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
      )
    }

    "propagate errors" in {
      stubFor(
        delete("/file/fileId")
          .willReturn(
            aResponse()
              .withStatus(BAD_GATEWAY)
          )
      )

      intercept[UpstreamErrorResponse] {
        await(connector.delete("fileId"))
      }

      WireMock.verify(
        deleteRequestedFor(urlEqualTo("/file/fileId"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
      )
    }
  }

  "Connector download" should {
    "handle missing file" in {
      stubFor(
        get("/digital-tariffs-local/id")
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val result = await(connector.downloadFile(wireMockUrl + "/digital-tariffs-local/id"))

      result shouldBe None
    }

    "handle error response" in {
      stubFor(
        get("/digital-tariffs-local/id")
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      assertThrows[Exception](await(connector.downloadFile(wireMockUrl + "/digital-tariffs-local/id")))
    }
  }

}
