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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.ZonedDateTime

import uk.gov.hmrc.play.test.UnitSpec

class AttachmentSpec extends UnitSpec {

  "Attachment 'Is Image'" should {

    "recognise PNG" in {
      anImageOfType("image/png").isImage shouldBe true
    }

    "recognise JPEG" in {
      anImageOfType("image/jpeg").isImage shouldBe true
    }

    "recognise GIF" in {
      anImageOfType("image/gif").isImage shouldBe true
    }

    "not recognise other types" in {
      anImageOfType("other").isImage shouldBe false
    }
  }

  "Attachment 'name'" should {

    "parse valid URL with filename" in {
      anAttachmentWithURL("http://host.com/path/image.png").name shouldBe Some("image.png")
    }

    "parse invalid URL" in {
      anAttachmentWithURL("abc").name shouldBe None
    }

    "parse valid URL without extension" in {
      anAttachmentWithURL("http://host.com/path/image").name shouldBe Some("image")
    }
  }

  private def anImageOfType(t: String) = {
    Attachment(application = false, public = false, "url", t, ZonedDateTime.now())
  }

  private def anAttachmentWithURL(url: String) = {
    Attachment(application = false, public = false, url, "type", ZonedDateTime.now())
  }

}
