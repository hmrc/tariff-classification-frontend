package uk.gov.hmrc.tariffclassificationfrontend.forms

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus

class AppealFormTest extends UnitSpec {

  "Bind from request" should {
    "Bind empty" in {
      val form = AppealForm.form.bindFromRequest(Map())

      form.hasErrors shouldBe true
    }

    "Bind blank" in {
      val form = AppealForm.form.bindFromRequest(Map("status" -> Seq("")))

      form.hasErrors shouldBe false
    }

    "Bind valid enum" in {
      val form = AppealForm.form.bindFromRequest(Map("status" -> Seq(AppealStatus.IN_PROGRESS.toString)))

      form.hasErrors shouldBe false
    }

    "Bind invalid enum" in {
      val form = AppealForm.form.bindFromRequest(Map("status" -> Seq("other")))

      form.hasErrors shouldBe true
    }
  }

  "Fill" should {
    "populate empty" in {
      val form = AppealForm.form.fill(None)

      form.hasErrors shouldBe false
      form.data shouldBe Map()
    }

    "populate some" in {
      val form = AppealForm.form.fill(Some(AppealStatus.IN_PROGRESS))

      form.hasErrors shouldBe false
      form.data shouldBe Map("status" -> "IN_PROGRESS")
    }
  }



}
