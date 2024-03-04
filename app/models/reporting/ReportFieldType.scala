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

package models
package reporting

sealed abstract class ReportFieldType(val name: String) extends Product with Serializable

object ReportFieldType {
  case object Number extends ReportFieldType("number")
  case object Status extends ReportFieldType("status")
  case object LiabilityStatus extends ReportFieldType("liability_status")
  case object CaseType extends ReportFieldType("caseType")
  case object Chapter extends ReportFieldType("chapter")
  case object Date extends ReportFieldType("date")
  case object String extends ReportFieldType("string")
  case object DaysSince extends ReportFieldType("daysSince")
}
