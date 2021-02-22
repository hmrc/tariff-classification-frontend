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

package views.partials

import models.{Keyword, Permission}
import models.forms.{KeywordForm, ManageKeywordForm}
import models.request.AuthenticatedRequest
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.keywords_details
import models.viewmodels.KeywordsTabViewModel
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeHeaders, FakeRequest}
import utils.Notification._
import play.api.test.CSRFTokenHelper._

class KeywordDetailsViewSpec extends ViewSpec {

  val keywords    = Seq(Keyword("shoes", true), Keyword("hats", true), Keyword("shirts", true))
  val form = KeywordForm.formWithAuto(keywords.map(_.name))

  "Keyword Details" should {

    "Render a case with no keywords" in {
      // Given
      val c           = aCase()
      val keywordsTab = KeywordsTabViewModel.fromCase(c, Seq("APPLES", "TOYS"))

      // When
      val doc = view(keywords_details(keywordsTab, form))

      // Then
      doc should containElementWithID("keywords-heading")
      doc shouldNot containElementWithID("keywords-row-0-keyword")
    }

    "Render a case with keywords" in {
      // Given
      val c           = aCase().copy(keywords = Set("APPLES", "CARS"))
      val keywordsTab = KeywordsTabViewModel.fromCase(c, Seq("APPLES", "TOYS"))

      // When
      val doc = view(keywords_details(keywordsTab, form))

      // Then
      doc                                          should containElementWithID("keywords-heading")
      doc                                          should containElementWithID("keywords-row-0-keyword")
      doc.getElementById("keywords-row-0-message") should containText("")
      doc.getElementById("keywords-row-1-message") should containText("Keyword is not from the list")
    }

    "Render a case with keywords with KEYWORDS permissions" in {
      // Given
      val c           = aCase().copy(keywords = Set("APPLES", "CARS"))
      val keywordsTab = KeywordsTabViewModel.fromCase(c, Seq("APPLES", "TOYS"))

      // When
      val doc = view(
        keywords_details(keywordsTab, form)(
          requestWithPermissions(Permission.KEYWORDS),
          messages,
          appConfig
        )
      )

      // Then
      doc                                          should containElementWithID("keywords-heading")
      doc                                          should containElementWithID("keywords-row-0-keyword")
      doc.getElementById("keywords-row-0-message") should containText("")
      doc.getElementById("keywords-row-1-message") should containText("Keyword is not from the list")

      doc should containElementWithID("keywords-row-0-remove")
      doc should containElementWithID("keyword_details-add_keyword")
    }

    "Render a case with keywords without KEYWORDS permissions" in {
      // Given
      val c           = aCase().copy(keywords = Set("APPLES", "CARS"))
      val keywordsTab = KeywordsTabViewModel.fromCase(c, Seq("APPLES", "TOYS"))

      // When
      val doc = view(keywords_details(keywordsTab, form)(operatorRequest, messages, appConfig))

      // Then
      doc shouldNot containElementWithID("keywords-row-0-remove")
      doc shouldNot containElementWithID("keyword_details-add_keyword")
    }

    "render notification banner when keyword was added" in {

      val requestWithFlashKeywordSuccess: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest("GET", "/", FakeHeaders(Seq("csrfToken" -> "csrfToken")), AnyContentAsEmpty)
          .withFlash(success("notification.success.keywords.add"))
          .withCSRFToken
          .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

      val doc = view(
        keywords_details(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")), form, 0)(
          AuthenticatedRequest(
            authenticatedOperator.copy(permissions = Set(Permission.KEYWORDS)),
            requestWithFlashKeywordSuccess
          ),
          messages,
          appConfig
        )
      )

      doc should containElementWithID("govuk-notification-banner-title")

    }
  }
}
