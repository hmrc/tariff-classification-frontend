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

package views.v2

import java.time.Instant

import models.forms.LiabilityDetailsForm
import models.{Address, Contact, LiabilityOrder, LiabilityStatus, RepaymentClaim, TraderContactDetails}
import utils.Cases
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.v2.liability_details_edit

class LiabilityDetailsEditViewSpec extends ViewSpec {

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

  private val sampleCase = Cases.liabilityCaseExample.copy(caseBoardsFileNumber = Some("SCR/ARD/123"), application = liability)


  "Liability Details Edit View" should {
    "render correctly" in {
      val doc = view(liability_details_edit(sampleCase, LiabilityDetailsForm.liabilityDetailsForm(sampleCase)))

      doc should containElementWithID("liability-details-edit-form")

    }
  }
}
