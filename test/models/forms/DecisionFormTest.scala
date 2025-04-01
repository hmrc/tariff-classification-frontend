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

import models.Decision
import play.api.data.FormError
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import org.mockito.Mockito.*
import base.SpecBase
import java.time.Instant

class DecisionFormTest extends SpecBase {

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
    "justification"        -> Seq("justification"),
    "goodsDescription"     -> Seq("goods description"),
    "methodSearch"         -> Seq("search"),
    "methodExclusion"      -> Seq("exclusion")
  )

  private val emptyParams = params.view.mapValues(_ => Seq("")).toMap

  val validDecisionFormData: DecisionFormData = DecisionFormData(
    bindingCommodityCode = "03000000000",
    goodsDescription = "desc",
    methodSearch = "method",
    justification = "justified",
    explanation = "some exp",
    expiryDate = Some(Instant.now),
    explicitEndDate = true
  )

  private def formProvider(commodityCodeConstraints: CommodityCodeConstraints) =
    new DecisionForm(commodityCodeConstraints)

  "liability form" should {
    "bind the form from the request" when {
      "provided with blank values" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(Valid)
        val form = formProvider(mockedCommodityCodeConstraint)
          .liabilityForm(decision)
          .bindFromRequest(emptyParams)

        form.hasErrors shouldBe false
      }

      "provided with valid values" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(Valid)
        val form = formProvider(mockedCommodityCodeConstraint).liabilityForm(decision).bindFromRequest(params)

        form.hasErrors shouldBe false
        form.get       shouldBe decision
      }
    }
    "bind the form from a decision model" when {
      "provided with valid values" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(Valid)
        val form                          = formProvider(mockedCommodityCodeConstraint).liabilityForm(decision)

        form.hasErrors shouldBe false
        form.data      shouldBe params.view.mapValues(v => v.head).toMap
      }
      "provided decision is not provided" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(Valid)
        val form                          = formProvider(mockedCommodityCodeConstraint).liabilityForm()

        form.hasErrors shouldBe false
      }
    }
  }
  "liability complete form" should {
    "bind the form from the request" when {
      "provided with valid values" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(Valid)
        val form = formProvider(mockedCommodityCodeConstraint).liabilityCompleteForm(decision).bindFromRequest(params)

        form.hasErrors shouldBe false
        form.get       shouldBe decision
      }
    }
    "bind the form from a decision model" when {
      "provided with valid values" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(Valid)
        val form                          = formProvider(mockedCommodityCodeConstraint).liabilityCompleteForm(decision)

        form.hasErrors shouldBe false
        form.data      shouldBe params.view.mapValues(v => v.head).toMap
      }
    }
    "return validation errors" when {
      "provided with invalid values" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(Valid)
        val form = formProvider(mockedCommodityCodeConstraint)
          .liabilityCompleteForm(decision)
          .bindFromRequest(emptyParams)

        form.hasErrors         shouldBe true
        form.errors.size       shouldBe 3
        form.errors.map(_.key) shouldBe Seq("goodsDescription", "methodSearch", "justification")
      }
    }
  }
  "bti complete form" should {
    "bind data correctly" when {
      "provided with valid values" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint()
        val form = formProvider(mockedCommodityCodeConstraint).btiCompleteForm.fillAndValidate(validDecisionFormData)

        form.hasErrors shouldBe false
      }
    }
    "return validation errors" when {
      "all fields are empty" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(validationResultForEmpty =
          Invalid("decision_form.error.bindingCommodityCode.required")
        )
        val form = formProvider(mockedCommodityCodeConstraint).btiCompleteForm.fillAndValidate(DecisionFormData())

        form.hasErrors shouldBe true

        form.errors shouldBe Seq(
          FormError("bindingCommodityCode", "decision_form.error.bindingCommodityCode.required"),
          FormError("goodsDescription", "decision_form.error.itemDescription.required"),
          FormError("methodSearch", "decision_form.error.searchesPerformed.required"),
          FormError("justification", "decision_form.error.legalJustification.required"),
          FormError("explanation", "decision_form.error.decisionExplanation.required")
        )
      }
    }
    "return commodity code invalid error" when {
      "provided by an invalid commodity code" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(
          validationResultForEmpty = Valid,
          validationResultForValidLength = Invalid("decision_form.error.bindingCommodityCode.valid")
        )

        val form = formProvider(mockedCommodityCodeConstraint).btiCompleteForm.fillAndValidate(validDecisionFormData)

        form.hasErrors shouldBe true

        form.errors shouldBe Seq(FormError("bindingCommodityCode", "decision_form.error.bindingCommodityCode.valid"))
      }
    }
    "return only commodity code empty validation error" when {
      "validation fails both on commodityCodeNonEmpty and commodityCodeValid" in {
        val mockedCommodityCodeConstraint = mockCommodityCodeConstraint(
          validationResultForEmpty = Invalid("decision_form.error.bindingCommodityCode.required"),
          validationResultForValidLength = Invalid("decision_form.error.bindingCommodityCode.valid")
        )

        val form = formProvider(mockedCommodityCodeConstraint).btiCompleteForm.fillAndValidate(validDecisionFormData)

        form.hasErrors shouldBe true

        form.errors shouldBe Seq(FormError("bindingCommodityCode", "decision_form.error.bindingCommodityCode.required"))
      }
    }
  }

  private def mockCommodityCodeConstraint(
    validationResultForEmpty: ValidationResult = Valid,
    validationResultForValidLength: ValidationResult = Valid,
    validationResultForValidNumberType: ValidationResult = Valid,
    validationResultForValidEvenDigits: ValidationResult = Valid
  ): CommodityCodeConstraints = {
    val mockCommodityCodeConstraints = mock[CommodityCodeConstraints]
    when(mockCommodityCodeConstraints.commodityCodeLengthValid)
      .thenReturn(Constraint[String]("error")(_ => validationResultForValidLength))
    when(mockCommodityCodeConstraints.commodityCodeNumbersValid)
      .thenReturn(Constraint[String]("error")(_ => validationResultForValidNumberType))
    when(mockCommodityCodeConstraints.commodityCodeEvenDigitsValid)
      .thenReturn(Constraint[String]("error")(_ => validationResultForValidEvenDigits))

    when(mockCommodityCodeConstraints.commodityCodeNonEmpty)
      .thenReturn(Constraint[String]("error")(_ => validationResultForEmpty))
    mockCommodityCodeConstraints
  }

}
