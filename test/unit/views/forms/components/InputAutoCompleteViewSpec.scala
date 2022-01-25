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

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import views.ViewMatchers._
import views.ViewSpec
import views.html.components.input_accessible_auto_complete

class InputAutoCompleteViewSpec extends ViewSpec {

  "Input Auto Complete" should {
    case class FormData(value: String)
    val form = Form(
      mapping(
        "field" -> text
      )(FormData.apply)(FormData.unapply)
    ).fill(FormData("v"))

    "Render" in {
      // When
      val doc = view(
        input_accessible_auto_complete(
          field               = form("field"),
          label               = Some("Label"),
          autoCompleteOptions = Seq("A", "B"),
          useTabIndex         = Some(100)
        )
      )

      // Then
      doc should containElementWithID("keyword-search-wrapper")
    }
  }

}
