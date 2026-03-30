/*
 * Copyright 2025 HM Revenue & Customs
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

import models.AppealType.AppealType

object AppealStatus extends Enumeration {
  type AppealStatus = Value
  val IN_PROGRESS, ALLOWED, DISMISSED = Value

  def format(`type`: AppealType, status: AppealStatus): String = `type` match {
    case AppealType.ADR    => formatDispute(status)
    case AppealType.REVIEW => formatReview(status)
    case _                 => formatAppeal(status)
  }

  def formatAppeal(status: AppealStatus): String = status match {
    case IN_PROGRESS => "Under appeal"
    case ALLOWED     => "Appeal allowed"
    case DISMISSED   => "Appeal dismissed"
  }

  def formatReview(status: AppealStatus): String = status match {
    case IN_PROGRESS => "Under review"
    case ALLOWED     => "Review upheld"
    case DISMISSED   => "Review overturned"
  }

  def formatDispute(status: AppealStatus): String = status match {
    case IN_PROGRESS => "Under mediation"
    case ALLOWED     => "Completed"
    case DISMISSED   => "Completed"
  }

  def validFor(appealType: AppealType): Seq[AppealStatus] = appealType match {
    case AppealType.REVIEW => Seq(AppealStatus.IN_PROGRESS, AppealStatus.ALLOWED, AppealStatus.DISMISSED)
    case AppealType.ADR    => Seq(AppealStatus.IN_PROGRESS, AppealStatus.ALLOWED)
    case _                 => Seq(AppealStatus.IN_PROGRESS, AppealStatus.ALLOWED, AppealStatus.DISMISSED)
  }

}
