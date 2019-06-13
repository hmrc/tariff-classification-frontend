/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.forms

import java.time.Instant

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.{Contact, LiabilityOrder, LiabilityStatus}

class LiabilityDetailsFormTest extends UnitSpec {

  private val liability = LiabilityOrder(
    Contact(name = "contact-name", email = "contact@email.com", Some("contact-phone")),
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
    "contactEmail" -> Seq("contact@email.com"),
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
