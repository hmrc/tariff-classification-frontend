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
