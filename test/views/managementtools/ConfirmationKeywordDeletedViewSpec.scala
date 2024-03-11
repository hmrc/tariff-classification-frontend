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

import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.confirmation_keyword_deleted

class ConfirmationKeywordDeletedViewSpec extends ViewSpec {

  def confirmKeywordDeletedView: confirmation_keyword_deleted = injector.instanceOf[confirmation_keyword_deleted]

  "ConfirmationKeywordDeletedView" should {

    "render successfully with the default tab" in {

      val doc = view(
        confirmKeywordDeletedView(
          )
      )
      doc should containText(messages("management.manage-keywords.edit-approved-keywords.deleted"))
      doc should containElementWithID("confirm_keyword_deleted")
      doc should containText(
        messages("management.manage-keywords.edit-approved-keywords.deleted.p")
      )
    }

    "contain a link to redirect to manage all keywords page" in {

      val doc = view(
        confirmKeywordDeletedView(
          )
      )
      doc should containText(messages("management.create-keyword.new-keyword-done.next"))
      doc.getElementById("manage-all-keywords-link") should containText(
        messages("management.create-keyword.new-keyword-done.link")
      )
    }
  }

}
