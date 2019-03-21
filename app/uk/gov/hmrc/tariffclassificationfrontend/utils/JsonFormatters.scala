/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.utils

import play.api.libs.json._
import play.json.extra.Jsonx
import uk.gov.hmrc.play.json.Union
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.response.{FileMetadata, ScanStatus}
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, _}

object JsonFormatters {

  implicit val operator: Format[Operator] = Jsonx.formatCaseClass[Operator]
  implicit val scanStatusFormat: Format[ScanStatus.Value] = EnumJson.format(ScanStatus)
  implicit val appealStatusFormat: Format[AppealStatus.Value] = EnumJson.format(AppealStatus)
  implicit val reviewStatusFormat: Format[ReviewStatus.Value] = EnumJson.format(ReviewStatus)
  implicit val cancelReasonFormat: Format[CancelReason.Value] = EnumJson.format(CancelReason)
  implicit val caseStatusFormat: Format[CaseStatus.Value] = EnumJson.format(CaseStatus)
  implicit val attachmentFormat: OFormat[Attachment] = Json.format[Attachment]
  implicit val appealFormat: OFormat[Appeal] = Json.format[Appeal]
  implicit val reviewFormat: OFormat[Review] = Json.format[Review]
  implicit val cancellationFormat: OFormat[Cancellation] = Json.format[Cancellation]
  implicit val contactFormat: OFormat[Contact] = Json.format[Contact]
  implicit val eoriDetailsFormat: OFormat[EORIDetails] = Json.format[EORIDetails]
  implicit val decisionFormat: OFormat[Decision] = Json.format[Decision]
  implicit val agentDetailsFormat: OFormat[AgentDetails] = Json.format[AgentDetails]
  implicit val liabilityOrderFormat: OFormat[LiabilityOrder] = Json.format[LiabilityOrder]
  implicit val btiApplicationFormat: OFormat[BTIApplication] = Json.format[BTIApplication]
  implicit val applicationFormat: Format[Application] = Union.from[Application]("type")
    .and[BTIApplication]("BTI")
    .and[LiabilityOrder]("LIABILITY_ORDER")
    .format
  implicit val caseFormat: OFormat[Case] = Json.format[Case]
  implicit val formatCaseStatusChange: OFormat[CaseStatusChange] = Json.format[CaseStatusChange]
  implicit val formatAppealStatusChange: OFormat[AppealStatusChange] = Json.format[AppealStatusChange]
  implicit val formatReviewStatusChange: OFormat[ReviewStatusChange] = Json.format[ReviewStatusChange]
  implicit val formatExtendedUseStatusChange: OFormat[ExtendedUseStatusChange] = Json.format[ExtendedUseStatusChange]
  implicit val formatAssignmentChange: OFormat[AssignmentChange] = Json.format[AssignmentChange]
  implicit val formatQueueChange: OFormat[QueueChange] = Json.format[QueueChange]
  implicit val formatNote: OFormat[Note] = Json.format[Note]
  implicit val fileMetaDataFormat: OFormat[FileMetadata] = Json.format[FileMetadata]
  implicit val formatEventDetail: Format[Details] = Union.from[Details]("type")
    .and[CaseStatusChange](EventType.CASE_STATUS_CHANGE.toString)
    .and[AppealStatusChange](EventType.APPEAL_STATUS_CHANGE.toString)
    .and[ReviewStatusChange](EventType.REVIEW_STATUS_CHANGE.toString)
    .and[ExtendedUseStatusChange](EventType.EXTENDED_USE_STATUS_CHANGE.toString)
    .and[AssignmentChange](EventType.ASSIGNMENT_CHANGE.toString)
    .and[QueueChange](EventType.QUEUE_CHANGE.toString)
    .and[Note](EventType.NOTE.toString)
    .format


  implicit val eventFormat: OFormat[Event] = Json.format[Event]
  implicit val newEventRequestFormat: OFormat[NewEventRequest] = Json.format[NewEventRequest]

  implicit val emailCompleteParamsFormat: OFormat[CaseCompletedEmailParameters] = Json.format[CaseCompletedEmailParameters]
  implicit val emailCompleteFormat: OFormat[CaseCompletedEmail] = Json.format[CaseCompletedEmail]
  implicit val emailFormat: Format[Email[_]] = Union.from[Email[_]]("templateId")
    .and[CaseCompletedEmail](EmailType.COMPLETE.toString)
    .format
  implicit val emailTemplateFormat: OFormat[EmailTemplate] = Json.format[EmailTemplate]
}

object EnumJson {

  implicit def format[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(Reads.enumNameReads(enum), Writes.enumNameWrites)
  }

}
