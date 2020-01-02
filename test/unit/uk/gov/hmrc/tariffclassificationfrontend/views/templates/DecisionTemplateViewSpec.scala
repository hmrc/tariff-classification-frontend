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

package uk.gov.hmrc.tariffclassificationfrontend.views.templates

import uk.gov.hmrc.tariffclassificationfrontend.models.LiabilityStatus
import uk.gov.hmrc.tariffclassificationfrontend.utils.Dates
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers.containText
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.templates.{decision_template, ruling_template}
import uk.gov.tariffclassificationfrontend.utils.Cases


class DecisionTemplateViewSpec extends ViewSpec {

  private val rulingCase = Cases.liabilityCaseWithDecisionExample
  private val liability = rulingCase.application.asLiabilityOrder
  private val ruling = rulingCase.decision.getOrElse(throw new Exception("Bad test data"))
  private val doc = view(decision_template(rulingCase, ruling)(authenticatedFakeRequest, messages, appConfig))

  "Decision pdf c592 section" must {

    val section = "section-c592"

    "contain the liability type" in {
      assertSectionContains(section, LiabilityStatus.format(liability.status))
    }

    "contain the complicance officer's name" in {
      assertSectionContains(section, liability.contact.name)
    }

    "contain the trader name" in {
      assertSectionContains(section, liability.traderName)
    }

    "contain the liability entry date" in {
      assertSectionContains(section, Dates.format(liability.entryDate))
    }

    "contain the liability entry number" in {
      assertSectionContains(section, liability.entryNumber.getOrElse("Bad test data"))
    }
  }

  "Decision pdf information section" must {

    val section = "section-information"

    "contain the classification officer name" in {
      assertSectionContains(section, rulingCase.assignee.flatMap(x => x.name).getOrElse("Hector Salamanca").toString)
    }

    "contain the case reference" in {
      assertSectionContains(section, rulingCase.reference)
    }

    "contain the decision date" in {
      assertSectionContains(section, Dates.format(ruling.effectiveStartDate))
    }

  }

  "Decision pdf commodity code section" must {

    val section = "section-commcode"

    "contain the suggested commodity code" in {
      assertSectionContains(section, ruling.bindingCommodityCode)
    }
  }

  "Decision pdf item description" must {

    val section = "section-description"

    "contain the goods description" in {
      assertSectionContains(section, ruling.goodsDescription)
    }
  }

  "Decision pdf justification" must {

    val section = "section-justification"

    "contain the justification" in {
      assertSectionContains(section, ruling.justification)
    }
  }

  "Decision pdf exclusions" must {

    val section = "section-exclusion"

    "contain the justification" in {
      assertSectionContains(section, ruling.methodExclusion.getOrElse("This will not match"))
    }
  }

  private def assertSectionContains(id: String, text: String) = {
    doc.getElementById(id) should containText(text)
  }

}
