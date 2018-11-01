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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import javax.inject.Singleton
import java.time.ZonedDateTime
import uk.gov.hmrc.tariffclassificationfrontend.forms.DecisionForm
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Decision}


@Singleton
class FormMapper {

  def mergeForm(c: Case, form: DecisionForm): Case = {

    val decision = c.decision match {
      case Some(d: Decision) => decisionFrom(d, form)
      case _ => emptyDecision
    }

    val attachments = c.attachments
      .map(att => att.copy(public = form.attachments.contains(att.url)))

    c.copy(decision = Some(decision), attachments = attachments)
  }


  private def decisionFrom(decision: Decision, form: DecisionForm): Decision = {
    decision.copy(
      bindingCommodityCode = form.bindingCommodityCode.toString,
      goodsDescription = form.goodsDescription,
      methodSearch = Some(form.methodSearch),
      justification = form.justification,
      methodCommercialDenomination = Some(form.methodCommercialDenomination),
      methodExclusion = Some(form.methodExclusion))
  }

  private def emptyDecision: Decision = {
    Decision(
      bindingCommodityCode = "",
      effectiveStartDate = ZonedDateTime.now(),
      effectiveEndDate = ZonedDateTime.now(),
      justification = "",
      goodsDescription = ""
    )
  }

}
