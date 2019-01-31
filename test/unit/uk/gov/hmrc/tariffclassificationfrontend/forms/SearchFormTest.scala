package uk.gov.hmrc.tariffclassificationfrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.Search

class SearchFormTest extends UnitSpec {

  "Search Form" should {
    "fill" in {
      SearchForm.fill(Search(
        traderName = Some("trader")
      )).data shouldBe Map(
        "traderName" -> "trader"
      )
    }

    "validate 'Trader Name'" in {
      SearchForm.form.bindFromRequest(
        Map(
          "traderName" -> Seq("")
        )
      ).errors shouldBe Seq(FormError("traderName", "error.required"))
    }
  }
}
