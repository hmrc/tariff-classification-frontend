/*
 * Copyright 2024 HM Revenue & Customs
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

package views.managementtools

import models.Keyword
import models.forms.KeywordForm
import play.api.data.Form
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.new_keyword_view

class NewKeywordViewSpec extends ViewSpec {

  val keywords: Seq[Keyword] =
    Seq(Keyword("shoes", approved = true), Keyword("hats", approved = true), Keyword("shirts", approved = true))
  val keywordForm: Form[String] = KeywordForm.formWithAuto(keywords.map(_.name))

  def newKeywordView: new_keyword_view = injector.instanceOf[new_keyword_view]

  "NewKeywordView" should {

    "render successfully with the default tab" in {

      val doc = view(
        newKeywordView(
          allKeywords = keywords,
          newKeywordForm = keywordForm
        )
      )
      doc                                should containText(messages("management.create-keyword.caption"))
      doc.getElementsByTag("h1").first() should containText(messages("management.create-keyword.heading"))

    }

    "contain a label and hint text" in {

      val doc = view(
        newKeywordView(
          allKeywords = keywords,
          newKeywordForm = keywordForm
        )
      )
      doc                                should containText(messages("management.create-keyword.label"))
      doc.getElementById("keyword-hint") should containText(messages("management.create-keyword.hint"))
    }

    "render successfully with create new keyword input component" in {

      val doc = view(
        newKeywordView(
          allKeywords = keywords,
          newKeywordForm = keywordForm
        )
      )
      doc should containElementWithID("keyword-search-wrapper")
    }

    "render successfully with save keyword button" in {

      val doc = view(
        newKeywordView(
          allKeywords = keywords,
          newKeywordForm = keywordForm
        )
      )
      doc should containElementWithID("save_new_keyword-button")
      doc.getElementById("save_new_keyword-button") should containText(
        messages("management.create-keyword.save-button")
      )

    }

  }

}
