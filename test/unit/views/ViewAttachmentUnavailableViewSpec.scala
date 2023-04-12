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

package views

import models.Operator
import models.request.AuthenticatedRequest
import models.response.{FileMetadata, ScanStatus}
import views.ViewMatchers._
import views.html.view_attachment_unavailable

class ViewAttachmentUnavailableViewSpec extends ViewSpec {

  val viewAttachmentUnavailableView: view_attachment_unavailable = app.injector.instanceOf[view_attachment_unavailable]

  "View Attachment Unavailable View" should {

    "render file not found" in {

      val fileMetadata = None


      val doc = view(
        viewAttachmentUnavailableView(fileMetadata)(
          AuthenticatedRequest(Operator("0", Some("name")), request),
          messages,
          appConfig
        )
      )


      doc should containElementWithID("attachment-not_found")

      doc shouldNot containElementWithID("attachment-scan_failed")
      doc shouldNot containElementWithID("attachment-processing")
    }
  }

  "render scan failed" in {

    val fileMetadata = Some(FileMetadata("id", Some("filename"), Some("mimetype"), None, Some(ScanStatus.FAILED)))


    val doc = view(
      viewAttachmentUnavailableView(fileMetadata)(
        AuthenticatedRequest(Operator("0", Some("name")), request),
        messages,
        appConfig
      )
    )


    doc should containElementWithID("attachment-scan_failed")

    doc shouldNot containElementWithID("attachment-not_found")
    doc shouldNot containElementWithID("attachment-processing")
  }

  "render still processing" in {

    val fileMetadata = Some(FileMetadata("id", Some("filename"), Some("mimetype"), None, None))


    val doc = view(
      viewAttachmentUnavailableView(fileMetadata)(
        AuthenticatedRequest(Operator("0", Some("name")), request),
        messages,
        appConfig
      )
    )


    doc should containElementWithID("attachment-processing")

    doc shouldNot containElementWithID("attachment-scan_failed")
    doc shouldNot containElementWithID("attachment-not_found")
  }

}
