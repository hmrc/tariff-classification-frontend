package views.v2

import models.Permission
import models.forms.KeywordForm
import models.viewmodels.KeywordsTabViewModel
import views.ViewMatchers._
import views.ViewSpec
import views.html.v2.keywords_details

class KeywordsDetailsViewSpec extends ViewSpec {

  def keywordDetails: keywords_details = app.injector.instanceOf[keywords_details]

  val keywordsTabViewModel = KeywordsTabViewModel("reference", Set("keyword1", "keyword2"), Seq("keyword1", "keywordX", "keywordY"))

  "Keyword Details" should {

    "render successfully" in {

      val doc = view(keywordDetails(keywordsTabViewModel,KeywordForm.form, 0))
      doc should containElementWithID("keywords-table")

    }

    "output 'Keyword is not from the list' when it is not in global keywords" in {

      val doc = view(keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keywordX")),KeywordForm.form, 0))
      doc.getElementById("keywords-row-0-message") should containText("Keyword is not from the list")

    }

    "not output 'Keyword is not from the list' when it is in global keywords" in {

      val doc = view(keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")),KeywordForm.form, 0))
      doc.getElementById("keywords-row-0-message") shouldNot containText("Keyword is not from the list")

    }

    "not show remove keyword when incorrect permissions" in {

      val doc = view(keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")),KeywordForm.form, 0))

      doc shouldNot containElementWithID("keywords-row-0-remove")

    }

    "show remove keyword when has correct permissions" in {

      val doc = view(keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")),KeywordForm.form, 0)(requestWithPermissions(Permission.KEYWORDS),messages, appConfig))

      doc should containElementWithID("keywords-row-0-remove")

    }

    "render add keyword form when has correct permissions" in {

      val doc = view(keywordDetails(KeywordsTabViewModel("reference", Set("keyword1"), Seq("keyword1")),KeywordForm.form, 0)(requestWithPermissions(Permission.KEYWORDS),messages, appConfig))

      doc should containElementWithTag("form")

    }
  }

}
