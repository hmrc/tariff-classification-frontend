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

package models.viewmodels.managementtools

import models.CaseStatus.CaseStatus
import models.{ApplicationType, CaseStatus}

case class Keyword(
  keyword: String,
  name: String,
  goods: String,
  caseType: ApplicationType,
  caseStatus: CaseStatus,
  approved: Boolean
) {
  def isApproved: Boolean = approved.equals(true)
}

//todo replace the dummy stub with queries
object Keywords {
  val keyword1 =
    Keyword("FIDGET SPINNER", "Alex Smith", "Space grade aluminium spinner", ApplicationType.LIABILITY, CaseStatus.REFERRED, true)
  val keyword2 = Keyword("FROZEN DOUGH", "Joy Fluter", "Frozen deepdish pizza", ApplicationType.ATAR, CaseStatus.OPEN, true)
  val keyword3 = Keyword("HOMECOOK KITS", "Graham Dixon", "Make your own frozen fajitas", ApplicationType.ATAR, CaseStatus.REFERRED, false)
  val keyword4 =
    Keyword("PLASTIC STRAW", "Nora Northcott", "Customisable painted straw shapes", ApplicationType.ATAR, CaseStatus.OPEN, false)
  val keyword5 = Keyword("POGS", "Alex Smith", "Cardboard and plastic boardgame", ApplicationType.ATAR, CaseStatus.OPEN, false)
  val keyword6 = Keyword("SELFIE STICK", "Adam Jones", "Telescopic camera holder", ApplicationType.ATAR, CaseStatus.OPEN, true)
  val keyword7 = Keyword("SPICES", "Alex Smith", "Basmati rice", ApplicationType.ATAR, CaseStatus.OPEN, false)
  val keyword8 = Keyword("VINYL TOY", "Art Mbabasi", "Limited edition painted art toy", ApplicationType.ATAR, CaseStatus.OPEN, false)

  def allKeywords: List[Keyword] = List(keyword1, keyword2, keyword3, keyword4, keyword5, keyword6, keyword7, keyword8)

}
