/*
 * Copyright 2024 HM Revenue & Customs
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

package views.templates

import utils.{Cases, Dates}
import views.ViewMatchers.containText
import views.ViewSpec
import views.html.templates.ruling_template

class RulingTemplateViewSpec extends ViewSpec {

  private val rulingCase = Cases.btiCaseExample
  private val holder     = rulingCase.application.asATAR.holder
  private val ruling     = rulingCase.decision.getOrElse(throw new Exception("Bad test data"))
  private val doc = view(
    ruling_template(rulingCase, ruling, _ => Some("dummy country name"))(messages)
  )

  "Ruling pdf holder section" should {

    val section = "section-holder"

    "contain the holders name" in {
      assertSectionContains(section, holder.businessName)
    }

    "contain the holders address" in {
      assertSectionContains(section, holder.addressLine1)
      assertSectionContains(section, holder.addressLine2)
      assertSectionContains(section, holder.addressLine3)
      assertSectionContains(section, holder.postcode)
      assertSectionContains(section, holder.country)
    }

    "contain the holders EORI" in {
      assertSectionContains(section, holder.eori)
    }
  }

  "Ruling pdf ruling section" should {

    val section = "section-ruling"

    "contain the binding commodity code" in {
      assertSectionContains(section, ruling.bindingCommodityCode)
    }

    "contain the case reference" in {
      assertSectionContains(section, rulingCase.reference)
    }

    "contain the ruling start data" in {
      assertSectionContains(section, Dates.format(ruling.effectiveStartDate))
    }

    "contain the ruling end data" in {
      assertSectionContains(section, Dates.format(ruling.effectiveEndDate))
    }
  }

  "Ruling pdf goods section" should {

    val section = "section-goods"

    "contain the good description" in {
      assertSectionContains(section, ruling.goodsDescription)
    }
  }

  "Ruling pdf goods commercial denomination" should {

    val section = "section-commercial"

    "contain the good description" in {
      assertSectionContains(
        section,
        ruling.methodCommercialDenomination.getOrElse(throw new Exception("Bad test data"))
      )
    }
  }

  "Ruling pdf justification" should {

    val section = "section-justification"

    "contain the good description" in {
      assertSectionContains(section, ruling.justification)
    }
  }

  private def assertSectionContains(id: String, text: String) =
    doc.getElementById(id) should containText(text)

}
