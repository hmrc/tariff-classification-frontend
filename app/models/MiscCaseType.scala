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

object MiscCaseType extends Enumeration {
  type MiscCaseType = Value

  private val APPEALS                           = Value("Appeals")
  val HARMONISED: models.MiscCaseType.Value     = Value("Harmonised systems")
  val IB: models.MiscCaseType.Value             = Value("IB")
  val NOMENCLATURE: models.MiscCaseType.Value   = Value("Nomenclature")
  val OTHER_GOVT_DEP: models.MiscCaseType.Value = Value("Other government dept")
  val OTHER: models.MiscCaseType.Value          = Value("Other")

  def format(caseType: MiscCaseType): String =
    caseType match {
      case APPEALS        => "Appeals"
      case HARMONISED     => "Harmonised System"
      case IB             => "IB"
      case NOMENCLATURE   => "Nomenclature"
      case OTHER_GOVT_DEP => "Other government department"
      case OTHER          => "Other"
    }
}
