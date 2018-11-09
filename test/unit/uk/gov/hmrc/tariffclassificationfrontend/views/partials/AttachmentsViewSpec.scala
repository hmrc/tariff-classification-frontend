/*
 * Copyright 2018 HM Revenue & Customs
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

import uk.gov.hmrc.tariffclassificationfrontend.models.Attachment
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
      val attachment = Attachment(application = true, public = true, "http://www.host.co.uk/name.png", "type", ZonedDateTime.now())

      // When
      val doc = view(attachments(Seq(attachment)))

      // Then
      doc should containElementWithID("attachment-list")
      doc.getElementsByTag("a") should haveSize(1)
      doc.getElementsByTag("a").first() should containText("name.png")
      doc.getElementsByTag("a").first() should haveAttribute("href", "http://www.host.co.uk/name.png")
    }

    "render list of attachments with unknown names" in {
      val attachment = Attachment(application = true, public = true, "http://www.host.co.uk", "type", ZonedDateTime.now())

      // When
      val doc = view(attachments(Seq(attachment)))

      // Then
      doc should containElementWithID("attachment-list")
      doc.getElementsByTag("a") should haveSize(1)
      doc.getElementsByTag("a").first() should containText("Unknown")
      doc.getElementsByTag("a").first() should haveAttribute("href", "http://www.host.co.uk")
    }
  }

}
