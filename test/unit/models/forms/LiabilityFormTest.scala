/*
 * Copyright 2020 HM Revenue & Customs
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

package models.forms

import uk.gov.hmrc.play.test.UnitSpec
import models.{Contact, LiabilityOrder, LiabilityStatus}

class LiabilityFormTest extends UnitSpec {

  "Bind from request" should {
    "Bind an empty form" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map())

      form.hasErrors shouldBe true
      form.errors should have(size(3))
    }

    "Bind a blank form" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map(
        "liability-status" -> Seq(""),
        "trader-name" -> Seq(""),
        "item-name" -> Seq("")
      ))

      form.hasErrors shouldBe true
      form.errors should have(size(3))
    }

    "Bind a valid form" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map(
        "liability-status" -> Seq("LIVE"),
        "trader-name" -> Seq("trader name"),
        "item-name" -> Seq("item name")
      ))

      form.hasErrors shouldBe false
      form.get shouldBe LiabilityOrder(
        contact = Contact(name = "", email = ""),
        status = LiabilityStatus.LIVE,
        traderName = "trader name",
        goodName = Some("item name")
      )
    }

    "Bind invalid status" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map(
        "liability-status" -> Seq("other"),
        "trader-name" -> Seq("Name"),
        "item-name" -> Seq("item name")
      ))

      form.hasErrors shouldBe true
      form.errors should have(size(1))
    }

    "Bind invalid trader name" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map(
        "liability-status" -> Seq("other"),
        "trader-name" -> Seq(""),
        "item-name" -> Seq("item name")
      ))

      form.hasErrors shouldBe true
      form.errors should have(size(1))
    }

    "Bind invalid item name" in {
      val form = LiabilityForm.newLiabilityForm.bindFromRequest(Map(
        "liability-status" -> Seq("other"),
        "trader-name" -> Seq("trader"),
        "item-name" -> Seq("")
      ))

      form.hasErrors shouldBe true
      form.errors should have(size(1))
    }
  }

  "Fill" should {

    "populate a correct form" in {
      val form = LiabilityForm.newLiabilityForm.fill(
        LiabilityOrder(
          contact = Contact(name = "", email = ""),
          status = LiabilityStatus.LIVE,
          traderName = "Name",
          goodName = Some("item name")
        )
      )

      form.hasErrors shouldBe false
      form.data shouldBe Map(
        "liability-status" -> "LIVE",
        "trader-name" -> "Name",
        "item-name" -> "item name"
      )
    }
  }

}
