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

object CancelReason extends Enumeration {
  type CancelReason = Value

  val ANNULLED, INVALIDATED_CODE_CHANGE, INVALIDATED_EU_MEASURE, INVALIDATED_NATIONAL_MEASURE,
    INVALIDATED_WRONG_CLASSIFICATION, INVALIDATED_OTHER, OTHER = Value

  def format(reason: CancelReason): String = {
    val message = reason match {
      case ANNULLED                         => s"Annulled"
      case INVALIDATED_CODE_CHANGE          => s"Invalidated due to nomenclature code changes"
      case INVALIDATED_EU_MEASURE           => s"Invalidated due to EU measure"
      case INVALIDATED_NATIONAL_MEASURE     => s"Invalidated due to national legal measure"
      case INVALIDATED_WRONG_CLASSIFICATION => s"Invalidated due to incorrect classification"
      case INVALIDATED_OTHER | OTHER        => s"Invalidated due to other reasons"
      case unknown: CancelReason            => throw new IllegalArgumentException(s"Unexpected reason: $unknown")
    }

    message + code(reason).map(c => s" ($c)").getOrElse("")
  }

  def code(reason: CancelReason): Option[Int] =
    reason match {
      case ANNULLED                         => Some(55)
      case INVALIDATED_CODE_CHANGE          => Some(61)
      case INVALIDATED_EU_MEASURE           => Some(62)
      case INVALIDATED_NATIONAL_MEASURE     => Some(63)
      case INVALIDATED_WRONG_CLASSIFICATION => Some(64)
      case INVALIDATED_OTHER                => Some(65)
      case OTHER                            => None
      case unknown: CancelReason            => throw new IllegalArgumentException(s"Unexpected reason: $unknown")
    }
}
