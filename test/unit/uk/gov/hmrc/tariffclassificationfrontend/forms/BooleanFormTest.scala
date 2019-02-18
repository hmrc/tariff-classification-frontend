package uk.gov.hmrc.tariffclassificationfrontend.forms

import uk.gov.hmrc.play.test.UnitSpec

class BooleanFormTest extends UnitSpec {

  "Boolean Form 'Bind From Request'" should {
    "fail on empty request" in {
      BooleanForm.form.bindFromRequest(Map("state" -> Seq())).hasErrors shouldBe true
      BooleanForm.form.bindFromRequest(Map("state" -> Seq(""))).hasErrors shouldBe true
    }

    "fail on non boolean request" in {
      BooleanForm.form.bindFromRequest(Map("state" -> Seq("123"))).hasErrors shouldBe true
    }

    "succeed on valid request" in {
      val emptyForm = BooleanForm.form.bindFromRequest(Map())
      emptyForm.hasErrors shouldBe false
      emptyForm.get shouldBe false

      val falsyForm = BooleanForm.form.bindFromRequest(Map("state" -> Seq("false")))
      falsyForm.hasErrors shouldBe false
      falsyForm.get shouldBe false

      val truthyForm = BooleanForm.form.bindFromRequest(Map("state" -> Seq("true")))
      truthyForm.hasErrors shouldBe false
      truthyForm.get shouldBe true
    }
  }

  "Boolean Form 'fill'" should {
    "pre populate form" in {
      BooleanForm.form.fill(true).data shouldBe Map("state" -> "true")
      BooleanForm.form.fill(false).data shouldBe Map("state" -> "false")
    }
  }

}
