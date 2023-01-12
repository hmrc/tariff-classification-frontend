/*
 * Copyright 2023 HM Revenue & Customs
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

package models

import java.time.Instant

import models.response.{FileMetadata, ScanStatus}

class StoredAttachmentTest extends ModelsBaseSpec {

  "'Is Image'" should {

    "recognise PNG" in {
      anImageOfType("image/png").isImage shouldBe true
    }

    "recognise JPEG" in {
      anImageOfType("image/jpeg").isImage shouldBe true
    }

    "recognise GIF" in {
      anImageOfType("image/gif").isImage shouldBe true
    }

    "not recognise other types" in {
      anImageOfType("other").isImage shouldBe false
    }
  }

  "'apply'" should {
    "Combine Attachment with Metadata" in {
      val attachment = anAttachment
      val metadata   = someMetadataWithType("type")

      StoredAttachment(attachment, metadata) shouldBe StoredAttachment(
        id                     = attachment.id,
        public                 = attachment.public,
        operator               = None,
        timestamp              = attachment.timestamp,
        url                    = metadata.url,
        fileName               = metadata.fileName,
        mimeType               = metadata.mimeType,
        scanStatus             = metadata.scanStatus,
        description            = Some("test description"),
        shouldPublishToRulings = attachment.shouldPublishToRulings
      )
    }

    "fail when try to combine with different attachments" in {
      val attachment = anAttachment
      val metadata   = someMetadataWithType("type").copy(id = "another-id")

      val caught = intercept[IllegalArgumentException] {
        StoredAttachment(attachment, metadata)
      }
      caught.getMessage shouldBe "requirement failed: Cannot combine different attachments"
    }
  }

  private def anImageOfType(t: String): StoredAttachment =
    StoredAttachment(anAttachment, someMetadataWithType(t))

  private def anAttachment = Attachment(
    id     = "id",
    public = true,
    None,
    timestamp   = Instant.EPOCH,
    description = Some("test description")
  )

  private def someMetadataWithType(t: String) = FileMetadata(
    id         = "id",
    fileName   = Some("name"),
    mimeType   = Some(t),
    url        = Some("url"),
    scanStatus = Some(ScanStatus.READY)
  )

}
