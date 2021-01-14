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
package viewmodels.miscellaneous

import utils.Dates

case class DetailsViewModel(
  caseReference: String,
  caseName: String,
  caseContactName: Option[String],
  caseType: String,
  caseCreatedDate: String,
  detailedDescription: Option[String],
  caseBoardsFileNumber: Option[String]
)

object DetailsViewModel {
  def fromCase(cse: Case): DetailsViewModel = {
    val miscellaneousApplication = cse.application.asMisc

    DetailsViewModel(
      cse.reference,
      caseName             = miscellaneousApplication.name,
      caseContactName      = miscellaneousApplication.contactName,
      caseType             = miscellaneousApplication.caseType.toString,
      caseCreatedDate      = Dates.format(cse.createdDate),
      detailedDescription  = miscellaneousApplication.detailedDescription,
      caseBoardsFileNumber = cse.caseBoardsFileNumber
    )
  }
}
