/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.ZonedDateTime

import javax.inject.Singleton
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Decision}


@Singleton
class FormMapper {

  def formToCase(c: Case, decisionForm: DecisionFormData): Case = {

    val decision = c.decision match {
      case Some(d: Decision) => from(d, decisionForm)
      case _ => from(decisionForm)
    }

    val attachments = c.attachments
      .map(att => att.copy(public = decisionForm.attachments.contains(att.url)))

    c.copy(decision = Some(decision), attachments = attachments)
  }

  def caseToDecisionFormData(c: Case): DecisionFormData = {

    val form =  c.decision map {
      case d: Decision =>
        DecisionFormData(
          d.bindingCommodityCode,
          d.goodsDescription,
          d.methodSearch.getOrElse(""),
          d.justification,
          d.methodCommercialDenomination.getOrElse(""),
          d.methodExclusion.getOrElse(""),
          c.attachments.filter(_.public).map(_.url)
        )
    }

    form.getOrElse( DecisionFormData())
  }

  private def from(decision: Decision, form: DecisionFormData): Decision = {
    decision.copy(
      bindingCommodityCode = form.bindingCommodityCode.toString,
      goodsDescription = form.goodsDescription,
      methodSearch = Some(form.methodSearch),
      justification = form.justification,
      methodCommercialDenomination = Some(form.methodCommercialDenomination),
      methodExclusion = Some(form.methodExclusion))
  }

  private def from(form: DecisionFormData): Decision = {
    Decision(
      effectiveStartDate = ZonedDateTime.now(),
      effectiveEndDate = ZonedDateTime.now(),
      bindingCommodityCode = form.bindingCommodityCode.toString,
      goodsDescription = form.goodsDescription,
      methodSearch = Some(form.methodSearch),
      justification = form.justification,
      methodCommercialDenomination = Some(form.methodCommercialDenomination),
      methodExclusion = Some(form.methodExclusion))
  }

}
