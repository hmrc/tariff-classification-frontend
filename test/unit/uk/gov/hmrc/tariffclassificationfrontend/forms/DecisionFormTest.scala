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

import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.validation.{Constraint, Valid}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.Decision

class DecisionFormTest extends UnitSpec with MockitoSugar {

  private val decision = Decision(
    bindingCommodityCode = "0100000000",
    justification = "justification",
    goodsDescription = "goods description",
    methodSearch = Some("search"),
    methodExclusion = Some("exclusion"),
    explanation = Some("explanation")
  )

  private val params = Map(
    "bindingCommodityCode" -> Seq("0100000000"),
    "justification" -> Seq("justification"),
    "goodsDescription" -> Seq("goods description"),
    "methodSearch" -> Seq("search"),
    "methodExclusion" -> Seq("exclusion")
  )

  private val commodityCodeConstraints = mock[CommodityCodeConstraints]
  private val formProvider = new DecisionForm(commodityCodeConstraints)

  "Bind from request" should {
    given(commodityCodeConstraints.commodityCodeExistsInUKTradeTariff).willReturn(Constraint[String]("error")(_ => Valid))

    "Bind blank" when {
      "using edit form" in {
        val form = formProvider.liabilityForm(decision).bindFromRequest(params.mapValues(_ => Seq("")))

        form.hasErrors shouldBe false
      }

      "using complete form" in {
        val form = formProvider.liabilityCompleteForm(decision).bindFromRequest(params.mapValues(_ => Seq("")))

        form.hasErrors shouldBe true
        form.errors should have(size(4))
        form.errors.map(_.key) shouldBe Seq("bindingCommodityCode", "goodsDescription", "methodSearch", "justification")
      }
    }

    "Bind valid form" when {
      "using edit form" in {
        val form = formProvider.liabilityForm(decision).bindFromRequest(params)

        form.hasErrors shouldBe false
        form.get shouldBe decision
      }

      "using complete form" in {
        val form = formProvider.liabilityCompleteForm(decision).bindFromRequest(params)

        form.hasErrors shouldBe false
        form.get shouldBe decision
      }
    }
  }

  "Fill" should {
    given(commodityCodeConstraints.commodityCodeExistsInUKTradeTariff).willReturn(Constraint[String]("error")(_ => Valid))

    "populate by default" when {
      "using edit form" in {
        val form = formProvider.liabilityForm(decision)

        form.hasErrors shouldBe false
        form.data shouldBe params.mapValues(v => v.head)
      }

      "using complete form" in {
        val form = formProvider.liabilityCompleteForm(decision)

        form.hasErrors shouldBe false
        form.data shouldBe params.mapValues(v => v.head)
      }
    }
  }

}
