package uk.gov.hmrc.tariffclassificationfrontend.forms

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.{Contact, LiabilityOrder, LiabilityStatus}

class LiabilityFormTest extends UnitSpec {

  "Bind from request" should {
    "Bind empty" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map())

      form.hasErrors shouldBe true
      form.errors should have(size(2))
    }

    "Bind blank" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map(
        "liability-status" -> Seq(""),
        "trader-name" -> Seq("")
      ))

      form.hasErrors shouldBe true
      form.errors should have(size(2))
    }

    "Bind valid form" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map(
        "liability-status" -> Seq("LIVE"),
        "trader-name" -> Seq("Name")
      ))

      form.hasErrors shouldBe false
      form.get shouldBe LiabilityOrder(
        contact = Contact(name = "", email = ""),
        status = LiabilityStatus.LIVE,
        traderName = "Name"
      )
    }

    "Bind invalid status" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map(
        "liability-status" -> Seq("other"),
        "trader-name" -> Seq("Name")
      ))

      form.hasErrors shouldBe true
      form.errors should have(size(1))
    }
  }

  "Fill" should {

    "populate" in {
      val form = LiabilityForm.newLiabilityForm.fill(
        LiabilityOrder(
          contact = Contact(name = "", email = ""),
          status = LiabilityStatus.LIVE,
          traderName = "Name"
        )
      )

      form.hasErrors shouldBe false
      form.data shouldBe Map(
        "liability-status" -> "LIVE",
        "trader-name" -> "Name"
      )
    }
  }

}
