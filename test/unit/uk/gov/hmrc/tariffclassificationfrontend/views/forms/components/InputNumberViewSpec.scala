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

package uk.gov.hmrc.tariffclassificationfrontend.views.forms.components

import play.api.data.Form
import play.api.data.Forms.{mapping, number}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.forms.components.input_number

class InputNumberViewSpec extends ViewSpec {

  "Input Number" should {
    case class FormData(value: Int)
    val form = Form(
      mapping(
        "field" -> number
      )(FormData.apply)(FormData.unapply)
    ).fill(FormData(5))

    "Render" in {
      // When
      val doc = view(input_number(form("field"), "Label"))

      // Then
      doc should containElementWithTag("input")
      doc should containElementWithID("field")
      doc.getElementById("field") should haveAttribute("type", "number")
      doc.getElementById("field") should haveAttribute("name", "field")
      doc.getElementById("field") should haveAttribute("value", "5")
      doc.getElementById("field") shouldNot haveAttribute("minLength")
      doc.getElementById("field") shouldNot haveAttribute("maxLength")
    }

    "Render with Optional Fields" in {
      // When
      val doc = view(input_number(form("field"), "Label", maxLength = Some(10), minLength = Some(1)))

      // Then
      doc should containElementWithTag("input")
      doc should containElementWithID("field")
      doc.getElementById("field") should haveAttribute("type", "number")
      doc.getElementById("field") should haveAttribute("name", "field")
      doc.getElementById("field") should haveAttribute("value", "5")
      doc.getElementById("field") should haveAttribute("minLength", "1")
      doc.getElementById("field") should haveAttribute("maxLength", "10")
    }
  }

}
