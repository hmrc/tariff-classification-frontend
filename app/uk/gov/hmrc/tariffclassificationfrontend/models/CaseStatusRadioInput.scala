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

import play.api.libs.json._
import uk.gov.hmrc.tariffclassificationfrontend.utils.{Enumerable, WithName}
import uk.gov.hmrc.tariffclassificationfrontend.views.forms.components.RadioOption
sealed trait CaseStatusRadioInput

object CaseStatusRadioInput {

  case object Complete extends WithName("complete") with CaseStatusRadioInput
  case object Refer extends WithName("refer") with CaseStatusRadioInput
  case object Reject extends WithName("reject") with CaseStatusRadioInput
  case object Suspend extends WithName("suspend") with CaseStatusRadioInput
  case object MoveBackToQueue extends WithName("move_back_to_queue") with CaseStatusRadioInput

  val values: Set[CaseStatusRadioInput] = Set(Complete, Refer, Reject, Suspend, MoveBackToQueue)

  val options: Set[RadioOption] = values.map{
    value =>
      RadioOption(value.toString, s"change_case_status_${value.toString}")
  }

  implicit val enumerable: Enumerable[CaseStatusRadioInput] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)
}
