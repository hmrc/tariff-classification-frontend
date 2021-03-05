package views.managementtools

import models.Keyword
import models.forms.v2.EditApprovedKeywordForm
import views.ViewSpec
import views.html.managementtools.edit_approved_keywords

class EditApprovedKeywordsViewSpec extends ViewSpec {

  val keywords    = Seq(Keyword("shoes", true), Keyword("hats", true), Keyword("shirts", true))
  val editApprovedKeywordForm = EditApprovedKeywordForm.formWithAuto(keywords.map(_.name))

  def editApprovedKeywordView: edit_approved_keywords = injector.instanceOf[edit_approved_keywords]

  "NewKeywordView" should {

    "render successfully with the default tab" in {

//      val doc = view(
//        editApprovedKeywordView(
//          allKeywords    = keywords,
//          newKeywordForm = keywordForm
//        )
//      )
//      doc                                        should containText(messages("management.create-keyword.caption"))
//      doc                                        should containElementWithID("common-cases-heading")
//      doc.getElementById("common-cases-heading") should containText(messages("management.create-keyword.heading"))
//    }
//
//    "contain a label and hint text" in {
//
//      val doc = view(
//        newKeywordView(
//          allKeywords    = keywords,
//          newKeywordForm = keywordForm
//        )
//      )
//      doc                                should containText(messages("management.create-keyword.label"))
//      doc.getElementById("keyword-hint") should containText(messages("management.create-keyword.hint"))
//    }
//


}
