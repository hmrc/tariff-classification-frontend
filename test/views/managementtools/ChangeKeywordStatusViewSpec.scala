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

import models.forms.KeywordForm
import models.{Case, Keyword}
import utils.Cases
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.managementtools.change_keyword_status_view

class ChangeKeywordStatusViewSpec extends ViewSpec {

  private val keyword = Keyword("potatoes")
  val aCase: Case     = Cases.btiCaseExample.copy(keywords = Set("potatoes"))

  def changeKeywordStatusView: change_keyword_status_view = injector.instanceOf[change_keyword_status_view]

  "ChangeKeywordStatus view" should {

    "display the keyword in the header" in {
      val doc = view(changeKeywordStatusView(keyword.name, aCase, KeywordForm.form))
      doc                                        should containElementWithID("keyword-edit-heading")
      doc.getElementById("keyword-edit-heading") should containText("potatoes")

    }

    "display the case reference in the h2" in {
      val doc = view(changeKeywordStatusView(keyword.name, aCase, KeywordForm.form))
      doc should containElementWithID("keyword-case-details")
    }
  }

}
