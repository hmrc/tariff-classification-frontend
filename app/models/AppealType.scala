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

object AppealType extends Enumeration {
  def format(value: AppealType): String = value match {
    case ADR              => "Alternative Dispute Resolution (ADR)"
    case REVIEW           => "Review"
    case APPEAL_TIER_1    => "Appeal tier 1"
    case APPEAL_TIER_2    => "Appeal tier 2"
    case COURT_OF_APPEALS => "Court of appeals"
    case SUPREME_COURT    => "Supreme Court"
  }

  def heading(value: AppealType): String = value match {
    case ADR    => "Dispute"
    case REVIEW => "Review"
    case _      => "Appeal"
  }

  /**
    * The order of enum matters as it is used how to show elements in UI in some cases, where it is sorted by ID
    */
  type AppealType = Value
  val ADR, REVIEW, APPEAL_TIER_1, APPEAL_TIER_2, COURT_OF_APPEALS, SUPREME_COURT = Value
}
