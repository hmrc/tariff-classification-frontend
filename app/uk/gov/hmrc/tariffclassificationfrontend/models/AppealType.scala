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

package uk.gov.hmrc.tariffclassificationfrontend.models

object AppealType extends Enumeration {
  def format(value: AppealType): String = value match {
    case REVIEW => "Review"
    case APPEAL_TIER_1 => "Appeal tier 1"
    case APPEAL_TIER_2 => "Appeal tier 2"
    case COURT_OF_APPEALS => "Court of appeals"
    case SUPREME_COURT => "Supreme Court"
  }

  type AppealType = Value
  val REVIEW  = Value(1)
  val APPEAL_TIER_1 = Value(2)
  val APPEAL_TIER_2 = Value(3)
  val COURT_OF_APPEALS = Value(4)
  val SUPREME_COURT = Value(5)
}