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

package views.partials

import models.response.ScanStatus
import utils.Cases
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.attachments_state_message

class AttachmentsStateMessageViewSpec extends ViewSpec {

  "Attachments State Message" should {

    "Render Nothing given no attachments" in {

      val doc = view(attachments_state_message(Seq.empty))

      doc shouldNot containElementWithID("attachment-state-message")
    }

    "Render Nothing for unprocessed attachments" in {

      val attachment = Cases.storedAttachment.copy(scanStatus = None)

      val doc = view(attachments_state_message.render(Seq(attachment), false, true, authenticatedManagerFakeRequest))

      doc shouldNot containElementWithID("attachment-state-message")
    }

    "Render Nothing for processed attachments" in {

      val attachment = Cases.storedAttachment.copy(scanStatus = Some(ScanStatus.READY))

      val doc = view(attachments_state_message.ref.f(Seq(attachment), false, true)(authenticatedManagerFakeRequest))

      doc shouldNot containElementWithID("attachment-state-message")
    }

    "Render message for failed attachments" in {

      val attachment = Cases.storedAttachment.copy(scanStatus = Some(ScanStatus.FAILED))

      val doc = view(attachments_state_message(Seq(attachment)))

      doc should containElementWithID("attachment-state-message")
    }

    "Allow auto-refresh for processing files" in {

      val attachment = Cases.storedAttachment.copy(scanStatus = None)

      val doc = view(attachments_state_message(Seq(attachment), autoRefresh = true))

      doc should containElementWithID("auto-refresh")
    }

    "Not auto-refresh for processing files when disabled" in {

      val attachment = Cases.storedAttachment.copy(scanStatus = None)

      val doc = view(attachments_state_message(Seq(attachment)))

      doc shouldNot containElementWithID("auto-refresh")
    }

    "Not auto-refresh for no files" in {

      val doc = view(attachments_state_message(Seq.empty, autoRefresh = true))

      doc shouldNot containElementWithID("auto-refresh")
    }

    "Not auto-refresh for failed files" in {

      val attachment = Cases.storedAttachment.copy(scanStatus = Some(ScanStatus.FAILED))

      val doc = view(attachments_state_message(Seq(attachment), autoRefresh = true))

      doc shouldNot containElementWithID("auto-refresh")
    }

    "Not auto-refresh for ready files" in {

      val attachment = Cases.storedAttachment.copy(scanStatus = Some(ScanStatus.READY))

      val doc = view(attachments_state_message(Seq(attachment), autoRefresh = true))

      doc shouldNot containElementWithID("auto-refresh")
    }
  }

}
