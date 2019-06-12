package uk.gov.hmrc.tariffclassificationfrontend.forms

import java.time.Instant

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.{Contact, LiabilityOrder, LiabilityStatus}

class LiabilityDetailsFormTest extends UnitSpec {

  private val liability = LiabilityOrder(
    Contact(name = "contact-name", email = "contact-email", Some("contact-phone")),
    status = LiabilityStatus.LIVE,
    traderName = "trader-name",
    goodName = Some("good-name"),
    entryDate = Some(Instant.EPOCH),
    entryNumber = Some("entry-no"),
    traderCommodityCode = Some("0200000000"),
    officerCommodityCode = Some("0100000000")
  )

  private val params = Map(
    "contactName" -> Seq("contact-name"),
    "contactEmail" -> Seq("contact-email"),
    "contactPhone" -> Seq("contact-phone"),
    "traderName" -> Seq("trader-name"),
    "goodName" -> Seq("good-name"),
    "entryDate.day" -> Seq("1"),
    "entryDate.month" -> Seq("1"),
    "entryDate.year" -> Seq("1970"),
    "entryNumber" -> Seq("entry-no"),
    "traderCommodityCode" -> Seq("0200000000"),
    "officerCommodityCode" -> Seq("0100000000")
  )

  "Bind from request" should {
    "Bind empty" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(liability).bindFromRequest(Map())

        form.hasErrors shouldBe true
        form.errors should have(size(3))
        form.errors.map(_.key) shouldBe Seq("traderName", "contactName", "contactEmail")
      }

      "using complete form" in {
        val form = LiabilityDetailsForm.liabilityDetailsCompleteForm(liability).bindFromRequest(Map())

        form.hasErrors shouldBe true
        form.errors should have(size(9))
        form.errors.map(_.key) shouldBe Seq("entryDate", "traderName", "goodName", "entryNumber", "traderCommodityCode", "officerCommodityCode", "contactName", "contactEmail", "contactPhone")
      }
    }

    "Bind blank" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(liability).bindFromRequest(params.mapValues(_ => Seq("")))

        form.hasErrors shouldBe true
        form.errors should have(size(1))
        form.errors.map(_.key) shouldBe Seq("traderName")
      }

      "using complete form" in {
        val form = LiabilityDetailsForm.liabilityDetailsCompleteForm(liability).bindFromRequest(params.mapValues(_ => Seq("")))

        form.hasErrors shouldBe true
        form.errors should have(size(9))
        form.errors.map(_.key) shouldBe Seq("entryDate", "traderName", "goodName", "entryNumber", "traderCommodityCode", "officerCommodityCode", "contactName", "contactEmail", "contactPhone")
      }
    }

    "Bind valid form" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(liability).bindFromRequest(params)

        form.hasErrors shouldBe false
        form.get shouldBe liability
      }

      "using complete form" in {
        val form = LiabilityDetailsForm.liabilityDetailsCompleteForm(liability).bindFromRequest(params)

        form.hasErrors shouldBe false
        form.get shouldBe liability
      }
    }
  }

  "Fill" should {
    "populate by default" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(liability)

        form.hasErrors shouldBe false
        form.data shouldBe params.mapValues(v => v.head)
      }

      "using complete form" in {
        val form = LiabilityDetailsForm.liabilityDetailsCompleteForm(liability)

        form.hasErrors shouldBe false
        form.data shouldBe params.mapValues(v => v.head)
      }
    }
  }


}
