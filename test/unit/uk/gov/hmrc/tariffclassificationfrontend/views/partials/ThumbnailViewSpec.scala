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

import java.time.Instant

import uk.gov.hmrc.tariffclassificationfrontend.models.StoredAttachment
import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.thumbnail

class ThumbnailViewSpec extends ViewSpec  {


  "Thumbnail" should {

    "Render thumbnail for attachment" in {
      // Given
      val storedAttachment = StoredAttachment(
        "id",
        public = true,
        operator = None,
        url = Some("some/file/url"),
        fileName = "the-file-name.png",
        mimeType = "image/png",
        scanStatus = Some(ScanStatus.READY),
        timestamp = Instant.now()
      )

      // When
      val doc = view(thumbnail("some-id", "some-case-reference", storedAttachment))

      // Then
      doc should containElementWithID("some-id")
      doc.getElementById("some-id") should haveTag("img")
      doc.getElementById("some-id") should haveAttribute("src", "some/file/url")
      doc.getElementById("some-id") should haveAttribute("alt", "Image the-file-name.png for case some-case-reference")
      doc.getElementById("some-id") should haveAttribute("title", "the-file-name.png")

      doc should containElementWithID("some-id-link")
      doc.getElementById("some-id-link") should haveAttribute("href", "some/file/url")
      doc.getElementById("some-id-link") should haveAttribute("target", "_blank")
      doc.getElementById("some-id-link") should haveAttribute("aria-label", "View case some-case-reference")

    }
  }
}
