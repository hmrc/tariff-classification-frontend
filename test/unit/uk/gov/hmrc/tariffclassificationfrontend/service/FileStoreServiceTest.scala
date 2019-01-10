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

package uk.gov.hmrc.tariffclassificationfrontend.service

import java.time.{Instant, ZoneOffset, ZonedDateTime}

import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.connector.FileStoreConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.response.{FileMetadata, ScanStatus}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future.successful

class FileStoreServiceTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val connector = mock[FileStoreConnector]
  private val service = new FileStoreService(connector)

  "Service 'getAttachments'" should {

    "Return Stored Attachments" in {
      val c = aCase(withAnAttachmentWithId("1"))
      givenFileStoreReturnsAttachments(someMetadataWithId("1"))

      await(service.getAttachments(c)) shouldBe Seq(
        aStoredAttachmentWithId("1")
      )
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
  }

  private def aStoredAttachmentWithId(id: String): StoredAttachment = {
    StoredAttachment(
      id = id,
      application = true,
      public = true,
      url = Some(s"url-$id"),
      fileName = s"name-$id",
      mimeType = s"type-$id",
      scanStatus = Some(ScanStatus.READY),
      timestamp = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)
    )
  }

  private def anAttachmentWithId(id: String): Attachment = {
    Attachment(
      id = id,
      application = true,
      public = true,
      timestamp = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)
    )
  }

  private def aCase(modifiers: (Case => Case)*): Case = {
    var c = Cases.btiCaseExample
    modifiers.foreach(m => c = m(c))
    c
  }

  private def withAnAttachmentWithId(id: String): Case => Case = c => {
    val attachments: Seq[Attachment] = c.attachments :+ Attachment(
      id = id,
      application = true,
      public = true,
      timestamp = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)
    )
    c.copy(attachments = attachments)
  }

  private def withLetterOfAuthWithId(id: String): Case => Case = c => {
    val details = AgentDetails(mock[EORIDetails], anAttachmentWithId(id))
    val app = c.application.asBTI.copy(agent = Some(details))
    c.copy(application = app)
  }

  private def someMetadataWithId(id: String): FileMetadata = {
    FileMetadata(
      id = id,
      fileName = s"name-$id",
      mimeType = s"type-$id",
      url = Some(s"url-$id"),
      scanStatus = Some(ScanStatus.READY),
      lastUpdated = Instant.EPOCH
    )
  }

  private def givenFileStoreReturnsNoAttachments(): Unit = {
    given(connector.get(any[Seq[Attachment]])(any[HeaderCarrier])) willReturn successful(Seq.empty)
    given(connector.get(any[Attachment])(any[HeaderCarrier])) willReturn successful(None)
  }

  private def givenFileStoreReturnsAttachments(attachments: FileMetadata*): Unit = {
    given(connector.get(any[Seq[Attachment]])(any[HeaderCarrier])) willReturn successful(attachments)
  }

  private def givenFileStoreReturnsAttachment(attachment: FileMetadata): Unit = {
    given(connector.get(any[Attachment])(any[HeaderCarrier])) willReturn successful(Some(attachment))
  }

}
