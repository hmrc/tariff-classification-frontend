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
  val NONE, AWAITING, MOVED_TO_ACT, MOVED_TO_ELM, SENT_TO_LAB, STORAGE, RETURNED, COURIER, DESTROYED = Value

  def format(status: Option[SampleStatus]): String = {
    status match {
      case Some(NONE) => "None"
      case Some(AWAITING) => "Awaiting sample"
      case Some(MOVED_TO_ACT) => "Moved to ACT"
      case Some(MOVED_TO_ELM) => "Moved to ELM"
      case Some(SENT_TO_LAB) => "Sent to lab"
      case Some(STORAGE) => "Sent to storage"
      case Some(RETURNED) => "Returned to trader"
      case Some(COURIER) => "Returned to courier"
      case Some(DESTROYED) => "Sample destroyed"
      case _ => "None"
    }
  }

}
