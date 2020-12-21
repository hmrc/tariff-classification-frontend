/*
 * Copyright 2020 HM Revenue & Customs
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

package service

import java.io.File
import java.time.Instant

import connector.FileStoreConnector
import models._
import models.response.{FileMetadata, ScanStatus}
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import play.api.libs.Files.TemporaryFile
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import utils.Cases._

import scala.concurrent.Future
import scala.concurrent.Future.successful
import java.nio.file.Path
import akka.stream.scaladsl.Source
import akka.util.ByteString

class FileStoreServiceSpec extends ServiceSpecBase {

  private val connector = mock[FileStoreConnector]
  private val service   = new FileStoreService(connector)

  "Service 'getAttachments' by Cases" should {

    "Return Stored Attachments" in {

      val c1 = aCase(withReference("c1"), withAnAttachmentWithId("1"), withAnAttachmentWithId("2"))
      val c2 = aCase(withReference("c2"), withAnAttachmentWithId("3"))
      givenFileStoreReturnsAttachments(someMetadataWithId("1"), someMetadataWithId("2"), someMetadataWithId("3"))

      await(service.getAttachments(Seq(c1, c2))) shouldBe Map(
        c1 -> Seq(aStoredAttachmentWithId("1"), aStoredAttachmentWithId("2")),
        c2 -> Seq(aStoredAttachmentWithId("3"))
      )
    }

    "Filter missing Attachments" in {
      val c1 = aCase(withReference("c1"), withAnAttachmentWithId("1"), withAnAttachmentWithId("2"))
      val c2 = aCase(withReference("c2"), withAnAttachmentWithId("3"))
      givenFileStoreReturnsNoAttachments()

      await(service.getAttachments(Seq(c1, c2))) shouldBe Map(c1 -> Seq.empty, c2 -> Seq.empty)
    }

    "Ignore extra Attachments" in {
      val c = aCase()
      givenFileStoreReturnsAttachments(someMetadataWithId("1"))

      await(service.getAttachments(Seq(c))) shouldBe Map(c -> Seq.empty)
    }
  }

  "Service 'getAttachments' by Case" should {

    "Return Stored Attachments" in {
      val c = aCase(withAnAttachmentWithId("1"))
      givenFileStoreReturnsAttachments(someMetadataWithId("1"))

      await(service.getAttachments(c)) shouldBe Seq(aStoredAttachmentWithId("1"))
    }

    "Return no Stored Attachments" in {
      val c = aCase(withAnAttachmentWithId("1"), withAnAttachmentWithId("11"))
      givenFileStoreReturnsAttachments(someMetadataWithId("2"), someMetadataWithId("22"))

      await(service.getAttachments(c)) shouldBe Seq.empty
    }

    "Filter missing Attachments" in {
      val c = aCase(withAnAttachmentWithId("1"))
      givenFileStoreReturnsNoAttachments()

      await(service.getAttachments(c)) shouldBe Seq.empty
    }

    "Ignore extra Attachments" in {
      val c = aCase()
      givenFileStoreReturnsAttachments(someMetadataWithId("1"))

      await(service.getAttachments(c)) shouldBe Seq.empty
    }
  }

  "Service 'getLetterOfAuthority'" should {

    "Return Stored Attachment" in {
      val c = aCase(withLetterOfAuthWithId("1"))
      givenFileStoreReturnsAttachment(someMetadataWithId("1"))

      await(service.getLetterOfAuthority(c)) shouldBe Some(
        aStoredAttachmentWithId("1")
      )
    }

    "Return None for missing letter" in {
      await(service.getLetterOfAuthority(aCase(withAgentDetails()))) shouldBe None
    }

    "Return None for missing agent" in {
      await(service.getLetterOfAuthority(aCase(withoutAgentDetails()))) shouldBe None
    }

    "Return None for Non BTI applications" in {
      await(service.getLetterOfAuthority(Cases.liabilityCaseExample)) shouldBe None
    }
  }

  "Service 'upload'" should {

    "Return Stored Attachment" in {
      val upload  = mock[FileUpload]
      val content = mock[TemporaryFile]
      val path    = mock[Path]
      val file    = mock[File]
      given(upload.content) willReturn content
      given(content.path) willReturn path
      given(path.toFile) willReturn file
      given(file.length()) willReturn 1

      val metadata = FileMetadata(id = "id", fileName = "name", mimeType = "mimetype")
      given(connector.upload(upload)) willReturn Future.successful(metadata)

      await(service.upload(upload)) shouldBe FileStoreAttachment("id", "name", "mimetype", 1)
    }
  }

  "Service Get FileMetadata" should {
    val file = mock[FileMetadata]

    "Delegate to Connector" in {
      given(connector.get("id")) willReturn Future.successful(Some(file))
      await(service.getFileMetadata("id")) shouldBe Some(file)
    }
  }

  "Service 'removeAttachment'" should {

    "call the connector" in {
      val id   = "id"
      val file = mock[FileMetadata]
      given(connector.get(id)) willReturn Future.successful(Some(file))

      service.removeAttachment(id)

      Mockito.verify(connector).delete(id)
    }

  }

  "Service 'downloadFile'" should {
    val fileContent = Some(Source.single(ByteString("Some file content".getBytes())))

    "call the connector" in {
      given(connector.downloadFile(any[String])(any[HeaderCarrier])).willReturn(successful(fileContent))

      await(service.downloadFile("http://localhost:4572/foo")) shouldBe fileContent
    }
  }

  private def aStoredAttachmentWithId(id: String): StoredAttachment =
    StoredAttachment(
      id                     = id,
      public                 = true,
      operator               = None,
      url                    = Some(s"url-$id"),
      fileName               = s"name-$id",
      mimeType               = s"type-$id",
      scanStatus             = Some(ScanStatus.READY),
      timestamp              = Instant.EPOCH,
      description            = Some("test description"),
      shouldPublishToRulings = true
    )

  private def aCase(modifiers: (Case => Case)*): Case = {
    var c = Cases.btiCaseExample
    modifiers.foreach(m => c = m(c))
    c
  }

  private def withAnAttachmentWithId(id: String): Case => Case = c => {
    val attachments: Seq[Attachment] = c.attachments :+ Attachment(
      id     = id,
      public = true,
      None,
      timestamp              = Instant.EPOCH,
      description            = Some("test description"),
      shouldPublishToRulings = true
    )
    c.copy(attachments = attachments)
  }

  private def withLetterOfAuthWithId(id: String): Case => Case = c => {
    val details = AgentDetails(mock[EORIDetails], Some(anAttachmentWithId(id)))
    val app     = c.application.asATAR.copy(agent = Some(details))
    c.copy(application = app)
  }

  private def anAttachmentWithId(id: String): Attachment =
    Attachment(
      id     = id,
      public = true,
      None,
      timestamp              = Instant.EPOCH,
      description            = Some("test description"),
      shouldPublishToRulings = true
    )

  private def withAgentDetails(): Case => Case = c => {
    val details = AgentDetails(mock[EORIDetails], None)
    val app     = c.application.asATAR.copy(agent = Some(details))
    c.copy(application = app)
  }

  private def withoutAgentDetails(): Case => Case = c => {
    val app = c.application.asATAR.copy(agent = None)
    c.copy(application = app)
  }

  private def someMetadataWithId(id: String): FileMetadata =
    FileMetadata(
      id         = id,
      fileName   = s"name-$id",
      mimeType   = s"type-$id",
      url        = Some(s"url-$id"),
      scanStatus = Some(ScanStatus.READY)
    )

  private def givenFileStoreReturnsNoAttachments(): Unit = {
    given(connector.get(any[Seq[Attachment]])(any[HeaderCarrier])) willReturn successful(Seq.empty)
    given(connector.get(any[String])(any[HeaderCarrier])) willReturn successful(None)
  }

  private def givenFileStoreReturnsAttachments(attachments: FileMetadata*): Unit =
    given(connector.get(any[Seq[Attachment]])(any[HeaderCarrier])) willReturn successful(attachments)

  private def givenFileStoreReturnsAttachment(attachment: FileMetadata): Unit =
    given(connector.get(any[String])(any[HeaderCarrier])) willReturn successful(Some(attachment))

}
