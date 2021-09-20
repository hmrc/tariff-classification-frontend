/*
 * Copyright 2021 HM Revenue & Customs
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

package views.forms.components

import org.scalatest.MustMatchers.convertToAnyMustWrapper
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import views.ViewMatchers._
import views.ViewSpec
import views.html.forms.components.{input_checkbox, input_text}

class InputCheckboxViewSpec extends ViewSpec {

  "Input Checkbox" should {
    case class FormData(text: String)
    val form = Form(
      mapping(
        "field" -> text
      )(FormData.apply)(FormData.unapply)
    )

    "Render" in {
      // When
      val doc = view(input_checkbox(form("field"), "Label"))

      // Then
      doc                         should containElementWithTag("input")
      doc                         should containElementWithID("field")
      doc.getElementById("field") should haveAttribute("type", "checkbox")
      doc.getElementById("field") should haveAttribute("name", "field")
      doc.getElementById("field") should haveAttribute("value", "true")
      doc.getElementById("field") shouldNot haveAttribute("onChange", "this.form.submit()")
    }

    "Render with Optional Fields" in {
      // When
      val doc = view(input_checkbox(form("field"), "Label", value = false, submitOnChange = true))

      // Then
      doc                         should containElementWithTag("input")
      doc                         should containElementWithID("field")
      doc.getElementById("field") should haveAttribute("type", "checkbox")
      doc.getElementById("field") should haveAttribute("name", "field")
      doc.getElementById("field") should haveAttribute("value", "false")
      doc.getElementById("field") should haveAttribute("onChange", "this.form.submit()")
    }
    "show an error in the value field's label" in {
      lazy val emptyForm = Map[String, String]()
      val formWithError  = form.bind(emptyForm).apply("field")
      val doc            = view(input_text(formWithError, "Span"))
      doc.getElementsByClass("govuk-visually-hidden").text() mustBe errorPrefix
    }
  }

}
