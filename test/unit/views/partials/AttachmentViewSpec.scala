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

package views.partials

import models.response.ScanStatus
import utils.Cases
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.attachment

class AttachmentViewSpec extends ViewSpec {

  "Attachment" should {

    "Render Pending attachment" in {
      val stored = Cases.storedAttachment.copy(id = "FILE_ID", fileName = Some("name"), scanStatus = None)

      // When
      val doc = view(attachment("MODULE", stored))

      // Then
      doc                                      should containElementWithID("MODULE-file")
      doc                                      should containElementWithID("MODULE-file-status")
      doc.getElementById("MODULE-file")        should haveChild("span").containingText("name")
      doc.getElementById("MODULE-file-status") should containText("Processing")
    }

    "Render Quarantined attachment" in {
      val stored = Cases.storedAttachment.copy(id = "FILE_ID", fileName = Some("name"), scanStatus = Some(ScanStatus.FAILED))

      // When
      val doc = view(attachment("MODULE", stored))

      // Then
      doc                                      should containElementWithID("MODULE-file")
      doc                                      should containElementWithID("MODULE-file-status")
      doc.getElementById("MODULE-file")        should haveChild("span").containingText("name")
      doc.getElementById("MODULE-file-status") should containText("Failed")
    }

    "Render Safe attachment without URL" in {
      val stored =
        Cases.storedAttachment.copy(id = "FILE_ID", fileName = Some("name"), scanStatus = Some(ScanStatus.READY), url = None)

      // When
      val doc = view(attachment("MODULE", stored))

      // Then
      doc                                      should containElementWithID("MODULE-file")
      doc                                      should containElementWithID("MODULE-file-status")
      doc.getElementById("MODULE-file")        should haveChild("span").containingText("name")
      doc.getElementById("MODULE-file-status") should containText("Failed")
    }

    "Render Safe attachment" in {
      val stored = Cases.storedAttachment
        .copy(id = "FILE_ID", fileName = Some("name"), scanStatus = Some(ScanStatus.READY), url = Some("url"))

      // When
      val doc = view(attachment("MODULE", stored))

      // Then
      doc should containElementWithID("MODULE-file")
      doc shouldNot containElementWithID("MODULE-file-status")

      val anchor = doc.getElementById("MODULE-file")
      anchor should haveChild("a").containingText("name")
      anchor should haveChild("a").withAttribute("href", "url")
    }

  }

}
