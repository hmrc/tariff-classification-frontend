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

import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.text_with_line_feeds

class TextWithLineFeedsViewSpec extends ViewSpec {

  "Progressing Disclosure" should {

    "Newline characters are rendered as HTML breaks" in {
      // When
      val doc = view(text_with_line_feeds("MODULE", "First paragraph\nSecond paragraph\nThird paragraph"))

      // Then
      doc.getElementById("MODULE-body") should containHtml("First paragraph\n<br>Second paragraph\n<br>Third paragraph")
    }
  }

}
