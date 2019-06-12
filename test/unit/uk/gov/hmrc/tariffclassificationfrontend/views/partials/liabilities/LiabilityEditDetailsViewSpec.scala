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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials.liabilities

import uk.gov.hmrc.tariffclassificationfrontend.forms.LiabilityDetailsForm
import uk.gov.hmrc.tariffclassificationfrontend.models.LiabilityOrder
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.liabilities.liability_details_edit
import uk.gov.tariffclassificationfrontend.utils.Cases._

class LiabilityEditDetailsViewSpec extends ViewSpec {

  "Liability Edit Details" should {

    "Render all fields for an empty form" in {
      // Given
      val c = aCase(withLiabilityApplication())

      // When
      val doc = view(liability_details_edit(c, LiabilityDetailsForm.liabilityDetailsForm))

      // Then
      doc should containElementWithID("liability-details-edit-form")
    }

    "Render all fields with expected values" in {
      // Given
      val c = aCase(withLiabilityApplication())

      // When
      val doc = view(liability_details_edit(c,createLiabilityForm(c.application.asLiabilityOrder)))

      // Then
      doc should containElementWithID("liability-details-edit-form")
      doc.getElementById("entryNumber").attr("value") shouldBe "entry number"
      doc.getElementById("traderName").attr("value") shouldBe "trader-business-name"
      doc.getElementById("goodName").attr("value") shouldBe "good-name"
      doc.getElementById("traderCommodityCode").attr("value") shouldBe "trader-1234567"
      doc.getElementById("officerCommodityCode").attr("value") shouldBe "officer-1234567"
      doc.getElementById("contactName").attr("value") shouldBe "name"
      doc.getElementById("contactEmail").attr("value") shouldBe "email"
      doc.getElementById("contactPhone").attr("value") shouldBe "phone"
    }
  }

  def createLiabilityForm(l: LiabilityOrder) = {
    LiabilityDetailsForm.liabilityDetailsForm.fill(
      LiabilityDetailsForm(
        entryDate = l.entryDate,
        traderName = l.traderName,
        goodName = l.goodName.getOrElse(""),
        entryNumber = l.entryNumber.getOrElse(""),
        traderCommodityCode = l.traderCommodityCode.getOrElse(""),
        officerCommodityCode = l.officerCommodityCode.getOrElse(""),
        contactName = l.contact.name,
        contactEmail = Some(l.contact.email),
        contactPhone = l.contact.phone.getOrElse("")
      )
    )
  }

}
