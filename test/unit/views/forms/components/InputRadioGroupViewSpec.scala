/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import views.ViewMatchers._
import views.ViewSpec
import views.html.forms.components.{input_radiogroup, input_text}

class InputRadioGroupViewSpec extends ViewSpec {

  "Input Radio Group" should {
    case class FormData(value: String)
    val form = Form(
      mapping(
        "field" -> text
      )(FormData.apply)(FormData.unapply)
    )

    "Render 'None'" in {
      // When
      val doc = view(input_radiogroup(id = "ID", field = form("field"), options = Seq.empty, allowNone = true))

      // Then
      doc                           should containElementWithID("ID-none")
      doc.getElementById("ID-none") should haveAttribute("value", "")
      doc.getElementById("ID-none") should haveAttribute("name", "field")

      doc                                 should containElementWithID("ID-none-label")
      doc.getElementById("ID-none-label") should containText("None")
    }

    "Render 'None' - Preselected when Field is Empty" in {
      // When
      val doc = view(input_radiogroup(id = "ID", field = form("field"), options = Seq.empty, allowNone = true))

      // Then
      doc                           should containElementWithID("ID-none")
      doc.getElementById("ID-none") should haveAttribute("checked", "checked")
    }

    "Render 'None' - Preselected when Field is Blank" in {
      // Given
      val filledForm = form.fill(FormData(""))

      // When
      val doc = view(input_radiogroup(id = "ID", field = filledForm("field"), options = Seq.empty, allowNone = true))

      // Then
      doc                           should containElementWithID("ID-none")
      doc.getElementById("ID-none") should haveAttribute("checked", "checked")
    }

    "Render 'None' - Not Preselected when Field is Blank but preselect is set to false" in {
      // Given
      val filledForm = form.fill(FormData(""))

      // When
      val doc = view(
        input_radiogroup(
          id        = "ID",
          field     = filledForm("field"),
          options   = Seq.empty,
          allowNone = true,
          preSelect = false
        )
      )

      // Then
      doc should containElementWithID("ID-none")
      doc.getElementById("ID-none") shouldNot haveAttribute("checked", "checked")
    }

    "Render 'None' - Unselected when Field is Set" in {
      // Given
      val filledForm = form.fill(FormData("value"))

      // When
      val doc = view(input_radiogroup(id = "ID", field = filledForm("field"), options = Seq.empty, allowNone = true))

      // Then
      doc should containElementWithID("ID-none")
      doc.getElementById("ID-none") shouldNot haveAttribute("checked")
    }

    "Render Option" in {
      // When
      val doc = view(
        input_radiogroup(
          id        = "ID",
          field     = form("field"),
          options   = Seq(RadioOption("VALUE", "LABEL")),
          allowNone = true
        )
      )

      // Then
      doc                            should containElementWithID("ID-VALUE")
      doc.getElementById("ID-VALUE") should haveAttribute("value", "VALUE")

      doc                                  should containElementWithID("ID-VALUE-label")
      doc.getElementById("ID-VALUE-label") should containText("LABEL")
    }

    "Render Option - Unselected when Field is Empty" in {
      // When
      val doc = view(input_radiogroup(id = "ID", field = form("field"), options = Seq(RadioOption("VALUE", "LABEL"))))

      // Then
      doc should containElementWithID("ID-VALUE")
      doc.getElementById("ID-VALUE") shouldNot haveAttribute("checked")
    }

    "Render Option - Preselected when Field is Set" in {
      // Given
      val filledForm = form.fill(FormData("VALUE"))

      // When
      val doc =
        view(input_radiogroup(id = "ID", field = filledForm("field"), options = Seq(RadioOption("VALUE", "LABEL"))))

      // Then
      doc                            should containElementWithID("ID-VALUE")
      doc.getElementById("ID-VALUE") should haveAttribute("checked", "checked")
    }
    "enable an error prefix for screen reader" in {
      lazy val emptyForm = Map[String, String]()
      val formWithError  = form.bind(emptyForm).apply("field")
      val doc            = view(input_text(formWithError, "Span"))
      doc.getElementsByClass("govuk-visually-hidden").text() mustBe errorPrefix
    }
  }

}
