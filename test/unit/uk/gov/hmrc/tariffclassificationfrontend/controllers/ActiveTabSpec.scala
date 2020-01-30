package uk.gov.hmrc.tariffclassificationfrontend.controllers

import uk.gov.hmrc.play.test.UnitSpec

class ActiveTabSpec  extends UnitSpec {
  "ActiveTab Binder" should {

    "Unbind Populated ActivityTab to Query String" in {
      ActiveTab.queryStringBindable.unbind("sort_by", ActiveTab.Activity) shouldBe "activeTab=tab-item-Activity"
    }

    "Bind populated query string" in {
      ActiveTab.queryStringBindable.bind("activeTab", Map("activeTab" -> Seq("tab-item-Activity"))) shouldBe Some(Right(ActiveTab.Activity))
    }
  }
}
