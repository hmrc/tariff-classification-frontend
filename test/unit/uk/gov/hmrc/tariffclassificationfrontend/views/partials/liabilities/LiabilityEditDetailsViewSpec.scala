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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials.liabilities

import uk.gov.hmrc.tariffclassificationfrontend.forms.LiabilityDetailsForm
import uk.gov.hmrc.tariffclassificationfrontend.models.Contact
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.liabilities.liability_details_edit
import uk.gov.tariffclassificationfrontend.utils.Cases._

class LiabilityEditDetailsViewSpec extends ViewSpec {

  "Liability Edit Details" should {

    "Render all fields for an empty form" in {
      // Given
      val c = aCase(withLiabilityApplication())
      val l  = c.application.asLiabilityOrder

      // When
      val doc = view(liability_details_edit(c, LiabilityDetailsForm.liabilityDetailsForm(l)))

      // Then
      doc should containElementWithID("liability-details-edit-form")
    }

    "Render all fields with expected values" in {
      // Given
      val c = aCase(withLiabilityApplication(
        entryNumber = Some("entry number"),
        traderName = "trader name",
        goodName = Some("good name"),
        traderCommodityCode = Some("123"),
        officerCommodityCode = Some("321"),
        contact = Contact(name  = "name", email = "email", phone = Some("phone"))
      ))
      val l  = c.application.asLiabilityOrder

      // When
      val doc = view(liability_details_edit(c,LiabilityDetailsForm.liabilityDetailsForm(l)))

      // Then
      doc should containElementWithID("liability-details-edit-form")
      doc.getElementById("entryNumber").attr("value") shouldBe "entry number"
      doc.getElementById("traderName").attr("value") shouldBe "trader name"
      doc.getElementById("goodName").attr("value") shouldBe "good name"
      doc.getElementById("traderCommodityCode").attr("value") shouldBe "123"
      doc.getElementById("officerCommodityCode").attr("value") shouldBe "321"
      doc.getElementById("contactName").attr("value") shouldBe "name"
      doc.getElementById("contactEmail").attr("value") shouldBe "email"
      doc.getElementById("contactPhone").attr("value") shouldBe "phone"
    }
  }

}
