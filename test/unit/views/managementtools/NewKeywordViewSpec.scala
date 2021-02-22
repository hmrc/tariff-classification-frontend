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

package views.managementtools

import models.Keyword
import models.forms.KeywordForm
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.new_keyword_view

class NewKeywordViewSpec extends ViewSpec {

  val keywords    = Seq(Keyword("shoes", true), Keyword("hats", true), Keyword("shirts", true))
  val keywordForm = KeywordForm.formWithAuto(keywords.map(_.name))

  def newKeywordView: new_keyword_view = injector.instanceOf[new_keyword_view]

  "NewKeywordView" should {

    "render successfully with the default tab" in {

      val doc = view(
        newKeywordView(
          allKeywords    = keywords,
          newKeywordForm = keywordForm
        )
      )
      doc                                        should containText(messages("management.create-keyword.caption"))
      doc                                        should containElementWithID("common-cases-heading")
      doc.getElementById("common-cases-heading") should containText(messages("management.create-keyword.heading"))
    }
  }

  /* "contains appropriate queue tabs for teams managed by the manager" in {
    val queues = Queues.allQueues
    val doc = view(
      manageUsersView(
        ManagerToolsUsersTab,
        UsersTabViewModel.forManagedTeams(queues)
      )
    )

    queues.foreach(q => doc should containElementWithID(q.slug.toUpperCase + "-tab"))
  }

  "contain a heading" in {
    val doc = view(
      manageUsersView(
        ManagerToolsUsersTab,
        UsersTabViewModel.forManagedTeams(Seq(Queues.cap).toList)
      )
    )
    doc should containElementWithID("common-cases-heading")
  }*/

}
