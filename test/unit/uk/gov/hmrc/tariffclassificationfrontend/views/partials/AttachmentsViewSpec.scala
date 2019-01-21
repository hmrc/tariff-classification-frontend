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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import java.time.ZonedDateTime

import uk.gov.hmrc.tariffclassificationfrontend.models.{Operator, StoredAttachment}
import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.attachments

class AttachmentsViewSpec extends ViewSpec {

  "Attachments View" should {

    "render empty list of attachments" in {

      // When
      val doc = view(attachments(Seq.empty))

      // Then
      doc should containElementWithID("attachment-list")
      doc should containText("None")
    }

    "render list of attachments" in {
      val attachment = StoredAttachment(
        id = "id",
        public = true,
        operator = None,
        timestamp = ZonedDateTime.now(),
        url = Some("url"),
        fileName = "name",
        mimeType = "type",
        scanStatus = Some(ScanStatus.READY)
      )

      // When
      val doc = view(attachments(Seq(attachment)))

      // Then
      doc should containElementWithID("attachment-list")
      doc should containElementWithID("file-id")
      doc.getElementById("file-id") should containText("name")
      doc.getElementById("file-id") should haveAttribute("href", "url")
    }

    "render list of attachments without URL" in {
      val attachment = StoredAttachment(
        id = "id",
        public = true,
        operator = None,
        timestamp = ZonedDateTime.now(),
        url = None,
        fileName = "name",
        mimeType = "type",
        scanStatus = Some(ScanStatus.READY)
      )

      // When
      val doc = view(attachments(Seq(attachment)))

      // Then
      doc should containElementWithID("attachment-list")
      doc should containElementWithID("file-id")
      doc.getElementById("file-id") should containText("name")
      doc.getElementById("file-id") shouldNot haveAttribute("href", "url")
    }
  }

}
