/*
 * Copyright 2023 HM Revenue & Customs
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

import models.viewmodels.ManagerToolsKeywordsTab
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.confirm_keyword_created

class ConfirmKeywordCreatedViewSpec extends ViewSpec {

  val keyword = "keyword"

  def confirmKeywordCreatedView: confirm_keyword_created = injector.instanceOf[confirm_keyword_created]

  "ConfirmKeywordCreatedView" should {

    "render successfully with the default tab" in {

      val doc = view(
        confirmKeywordCreatedView(
          keyword
        )
      )
      doc                                should containText(messages("management.create-keyword.new-keyword-done"))
      doc.getElementById("main-content") should containText(keyword.toUpperCase)
      doc.getElementById("main-content") should containText(
        messages("management.create-keyword.new-keyword-done.caption")
      )
    }

    "contain a link to redirect to manage all keywords page" in {

      val doc = view(
        confirmKeywordCreatedView(
          keyword
        )
      )
      doc should containText(messages("management.create-keyword.new-keyword-done.next"))
      doc.getElementById("manage-all-keywords-link") should containText(
        messages("management.create-keyword.new-keyword-done.link")
      )
    }
  }

}
