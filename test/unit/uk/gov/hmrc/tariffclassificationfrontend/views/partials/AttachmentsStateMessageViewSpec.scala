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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.attachments_state_message
import uk.gov.tariffclassificationfrontend.utils.Cases

class AttachmentsStateMessageViewSpec extends ViewSpec {

  "Attachments State Message" should {

    "Render Nothing given no attachments" in {
      // When
      val doc = view(attachments_state_message(Seq.empty))

      // Then
      doc shouldNot containElementWithID("attachment-state-message")
    }

    "Render Nothing for unprocessed attachments" in {
      // Given
      val attachment = Cases.storedAttachment.copy(scanStatus = None)

      // When
      val doc = view(attachments_state_message(Seq(attachment)))

      // Then
      doc shouldNot containElementWithID("attachment-state-message")
    }

    "Render Nothing for processed attachments" in {
      // Given
      val attachment = Cases.storedAttachment.copy(scanStatus = Some(ScanStatus.READY))

      // When
      val doc = view(attachments_state_message(Seq(attachment)))

      // Then
      doc shouldNot containElementWithID("attachment-state-message")
    }

    "Render message for failed attachments" in {
      // Given
      val attachment = Cases.storedAttachment.copy(scanStatus = Some(ScanStatus.FAILED))

      // When
      val doc = view(attachments_state_message(Seq(attachment)))

      // Then
      doc should containElementWithID("attachment-state-message")
    }

    "Allow auto-refresh for processing files" in {
      // Given
      val attachment = Cases.storedAttachment.copy(scanStatus = None)

      // When
      val doc = view(attachments_state_message(Seq(attachment), autoRefresh = true))

      // Then
      doc should containElementWithID("auto-refresh")
    }

    "Not auto-refresh for processing files when disabled" in {
      // Given
      val attachment = Cases.storedAttachment.copy(scanStatus = None)

      // When
      val doc = view(attachments_state_message(Seq(attachment)))

      // Then
      doc shouldNot containElementWithID("auto-refresh")
    }

    "Not auto-refresh for no files" in {
      // When
      val doc = view(attachments_state_message(Seq.empty, autoRefresh = true))

      // Then
      doc shouldNot containElementWithID("auto-refresh")
    }

    "Not auto-refresh for failed files" in {
      // Given
      val attachment = Cases.storedAttachment.copy(scanStatus = Some(ScanStatus.FAILED))

      // When
      val doc = view(attachments_state_message(Seq(attachment), autoRefresh = true))

      // Then
      doc shouldNot containElementWithID("auto-refresh")
    }

    "Not auto-refresh for ready files" in {
      // Given
      val attachment = Cases.storedAttachment.copy(scanStatus = Some(ScanStatus.READY))

      // When
      val doc = view(attachments_state_message(Seq(attachment), autoRefresh = true))

      // Then
      doc shouldNot containElementWithID("auto-refresh")
    }
  }

}
