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
package viewmodels.atar

case class SampleTabViewModel(
  caseReference: String,
  sampleToBeProvided: Boolean,
  sampleToBeReturned: Boolean,
  sampleRequestedBy: Option[String],
  sampleReturnStatus: String,
  sampleStatus: String,
  sampleActivity: Paged[Event]
)

object SampleTabViewModel {
  def fromCase(cse: Case, activity: Paged[Event]): SampleTabViewModel = {
    val atarApplication = cse.application.asATAR

    SampleTabViewModel(
      caseReference = cse.reference,
      sampleToBeProvided = atarApplication.sampleToBeProvided,
      sampleToBeReturned = atarApplication.sampleToBeReturned,
      sampleRequestedBy = cse.sample.requestedBy.flatMap(_.name),
      sampleReturnStatus = SampleReturn.format(cse.sample.returnStatus),
      sampleStatus = SampleStatus.format(cse.sample.status),
      sampleActivity = activity
    )
  }
}
