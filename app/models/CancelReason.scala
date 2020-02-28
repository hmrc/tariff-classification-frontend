/*
 * Copyright 2020 HM Revenue & Customs
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

object CancelReason extends Enumeration {
  type CancelReason = Value

  val ANNULLED, INVALIDATED_CODE_CHANGE, INVALIDATED_EU_MEASURE, INVALIDATED_NATIONAL_MEASURE,
  INVALIDATED_WRONG_CLASSIFICATION, INVALIDATED_OTHER = Value

  def format(reason: CancelReason): String = {
    reason match {
      case ANNULLED => s"Annulled (${code(ANNULLED)})"
      case INVALIDATED_CODE_CHANGE => s"Invalidated due to nomenclature code changes (${code(INVALIDATED_CODE_CHANGE)})"
      case INVALIDATED_EU_MEASURE => s"Invalidated due to EU measure (${code(INVALIDATED_EU_MEASURE)})"
      case INVALIDATED_NATIONAL_MEASURE => s"Invalidated due to national legal measure (${code(INVALIDATED_NATIONAL_MEASURE)})"
      case INVALIDATED_WRONG_CLASSIFICATION => s"Invalidated due to incorrect classification (${code(INVALIDATED_WRONG_CLASSIFICATION)})"
      case INVALIDATED_OTHER => s"Invalidated due to other reasons (${code(INVALIDATED_OTHER)})"
      case unknown => throw new IllegalArgumentException(s"Unexpected reason: $unknown")
    }
  }

  def code(reason: CancelReason): Int = reason match {
    case ANNULLED => 55
    case INVALIDATED_CODE_CHANGE => 61
    case INVALIDATED_EU_MEASURE => 62
    case INVALIDATED_NATIONAL_MEASURE => 63
    case INVALIDATED_WRONG_CLASSIFICATION => 64
    case INVALIDATED_OTHER => 65
  }
}