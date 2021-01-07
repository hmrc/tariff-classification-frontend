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

package views.forms.components

import models.ApplicationType
import models.ApplicationType.{ATAR, CORRESPONDENCE, LIABILITY, MISCELLANEOUS}
import play.twirl.api.Html

case class RadioOption(
  value: String,
  label: String,
  dataTarget: Option[String]             = None,
  customHtml: Option[Html]               = None,
  applicationTypes: Set[ApplicationType] = ApplicationType.values
) {

  def validFor(applicationType: ApplicationType): Boolean =
    applicationTypes.contains(applicationType)
}
//object RadioOption {
//
//  val applicant  = RadioOption("Applicant (the main contact for this case)", "", None, None, Set(ATAR, LIABILITY))
//  val labAnalyst = RadioOption("Laboratory analyst", "", None, None, ApplicationType.values)
//  val dtu        = RadioOption("DTU", "", None, None, Set(CORRESPONDENCE, MISCELLANEOUS))
//  val ib         = RadioOption("IB", "", None, None, Set(CORRESPONDENCE, MISCELLANEOUS))
//  val maff       = RadioOption("MAFF", "", None, None, Set(CORRESPONDENCE, MISCELLANEOUS))
//  val ogd        = RadioOption("OGD", "", None, None, Set(CORRESPONDENCE, MISCELLANEOUS))
//  val otherCe    = RadioOption("Other C&E", "", None, None, Set(CORRESPONDENCE, MISCELLANEOUS))
//  val trader     = RadioOption("Trader", "", None, None, Set(CORRESPONDENCE, MISCELLANEOUS))
//  val other      = RadioOption("Other", "", None, None, ApplicationType.values)
//
//  private val changeCaseStatusReferredValues: Seq[RadioOption] =
//    Seq(applicant, dtu, ib, labAnalyst, maff, ogd, otherCe, trader, other)
//
//  def changeCaseStatusReferredOptionsFor(applicationType: ApplicationType): Seq[RadioOption] =
//    changeCaseStatusReferredValues.filter(_.validFor(applicationType))
//
//}
