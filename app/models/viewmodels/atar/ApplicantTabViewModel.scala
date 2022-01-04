/*
 * Copyright 2022 HM Revenue & Customs
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

case class ApplicantTabViewModel(
  caseReference: String,
  eoriDetails: EORIDetails,
  contact: Contact,
  countryName: String,
  caseBoardsFileNumber: Option[String],
  agentDetails: Option[AgentDetails]
)

object ApplicantTabViewModel {
  def fromCase(cse: Case, countryNames: Map[String, String]) = ApplicantTabViewModel(
    cse.reference,
    cse.application.asATAR.holder,
    cse.application.contact,
    countryNames.get(cse.application.asATAR.holder.country).getOrElse(""),
    caseBoardsFileNumber = cse.caseBoardsFileNumber,
    agentDetails         = cse.application.asATAR.agent
  )
}
