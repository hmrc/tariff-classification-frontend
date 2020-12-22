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

case class AttachmentsTabViewModel(
  caseReference: String,
  caseContact: String,
  attachmentsFromApplicant: Seq[StoredAttachment],
  attachmentsFromClassification: Seq[StoredAttachment]
) {
  def allAttachments = attachmentsFromApplicant ++ attachmentsFromClassification
}

object AttachmentsTabViewModel {
  def fromCase(cse: Case, attachments: Seq[StoredAttachment]): AttachmentsTabViewModel = {
    val (fromApplicant, fromClassification) = attachments.partition(_.operator.isEmpty)

    AttachmentsTabViewModel(
      caseReference = cse.reference,
      caseContact = cse.application.contact.name,
      attachmentsFromApplicant = fromApplicant,
      attachmentsFromClassification = fromClassification
    )
  }
}
