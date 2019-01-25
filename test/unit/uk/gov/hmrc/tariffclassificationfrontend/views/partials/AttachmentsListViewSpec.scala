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

import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.attachments_list
import uk.gov.tariffclassificationfrontend.utils.Cases

class AttachmentsListViewSpec extends ViewSpec {

  "Attachments List" should {

    "Render Nothing given no attachments" in {
      // When
      val doc = view(attachments_list("MODULE", Seq.empty))

      // Then
      doc shouldNot containElementWithID("MODULE-table")
      doc should containElementWithID("MODULE-empty-table")
    }

    "Render List of Pending attachments" in {
      val attachment = Cases.storedAttachment.copy(id = "FILE_ID", fileName = "name", scanStatus = None)

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment)))

      // Then
      doc should containElementWithID("MODULE-table")
      doc should containElementWithID("MODULE-file-FILE_ID")
      doc should containElementWithID("MODULE-file-FILE_ID-status")
      doc.getElementById("MODULE-file-FILE_ID") should haveChild("span").containingText("name")
      doc.getElementById("MODULE-file-FILE_ID-status") should containText("Processing")
    }

    "Render List of Quarantined attachments" in {
      val attachment = Cases.storedAttachment.copy(id = "FILE_ID", fileName = "name", scanStatus = Some(ScanStatus.FAILED))

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment)))

      // Then
      doc should containElementWithID("MODULE-table")
      doc should containElementWithID("MODULE-file-FILE_ID")
      doc should containElementWithID("MODULE-file-FILE_ID-status")
      doc.getElementById("MODULE-file-FILE_ID") should haveChild("span").containingText("name")
      doc.getElementById("MODULE-file-FILE_ID-status") should containText("Failed")
    }

    "Render List of Safe attachments without URL" in {
      val attachment = Cases.storedAttachment.copy(id = "FILE_ID", fileName = "name", scanStatus = Some(ScanStatus.READY), url = None)

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment)))

      // Then
      doc should containElementWithID("MODULE-table")
      doc should containElementWithID("MODULE-file-FILE_ID")
      doc should containElementWithID("MODULE-file-FILE_ID-status")
      doc.getElementById("MODULE-file-FILE_ID") should haveChild("span").containingText("name")
      doc.getElementById("MODULE-file-FILE_ID-status") should containText("Failed")
    }

    "Render List of Safe attachments" in {
      val attachment = Cases.storedAttachment.copy(id = "FILE_ID", fileName = "name", scanStatus = Some(ScanStatus.READY), url = Some("url"))

      // When
      val doc = view(attachments_list("MODULE", Seq(attachment)))

      // Then
      doc should containElementWithID("MODULE-table")
      doc should containElementWithID("MODULE-file-FILE_ID")
      doc shouldNot containElementWithID("MODULE-file-FILE_ID-status")

      val anchor = doc.getElementById("MODULE-file-FILE_ID")
      anchor should haveChild("a").containingText("name")
      anchor should haveChild("a").withAttribute("href", "url")
    }

  }

}
