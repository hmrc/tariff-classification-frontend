/*
 * Copyright 2024 HM Revenue & Customs
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
import views.html.partials.attachments_edit_list

import java.time.{ZoneOffset, ZonedDateTime}

class AttachmentsEditListViewSpec extends ViewSpec {

  private val caseRef: String = "600000004"

  "Attachments Edit List" should {

    "Render Nothing given no attachments" in {

      val doc = view(attachments_edit_list("MODULE", Seq.empty, caseRef = caseRef))

      doc shouldNot containElementWithID("MODULE-div")
      doc should containElementWithID("MODULE-empty-div")
    }

    "Render attachments" in {
      val attachment = Cases.storedAttachment.copy(
        id         = "FILE_ID",
        fileName   = Some("name"),
        url        = Some("url"),
        scanStatus = Some(ScanStatus.READY),
        timestamp  = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant
      )

      val doc = view(attachments_edit_list("MODULE", Seq(attachment), caseRef = caseRef))

      doc                                      should containElementWithID("MODULE-div")
      doc                                      should containElementWithID("MODULE-row-0")
      doc                                      should containElementWithID("MODULE-row-0-title")
      doc                                      should containElementWithID("MODULE-row-0-date")
      doc.getElementById("MODULE-row-0-title") should containText("name")
      doc.getElementById("MODULE-row-0-date")  should containText("01 Jan 2019")
    }

  }
}
