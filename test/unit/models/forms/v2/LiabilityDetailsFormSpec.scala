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
import org.joda.time.{DateTime, DateTimeZone}
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases

class LiabilityDetailsFormSpec extends UnitSpec {

  private val emptyLiabilityOrder = LiabilityOrder(
    Contact(name = "", email = "", Some("")),
    status = LiabilityStatus.LIVE,
    traderName = "trader-name",
    goodName = Some("good-name"),
    entryDate = Some(Instant.EPOCH),
    entryNumber = Some(""),
    traderCommodityCode = Some(""),
    officerCommodityCode = Some(""),
    btiReference = Some(""),
    repaymentClaim = None,
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

  private val liabilityOrder = LiabilityOrder(
    Contact(name = "contact-name", email = "contact@email.com", Some("contact-phone")),
    status = LiabilityStatus.LIVE,
    traderName = "trader-name",
    goodName = Some("good-name"),
    entryDate = Some(Instant.EPOCH),
    entryNumber = Some("123456"),
    traderCommodityCode = Some("0200000000"),
    officerCommodityCode = Some("0100000000"),
    btiReference = Some("12345678"),
    repaymentClaim = Some(RepaymentClaim(dvrNumber = Some("123456"), dateForRepayment = Some(Instant.EPOCH))),
    dateOfReceipt = Some(Instant.EPOCH),
    traderContactDetails = Some(TraderContactDetails(email = Some("trader@email.com"),
      phone = Some("0123456764"),
      address = Some(Address(buildingAndStreet = "1 Street",
        townOrCity = "Town",
        county = Some("County"),
        postCode = Some("postcode")
      ))
    ))
  )

  private val sampleCase = Cases.newLiabilityLiveCaseExample.copy(caseBoardsFileNumber = Some("SCR/ARD/123"), application = liabilityOrder)
  private val sampleEmptyCase = Cases.newLiabilityLiveCaseExample.copy(caseBoardsFileNumber = Some("SCR/ARD/123"), application = emptyLiabilityOrder)

  private val day = new DateTime(Instant.EPOCH.getEpochSecond, DateTimeZone.forID("Etc/UTC")).dayOfMonth.getAsText
  private val month = new DateTime(Instant.EPOCH.getEpochSecond, DateTimeZone.forID("Etc/UTC")).getMonthOfYear.toString
  private val year = new DateTime(Instant.EPOCH.getEpochSecond, DateTimeZone.forID("Etc/UTC")).year.getAsText


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
    "traderPostcode" -> Seq("postcode"),
    "boardsFileNumber" -> Seq("SCR/ARD/123"),
    "goodName" -> Seq("good-name"),
    "entryDate.day" -> Seq(day),
    "entryDate.month" -> Seq(month),
    "entryDate.year" -> Seq(year),
    "entryNumber" -> Seq("123456"),
    "traderCommodityCode" -> Seq("0200000000"),
    "officerCommodityCode" -> Seq("0100000000"),
    "btiReference" -> Seq("12345678"),
    "repaymentClaim"-> Seq("true"),
    "dateOfReceipt.day" -> Seq(day),
    "dateOfReceipt.month" -> Seq(month),
    "dateOfReceipt.year" -> Seq(year),
    "dvrNumber" -> Seq("123456"),
    "dateForRepayment.day" -> Seq(day),
    "dateForRepayment.month" -> Seq(month),
    "dateForRepayment.year" -> Seq(year)
  )

  private val traderAddressParams: Map[String, Seq[String]] = Map("traderName" -> params.getOrElse("traderName", Seq("traderName")))

  private val booleanValues = Seq("repaymentClaim")
  private val emptyParams = (params -- booleanValues).mapValues(_ => Seq(""))

  "Bind from request" should {
    "Bind blank" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleEmptyCase).bindFromRequest(emptyParams)

        form.hasErrors shouldBe true
        form.errors should have(size(1))
        form.errors.map(_.key) shouldBe Seq("traderName")
      }

    }

    "Bind valid form" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleCase).bindFromRequest(params)
        form.hasErrors shouldBe false
        form.get shouldBe sampleCase
      }

    }
  }

  "Fill" should {
    "populate by default" when {
      "using edit form" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleCase)

        form.hasErrors shouldBe false
        form.data shouldBe params.mapValues(v => v.head)
      }

      "using edit form is repayments claim set to true" in {
        val form = LiabilityDetailsForm.liabilityDetailsForm(sampleCase)

        form.hasErrors shouldBe false
        form.get.application.asLiabilityOrder.repaymentClaim shouldBe sampleCase.application.asLiabilityOrder.repaymentClaim
      }

    }
  }


}
