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
package viewmodels.correspondence

import utils.Dates

case class CaseDetailsViewModel(
  caseReference: String,
  summary: String,
  detailedDescription: String,
  caseCreatedDate: String,
  caseBoardsFileNumber: Option[String],
  relatedBTIReferences: List[String]
)

object CaseDetailsViewModel {
  def fromCase(cse: Case): CaseDetailsViewModel = {
    val correspondenceApplication = cse.application.asCorrespondence

    CaseDetailsViewModel(
      cse.reference,
      summary              = correspondenceApplication.summary,
      detailedDescription  = correspondenceApplication.detailedDescription,
      caseCreatedDate      = Dates.format(cse.createdDate),
      caseBoardsFileNumber = cse.caseBoardsFileNumber,
      if (correspondenceApplication.relatedBTIReferences.nonEmpty) {
        correspondenceApplication.relatedBTIReferences
      } else {
        correspondenceApplication.relatedBTIReference.toList
      }
    )
  }
}
