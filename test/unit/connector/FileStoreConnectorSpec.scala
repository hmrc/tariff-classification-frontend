/*
 * Copyright 2021 HM Revenue & Customs
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
import config.AppConfig
import models.request.FileStoreInitiateRequest
import models.response._
import models.{Attachment, FileUpload}
import org.mockito.BDDMockito.given
import play.api.http.Status
import play.api.libs.Files.SingletonTemporaryFileCreator

class FileStoreConnectorSpec extends ConnectorTest {

  private val config = fakeApplication.injector.instanceOf[AppConfig]
  given(mockAppConfig.maxUriLength) willReturn 2048L
  given(mockAppConfig.fileStoreUrl) willReturn wireMockUrl
  given(mockAppConfig.apiToken).willReturn(config.apiToken)
  private val attachmentId = "id"
  private val connector    = new FileStoreConnector(mockAppConfig, authenticatedHttpClient, wsClient, metrics)

  "Connector 'GET' one" should {
    "handle 404" in {
      val att = mock[Attachment]
      given(att.id) willReturn attachmentId

      stubFor(
        get("/file/id")
          .willReturn(aResponse().withStatus(Status.NOT_FOUND))
      )

      await(connector.get(attachmentId)) shouldBe None

      verify(
        getRequestedFor(urlEqualTo(s"/file/$attachmentId"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
      )
    }

    "handle response with mandatory fields only" in {
      val att = mock[Attachment]
      given(att.id) willReturn attachmentId

      stubFor(
        get(s"/file/$attachmentId")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(fromResource("filestore/single_file_with_mandatory_fields-response.json"))
          )
      )

      await(connector.get(attachmentId)) shouldBe Some(
        FileMetadata(
          id         = attachmentId,
          fileName   = Some("name"),
          mimeType   = Some("text/plain"),
          url        = None,
          scanStatus = None
        )
      )

      verify(
        getRequestedFor(urlEqualTo(s"/file/$attachmentId"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
      )
    }

    "handle response with optional fields" in {
      val att = mock[Attachment]
      given(att.id) willReturn attachmentId

      stubFor(
        get(s"/file/$attachmentId")
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(fromResource("filestore/single_file_with_optional_fields-response.json"))
          )
      )

      await(connector.get(attachmentId)) shouldBe Some(
        FileMetadata(
          id         = attachmentId,
          fileName   = Some("name"),
          mimeType   = Some("text/plain"),
          url        = Some("url"),
          scanStatus = Some(ScanStatus.READY)
        )
      )

      verify(
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
          id         = "id",
          fileName   = Some("name"),
          mimeType   = Some("text/plain"),
          url        = None,
          scanStatus = None
        )
      )

      verify(
        getRequestedFor(urlEqualTo("/file?id=id1&id=id2"))
          .withHeader("X-Api-Token", equalTo(config.apiToken))
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
          id         = "id",
          fileName   = Some("name"),
          mimeType   = Some("text/plain"),
          url        = Some("url"),
          scanStatus = Some(ScanStatus.READY)
        )
      )

      verify(
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
            .withStatus(Status.ACCEPTED)
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

    verify(
      postRequestedFor(urlEqualTo("/file/initiate"))
        .withHeader("X-Api-Token", equalTo(config.apiToken))
    )
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

    val file   = FileUpload(SingletonTemporaryFileCreator.create("example-file.txt"), "file.txt", "text/plain")
    val result = await(connector.upload(file))

    verify(
      postRequestedFor(urlEqualTo("/file"))
        .withHeader("X-Api-Token", equalTo(config.apiToken))
        .withRequestBody(containing("file"))
        .withRequestBody(containing("publish"))
    )

    result shouldBe FileMetadata(
      id       = "id",
      fileName = Some("file-name.txt"),
      mimeType = Some("text/plain")
    )
  }

  "Delete" in {
    stubFor(
      delete("/file/fileId")
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
        )
    )

    await(connector.delete("fileId"))

    verify(
      deleteRequestedFor(urlEqualTo("/file/fileId"))
        .withHeader("X-Api-Token", equalTo(config.apiToken))
    )
  }

  "Connector download" should {
    "handle missing file" in {
      stubFor(
        get("/digital-tariffs-local/id")
          .willReturn(
            aResponse()
              .withStatus(Status.NOT_FOUND)
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
              .withStatus(Status.INTERNAL_SERVER_ERROR)
          )
      )

      assertThrows[Exception](await(connector.downloadFile(wireMockUrl + "/digital-tariffs-local/id")))
    }
  }

}
