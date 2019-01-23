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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.{Instant, ZoneOffset, ZonedDateTime}

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.response.{FilestoreResponse, ScanStatus}

class StoredAttachmentTest extends UnitSpec {

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
      val metadata = someMetadataWithType("type")

      StoredAttachment(attachment, metadata) shouldBe StoredAttachment(
        id = attachment.id,
        public = attachment.public,
        operator = None,
        timestamp = attachment.timestamp,
        url = metadata.url,
        fileName = metadata.fileName,
        mimeType = metadata.mimeType,
        scanStatus = metadata.scanStatus
      )
    }
  }

  private def anImageOfType(t: String): StoredAttachment = {
    StoredAttachment(anAttachment, someMetadataWithType(t))
  }

  private def anAttachment = Attachment(id = "id", public = true, None,  timestamp = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC))

  private def someMetadataWithType(t: String = "text/plain") = FilestoreResponse(
    id = "id",
    fileName = "name",
    mimeType = t,
    url = Some("url"),
    scanStatus = Some(ScanStatus.READY),
    lastUpdated = Instant.EPOCH
  )

}
