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

object SampleStatus extends Enumeration {
  type SampleStatus = Value
  val AWAITING, MOVED_TO_ACT, MOVED_TO_ELM, SENT_FOR_ANALYSIS, SENT_TO_APPEALS, STORAGE, RETURNED_APPLICANT,
  RETURNED_PORT_OFFICER, RETURNED_COURIER, DESTROYED = Value

  def format(status: Option[SampleStatus]): String = {
    status match {
      case Some(AWAITING) => "Awaiting sample"
      case Some(MOVED_TO_ACT) => "Moved to ACT"
      case Some(MOVED_TO_ELM) => "Moved to ELM"
      case Some(SENT_FOR_ANALYSIS) => "Sent for analysis"
      case Some(SENT_TO_APPEALS) => "Sent to review and appeals team"
      case Some(STORAGE) => "Moved to storage"
      case Some(RETURNED_APPLICANT) => "Returned to applicant"
      case Some(RETURNED_PORT_OFFICER) => "Returned to port officer"
      case Some(RETURNED_COURIER) => "Returned by courier"
      case Some(DESTROYED) => "Destroyed"
      case _ => "None"
    }
  }

}
