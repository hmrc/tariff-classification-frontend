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

import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus.{ALLOWED, DISMISSED, IN_PROGRESS}

object CancelReason extends Enumeration {
  type CancelReason = Value

  val ANNULLED, INVALIDATED_CODE_CHANGE, INVALIDATED_EU_MEASURE, INVALIDATED_NATIONAL_MEASURE,
  INVALIDATED_WRONG_CLASSIFICATION, INVALIDATED_OTHER = Value

  def format(reason: Option[CancelReason]): String = {
    reason match {
      case Some(ANNULLED) => "Annulled (55)"
      case Some(INVALIDATED_CODE_CHANGE) => "Invalidated due to nomenclature code changes (61)"
      case Some(INVALIDATED_EU_MEASURE) => "Invalidated due to EU measure (62)"
      case Some(INVALIDATED_NATIONAL_MEASURE) => "Invalidated due to national legal measure (63)"
      case Some(INVALIDATED_WRONG_CLASSIFICATION) => "Invalidated due to incorrect classification (64)"
      case Some(INVALIDATED_OTHER) => "Invalidated due to other reasons (65)"
      case Some(r) => throw new IllegalArgumentException(s"Unexpected reason: $r")
      case None => "None"
    }
  }
}