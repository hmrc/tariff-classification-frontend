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

import models.{Case, Decision}

import javax.inject.Singleton

@Singleton
class DecisionFormMapper {

  def mergeFormIntoCase(c: Case, decisionForm: DecisionFormData): Case = {

    val decision = c.decision match {
      case Some(d: Decision) => amendDecision(d, decisionForm)
      case _                 => from(decisionForm)
    }

    val attachments = c.attachments
      .map(att => att.copy(shouldPublishToRulings = decisionForm.attachments.contains(att.id)))

    c.copy(decision = Some(decision), attachments = attachments)
  }

  def caseToDecisionFormData(c: Case): DecisionFormData = {

    val form = c.decision map { d: Decision =>
      DecisionFormData(
        d.bindingCommodityCode,
        d.goodsDescription,
        d.methodSearch.getOrElse(""),
        d.justification,
        d.methodCommercialDenomination.getOrElse(""),
        d.methodExclusion.getOrElse(""),
        c.attachments.filter(_.shouldPublishToRulings).map(_.id),
        d.explanation.getOrElse(""),
        d.effectiveEndDate,
        explicitEndDate = if (d.effectiveEndDate.isDefined) true else false
      )
    }

    form.getOrElse(DecisionFormData())
  }

  private def amendDecision(decision: Decision, form: DecisionFormData): Decision =
    decision.copy(
      bindingCommodityCode = form.bindingCommodityCode,
      goodsDescription = form.goodsDescription,
      methodSearch = Some(form.methodSearch),
      justification = form.justification,
      methodCommercialDenomination = Some(form.methodCommercialDenomination),
      methodExclusion = Some(form.methodExclusion),
      explanation = Some(form.explanation),
      effectiveEndDate = form.expiryDate
    )

  private def from(form: DecisionFormData): Decision =
    Decision(
      bindingCommodityCode = form.bindingCommodityCode,
      goodsDescription = form.goodsDescription,
      methodSearch = Some(form.methodSearch),
      justification = form.justification,
      methodCommercialDenomination = Some(form.methodCommercialDenomination),
      methodExclusion = Some(form.methodExclusion),
      explanation = Some(form.explanation),
      effectiveEndDate = form.expiryDate
    )

}
