/*
 * Copyright 2021 HM Revenue & Customs
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

import models.ApplicationType._
import utils.{Enumerable, WithName}

sealed abstract class CaseStatusRadioInput(name: String, val applicationTypes: Set[ApplicationType])
    extends WithName(name) {

  def validFor(applicationType: ApplicationType): Boolean =
    applicationTypes.contains(applicationType)
}

object CaseStatusRadioInput extends Enumerable.Implicits {

  case object Complete extends CaseStatusRadioInput("complete", ApplicationType.values)
  case object Refer extends CaseStatusRadioInput("refer", ApplicationType.values)
  case object Reject extends CaseStatusRadioInput("reject", Set(ATAR, LIABILITY))
  case object Suspend extends CaseStatusRadioInput("suspend", Set(ATAR, LIABILITY))
  case object MoveBackToQueue extends CaseStatusRadioInput("move_back_to_queue", ApplicationType.values)
  case object Release extends CaseStatusRadioInput("release", ApplicationType.values)
  case object Suppress extends CaseStatusRadioInput("suppress", ApplicationType.values)

  case object ApplicationWithdrawn extends CaseStatusRadioInput("application_withdrawn", Set(ATAR, LIABILITY))
  case object ATaRRulingExists extends CaseStatusRadioInput("atar_ruling_already_exists", Set(ATAR, LIABILITY))
  case object DuplicateApplication extends CaseStatusRadioInput("duplicate_application", Set(ATAR, LIABILITY))
  case object NoInfoFromTrader extends CaseStatusRadioInput("no_information_from_trader", Set(ATAR, LIABILITY))
  case object Other extends CaseStatusRadioInput("other", Set(ATAR, LIABILITY))

  private val changeCaseStatusValues: Seq[CaseStatusRadioInput]  = Seq(Complete, Refer, Reject, Suspend, MoveBackToQueue)
  private val rejectedReasonValues: Seq[CaseStatusRadioInput]    = Seq(ApplicationWithdrawn, ATaRRulingExists, DuplicateApplication, NoInfoFromTrader, Other)
  private val releaseOrSuppressValues: Seq[CaseStatusRadioInput] = Seq(Release, Suppress)

  def changeCaseStatusOptionsFor(applicationType: ApplicationType): Seq[InputRadio] =
    changeCaseStatusValues.filter(_.validFor(applicationType)).map { value =>
      InputRadio("change_case_status", value.toString)
    }

  val rejectedReasonOptions: Seq[InputRadio] = rejectedReasonValues.map { value =>
    InputRadio("change_case_status.rejected", value.toString)
  }

  val releaseOrSuppressOptions: Seq[InputRadio] = releaseOrSuppressValues.map { value =>
    InputRadio("change_case_status", value.toString)
  }

  implicit val enumerable: Enumerable[CaseStatusRadioInput] =
    Enumerable((changeCaseStatusValues ++ releaseOrSuppressValues).map(v => v.toString -> v): _*)
}
