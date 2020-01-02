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

package uk.gov.hmrc.tariffclassificationfrontend.views

import uk.gov.hmrc.tariffclassificationfrontend.models.response.{FileMetadata, ScanStatus}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._

class ViewAttachmentUnavailableViewSpec extends ViewSpec {

  "View Attachment Unavailable View" should {

    "render file not found" in {
      // Given
      val fileMetadata = None

      // When
      val doc = view(html.view_attachment_unavailable(fileMetadata)(request, messages, appConfig))

      // Then
      doc should containElementWithID("attachment-not_found")

      doc shouldNot containElementWithID("attachment-scan_failed")
      doc shouldNot containElementWithID("attachment-processing")
    }
  }

  "render scan failed" in {
    // Given
    val fileMetadata = Some(FileMetadata("id", "filename", "mimetype", None, Some(ScanStatus.FAILED)))

    // When
    val doc = view(html.view_attachment_unavailable(fileMetadata)(request, messages, appConfig))

    // Then
    doc should containElementWithID("attachment-scan_failed")

    doc shouldNot containElementWithID("attachment-not_found")
    doc shouldNot containElementWithID("attachment-processing")
  }

  "render still processing" in {
    // Given
    val fileMetadata = Some(FileMetadata("id", "filename", "mimetype", None, None))

    // When
    val doc = view(html.view_attachment_unavailable(fileMetadata)(request, messages, appConfig))

    // Then
    doc should containElementWithID("attachment-processing")

    doc shouldNot containElementWithID("attachment-scan_failed")
    doc shouldNot containElementWithID("attachment-not_found")
  }

}
