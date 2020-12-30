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

package models.viewmodels

import models._

case class CasesTab(tabMessageKey: String, elementId: String, searchResult: Paged[Case])

case class CasesTabViewModel(headingMessageKey: String, caseType: ApplicationType, casesTabs: List[CasesTab])

object CasesTabViewModel {
  def forApplicationType(applicationType: ApplicationType, queuesForType: List[Queue], allQueueCases: Seq[Case]) = {
    val matchingCases = allQueueCases.filter(_.application.`type` == applicationType)
    CasesTabViewModel(
      s"cases.opencases.${applicationType.prettyName.toLowerCase}.heading",
      applicationType,
      queuesForType.map { queue =>
        CasesTab(
          s"cases.opencases.tab_${queue.slug.toUpperCase}",
          s"${queue.slug}_tab",
          Paged(matchingCases.filter(_.queueId.contains(queue.id)))
        )
      }
    )
  }
}
