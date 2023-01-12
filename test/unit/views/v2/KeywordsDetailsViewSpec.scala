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

package views.v2

import models.forms.KeywordForm
import models.request.AuthenticatedRequest
import models.viewmodels.KeywordsTabViewModel
import models.{Keyword, Permission}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.{FakeHeaders, FakeRequest}
import utils.Notification._
import views.ViewMatchers._
import views.ViewSpec
import views.html.v2.keywords_details

class KeywordsDetailsViewSpec extends ViewSpec {

  def keywordDetails: keywords_details = injector.instanceOf[keywords_details]

  val keywordsTabViewModel =
    KeywordsTabViewModel("reference", Set("keyword1", "keyword2"), Seq("keyword1", "keywordX", "keywordY"))
  val keywords = Seq(Keyword("shoes", true), Keyword("hats", true), Keyword("shirts", true))
  val form     = KeywordForm.formWithAuto(keywords.map(_.name))

  "Keyword Details" should {

    "render successfully" in {

      val doc = view(keywordDetails(keywordsTabViewModel, form, 0))
      doc should containElementWithID("keywords-table")

    }

    "output 'Keyword is not from the list' when it is not in global keywords" in {

      val doc =
        view(keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keywordX")), form, 0))
      doc.getElementById("keywords-row-0-message") should containText("Keyword is not from the list")

    }

    "not output 'Keyword is not from the list' when it is in global keywords" in {

      val doc =
        view(keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")), form, 0))
      doc.getElementById("keywords-row-0-message") shouldNot containText("Keyword is not from the list")

    }

    "not show remove keyword when incorrect permissions" in {

      val doc =
        view(keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")), form, 0))

      doc shouldNot containElementWithID("keywords-row-0-remove")

    }

    "show remove keyword when has correct permissions" in {

      val doc = view(
        keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")), form, 0)(
          requestWithPermissions(Permission.KEYWORDS),
          messages,
          appConfig
        )
      )

      doc should containElementWithID("keywords-row-0-remove")

    }

    "render add keyword form when has correct permissions" in {

      val doc = view(
        keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")), form, 0)(
          requestWithPermissions(Permission.KEYWORDS),
          messages,
          appConfig
        )
      )

      doc should containElementWithTag("form")

    }

    "render notification banner when keyword was added" in {

      val requestWithFlashKeywordSuccess: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest("GET", "/", FakeHeaders(Seq("csrfToken" -> "csrfToken")), AnyContentAsEmpty)
          .withFlash(success("notification.success.keywords.add"))
          .withCSRFToken
          .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

      val doc = view(
        keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")), form, 0)(
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
