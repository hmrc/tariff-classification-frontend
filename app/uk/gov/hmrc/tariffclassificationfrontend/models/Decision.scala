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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.ZonedDateTime

case class Decision
(
  bindingCommodityCode: String,
  effectiveStartDate: ZonedDateTime,
  effectiveEndDate: ZonedDateTime,
  justification: String,
  goodsDescription: String,
  keywords: Seq[String] = Seq.empty,
  methodSearch: Option[String] = None,
  methodExclusion: Option[String] = None,
  methodCommercialDenomination: Option[String] = None,
  appeal: Option[Appeal] = None
)

case class Appeal
(
  reviewStatus: String,
  reviewResult: String
)
