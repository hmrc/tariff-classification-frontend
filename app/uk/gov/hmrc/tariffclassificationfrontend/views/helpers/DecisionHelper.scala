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

package uk.gov.hmrc.tariffclassificationfrontend.views.helpers

import uk.gov.hmrc.tariffclassificationfrontend.models.Case

object DecisionHelper {

  def hasValidDecision(c: Case) = {
    hasCommodityCode(c) && hasGoodsDescription(c) && hasJustification(c) && hasMethodSearch(c)
  }

  def hasCommodityCode: Case => Boolean = { c =>
    c.decision.exists(d => hasContent(d.bindingCommodityCode))
  }

  def hasGoodsDescription: Case => Boolean = { c =>
    c.decision.exists(d => hasContent(d.goodsDescription))
  }

  def hasJustification: Case => Boolean = { c =>
    c.decision.exists(d => hasContent(d.justification))
  }

  def hasMethodSearch: Case => Boolean = { c =>
    c.decision.exists(d => hasContent(d.methodSearch.getOrElse("")))
  }

  private def hasContent: String => Boolean = { s => !s.trim.isEmpty }

}
