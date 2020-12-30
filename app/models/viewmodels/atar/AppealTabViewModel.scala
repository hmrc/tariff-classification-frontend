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
package viewmodels.atar

case class AppealTabViewModel(
  caseReference: String,
  caseStatus: CaseStatus.Value,
  appeals: List[Appeal],
  applicationForExtendedUse: Boolean
)

object AppealTabViewModel {
  def fromCase(cse: Case): Option[AppealTabViewModel] =
    if (Set(CaseStatus.COMPLETED, CaseStatus.CANCELLED).contains(cse.status)) {
      val appeals = cse.decision.map(_.appeal).getOrElse(Seq.empty)
      val extendedUse = cse.decision.flatMap(_.cancellation).exists(_.applicationForExtendedUse)

      Some(AppealTabViewModel(
        caseReference = cse.reference,
        caseStatus = cse.status,
        appeals = appeals.sortBy(_.`type`.id).toList,
        applicationForExtendedUse = extendedUse
      ))
    } else {
      None
    }
}
