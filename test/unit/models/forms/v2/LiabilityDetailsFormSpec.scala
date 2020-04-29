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

package models.forms.v2

import java.time.Instant

import models.{Address, Contact, LiabilityOrder, LiabilityStatus, RepaymentClaim, TraderContactDetails}
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases

class LiabilityDetailsFormSpec extends UnitSpec {


  private val emptyLiability = LiabilityOrder(
    Contact(name = "", email = "", Some("")),
    status = LiabilityStatus.LIVE,
    traderName = "trader-name",
    goodName = Some("good-name"),
    entryDate = Some(Instant.EPOCH),
    entryNumber = Some(""),
    traderCommodityCode = Some(""),
    officerCommodityCode = Some(""),
    btiReference = Some(""),
    repaymentClaim = Some(RepaymentClaim(dvrNumber = Some(""), dateForRepayment = Some(Instant.EPOCH))),
    dateOfReceipt = Some(Instant.EPOCH),
    traderContactDetails = Some(TraderContactDetails(email = Some(""),
      phone = Some(""),
      address = Some(Address(buildingAndStreet = "",
        townOrCity = "",
        county = Some(""),
        postCode = Some("")
      ))
    ))

  )

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
    traderContactDetails = Some(TraderContactDetails(email = Some("trader@email.com"),
      phone = Some("2345"),
      address = Some(Address(buildingAndStreet = "STREET 1",
        townOrCity = "Town",
        county = Some("County"),
        postCode = Some("postcode")
      ))
    ))
  )

  private val sampleCase = Cases.newLiabilityLiveCaseExample.copy(caseBoardsFileNumber = Some("SCR/ARD/123"), application = liability)
  private val sampleEmptyCase = Cases.newLiabilityLiveCaseExample.copy(caseBoardsFileNumber = Some("SCR/ARD/123"), application = emptyLiability)

  private val params = Map(
    "contactName" -> Seq("contact-name"),
    "contactEmail" -> Seq("contact@email.com"),
    "contactPhone" -> Seq("contact-phone"),
    "traderName" -> Seq("trader-name"),
    "traderEmail" -> Seq("trader@email.com"),
    "traderPhone" -> Seq("0123456764"),
    "traderBuildingAndStreet" -> Seq("1 Street"),
    "traderTownOrCity" -> Seq("Town"),
    "traderCounty" -> Seq("County"),
    "traderPostcode" -> Seq("AA11AA"),
    "boardsFileNumber" -> Seq("SCR/ARD/123"),
    "goodName" -> Seq("good-name"),
    "entryDate.day" -> Seq("1"),
    "entryDate.month" -> Seq("1"),
    "entryDate.year" -> Seq("1970"),
    "entryNumber" -> Seq("123456"),
    "traderCommodityCode" -> Seq("0200000000"),
    "officerCommodityCode" -> Seq("0100000000"),
    "btiReference" -> Seq("12345678"),
//    "repaymentClaim"-> Seq(),
    "dateOfReceipt.day" -> Seq("2"),
    "dateOfReceipt.month" -> Seq("3"),
    "dateOfReceipt.year" -> Seq("2010"),
    "dvrNumber" -> Seq("123456"),
    "dateForRepayment.day" -> Seq("2"),
    "dateForRepayment.month" -> Seq("3"),
    "dateForRepayment.year" -> Seq("2020")
  )

  "Bind from request" should {
    "Bind blank" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleEmptyCase).bindFromRequest(params.mapValues(_ => Seq("")))

        form.hasErrors shouldBe true
        form.errors should have(size(1))
        form.errors.map(_.key) shouldBe Seq("traderName")
      }

    }

    "Bind valid form" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleEmptyCase).bindFromRequest(params)

        println("111111111111111")
        println(form.errors)
        form.hasErrors shouldBe false
        form.get shouldBe sampleEmptyCase
      }

    }
  }

  "Fill" should {
    "populate by default" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleEmptyCase)

        form.hasErrors shouldBe false
        form.data shouldBe params.mapValues(v => v.head)
      }

    }
  }


}
