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

object SampleStatus extends Enumeration {
  type SampleStatus = Value
  val NONE, AWAITING, MOVED_TO_ACT, MOVED_TO_ELM, SENT_FOR_ANALYSIS, SENT_TO_APPEALS, STORAGE, RETURNED_APPLICANT,
    RETURNED_PORT_OFFICER, RETURNED_COURIER, DESTROYED = Value

  def format(status: Option[SampleStatus], initialCaps: Boolean = true): String = {
    val text = status match {
      case Some(AWAITING)              => "awaiting sample"
      case Some(MOVED_TO_ACT)          => "moved to ACT"
      case Some(MOVED_TO_ELM)          => "moved to ELM"
      case Some(SENT_FOR_ANALYSIS)     => "sent for analysis"
      case Some(SENT_TO_APPEALS)       => "sent to review and appeals team"
      case Some(STORAGE)               => "moved to storage"
      case Some(RETURNED_APPLICANT)    => "returned to applicant"
      case Some(RETURNED_PORT_OFFICER) => "returned to port officer"
      case Some(RETURNED_COURIER)      => "returned by courier"
      case Some(DESTROYED)             => "destroyed"
      case _                           => "none"
    }

    if (initialCaps) text.capitalize else text
  }

}
