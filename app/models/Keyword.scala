/*
 * Copyright 2021 HM Revenue & Customs
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

package models

case class Keyword(
  name: String,
  approved: Boolean = false
)

case class CaseHeader(
                       reference: String,
                       assignee: Option[Operator],
                       team: Option[String],
                       goodsName: Option[String],
                       caseType: AppType.Value,
                       status: CaseStatus.Value
                     )

case class CaseKeyword(keyword: Keyword,
                       cases: List[CaseHeader])

object AppType extends Enumeration {
  type ApplicationType = Value
  val BTI, LIABILITY_ORDER, CORRESPONDENCE, MISCELLANEOUS = Value
}
