/*
 * Copyright 2025 HM Revenue & Customs
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

import models._
import utils.Cases

import java.time.Instant

class LiabilityDetailsFormTest extends ModelsBaseSpec {

  private val liability = LiabilityOrder(
    Contact(name = "contact-name", email = "contact@email.com", Some("contact-phone")),
    status = LiabilityStatus.LIVE,
    traderName = "trader-name",
    goodName = Some("good-name"),
    entryDate = Some(Instant.EPOCH),
    entryNumber = Some("entry-no"),
    traderCommodityCode = Some("0200000000"),
    officerCommodityCode = Some("0100000000"),
    btiReference = Some("btiReferenceN"),
    repaymentClaim = Some(RepaymentClaim(dvrNumber = Some(""), dateForRepayment = Some(Instant.EPOCH))),
    dateOfReceipt = Some(Instant.EPOCH),
    traderContactDetails = Some(
      TraderContactDetails(
        email = Some("trader@email.com"),
        phone = Some("2345"),
        address = Some(
          Address(
            buildingAndStreet = "STREET 1",
            townOrCity = "Town",
            county = Some("County"),
            postCode = Some("postcode")
          )
        )
      )
    )
  )

  private val sampleCase =
    Cases.liabilityCaseExample.copy(caseBoardsFileNumber = Some("SCR/ARD/123"), application = liability)

  private val params = Map(
    "contactName"          -> Seq("contact-name"),
    "contactEmail"         -> Seq("contact@email.com"),
    "contactPhone"         -> Seq("contact-phone"),
    "traderName"           -> Seq("trader-name"),
    "goodName"             -> Seq("good-name"),
    "entryDate.day"        -> Seq("1"),
    "entryDate.month"      -> Seq("1"),
    "entryDate.year"       -> Seq("1970"),
    "entryNumber"          -> Seq("entry-no"),
    "traderCommodityCode"  -> Seq("0200000000"),
    "officerCommodityCode" -> Seq("0100000000")
  )

  private val emptyParams = params.view.mapValues(_ => Seq("")).toMap

  "Bind from request" should {

    "Bind blank" when {

      "using edit form" in {

        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleCase).bindFromRequest(emptyParams)

        form.hasErrors         shouldBe true
        form.errors              should have(size(2))
        form.errors.map(_.key) shouldBe Seq("traderName", "contactName")
      }

      "using complete form" in {
        val form =
          LiabilityDetailsForm.liabilityDetailsCompleteForm(sampleCase).bindFromRequest(emptyParams)

        form.hasErrors shouldBe true
        form.errors      should have(size(9))
        form.errors.map(_.key) shouldBe Seq(
          "entryDate",
          "traderName",
          "goodName",
          "entryNumber",
          "traderCommodityCode",
          "officerCommodityCode",
          "contactName",
          "contactEmail",
          "contactPhone"
        )
      }
    }

    "Bind valid form" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleCase).bindFromRequest(params)

        form.hasErrors shouldBe false
        form.get       shouldBe sampleCase
      }

      "using complete form" in {
        val form = LiabilityDetailsForm.liabilityDetailsCompleteForm(sampleCase).bindFromRequest(params)

        form.hasErrors shouldBe false
        form.get       shouldBe sampleCase
      }
    }
  }

  "Fill" should {
    "populate by default" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleCase)

        form.hasErrors shouldBe false
        form.data      shouldBe params.view.mapValues(v => v.head).toMap
      }

      "using complete form" in {
        val form = LiabilityDetailsForm.liabilityDetailsCompleteForm(sampleCase)

        form.hasErrors shouldBe false
        form.data      shouldBe params.view.mapValues(v => v.head).toMap
      }
    }
  }

}
