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
import uk.gov.hmrc.tariffclassificationfrontend.models.LiabilityStatus.LiabilityStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models.Role.Role
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.response.{FileMetadata, ScanStatus}
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, LiabilityStatus, _}

object JsonFormatters {

  case class Something(value: String)

  implicit val role: Format[Role] = EnumJson.format(Role)
  implicit val liabilityStatus: Format[LiabilityStatus] = EnumJson.format(LiabilityStatus)
  implicit val permission: Format[Permission] = EnumJson.format(Permission)
  implicit val formatReferralReason: Format[ReferralReason.Value] = EnumJson.format(ReferralReason)
  implicit val reportField: Format[CaseReportField.Value] = EnumJson.format(CaseReportField)
  implicit val reportGroup: Format[CaseReportGroup.Value] = EnumJson.format(CaseReportGroup)
  implicit val importExportFormat: Format[ImportExport.Value] = EnumJson.format(ImportExport)

  implicit val formatReportResultMap: OFormat[Map[CaseReportGroup.Value, Option[String]]] = {
    implicit val optrds: Reads[Option[String]] = Reads.optionNoError[String]
    EnumJson.formatMap[CaseReportGroup.Value, Option[String]]
  }

  implicit val reportResult: Format[ReportResult] = Json.format[ReportResult]



  implicit val instantRange: Format[InstantRange] = Json.format[InstantRange]
  implicit val caseReportFilter: Format[CaseReportFilter] = Json.format[CaseReportFilter]
  implicit val caseReport: Format[CaseReport] = Json.format[CaseReport]
  implicit val operator: Format[Operator] = Jsonx.formatCaseClass[Operator]
  implicit val scanStatusFormat: Format[ScanStatus.Value] = EnumJson.format(ScanStatus)
  implicit val appealStatusFormat: Format[AppealStatus.Value] = EnumJson.format(AppealStatus)
  implicit val sampleStatusFormat: Format[SampleStatus.Value] = EnumJson.format(SampleStatus)
  implicit val sampleReturnFormat: Format[SampleReturn.Value] = EnumJson.format(SampleReturn)
  implicit val appealTypeFormat: Format[AppealType.Value] = EnumJson.format(AppealType)
  implicit val cancelReasonFormat: Format[CancelReason.Value] = EnumJson.format(CancelReason)
  implicit val caseStatusFormat: Format[CaseStatus.Value] = EnumJson.format(CaseStatus)
  implicit val attachmentFormat: OFormat[Attachment] = Json.format[Attachment]
  implicit val appealFormat: OFormat[Appeal] = Json.format[Appeal]
  implicit val cancellationFormat: OFormat[Cancellation] = Json.format[Cancellation]
  implicit val contactFormat: OFormat[Contact] = Json.format[Contact]
  implicit val eoriDetailsFormat: OFormat[EORIDetails] = Json.format[EORIDetails]
  implicit val decisionFormat: OFormat[Decision] = Json.format[Decision]
  implicit val sampleFormat: OFormat[Sample] = Json.format[Sample]
  implicit val agentDetailsFormat: OFormat[AgentDetails] = Json.format[AgentDetails]
  implicit val liabilityOrderFormat: OFormat[LiabilityOrder] = Json.format[LiabilityOrder]
  implicit val btiApplicationFormat: OFormat[BTIApplication] = Json.format[BTIApplication]
  implicit val applicationFormat: Format[Application] = Union.from[Application]("type")
    .and[BTIApplication](ApplicationType.BTI.toString)
    .and[LiabilityOrder](ApplicationType.LIABILITY_ORDER.toString)
    .format

  implicit val caseFormat: OFormat[Case] = Json.format[Case]
  implicit val newCaseFormat: OFormat[NewCaseRequest] = Json.format[NewCaseRequest]
  implicit val formatCaseStatusChange: OFormat[CaseStatusChange] = Json.format[CaseStatusChange]
  implicit val formatCancellationCaseStatusChange: OFormat[CancellationCaseStatusChange] = Json.format[CancellationCaseStatusChange]
  implicit val formatReferralCaseStatusChange: OFormat[ReferralCaseStatusChange] = Json.format[ReferralCaseStatusChange]
  implicit val formatCompletedCaseStatusChange: OFormat[CompletedCaseStatusChange] = Json.format[CompletedCaseStatusChange]
  implicit val formatAppealStatusChange: OFormat[AppealStatusChange] = Json.format[AppealStatusChange]
  implicit val formatSampleStatusChange: OFormat[SampleStatusChange] = Json.format[SampleStatusChange]
  implicit val formatSampleReturnChange: OFormat[SampleReturnChange] = Json.format[SampleReturnChange]
  implicit val formatAppealAdded: OFormat[AppealAdded] = Json.format[AppealAdded]
  implicit val formatExtendedUseStatusChange: OFormat[ExtendedUseStatusChange] = Json.format[ExtendedUseStatusChange]
  implicit val formatAssignmentChange: OFormat[AssignmentChange] = Json.format[AssignmentChange]
  implicit val formatQueueChange: OFormat[QueueChange] = Json.format[QueueChange]
  implicit val formatNote: OFormat[Note] = Json.format[Note]
  implicit val fileMetaDataFormat: OFormat[FileMetadata] = Json.format[FileMetadata]
  implicit val formatEventDetail: Format[Details] = Union.from[Details]("type")
    .and[CaseStatusChange](EventType.CASE_STATUS_CHANGE.toString)
    .and[CancellationCaseStatusChange](EventType.CASE_CANCELLATION.toString)
    .and[ReferralCaseStatusChange](EventType.CASE_REFERRAL.toString)
    .and[CompletedCaseStatusChange](EventType.CASE_COMPLETED.toString)
    .and[AppealStatusChange](EventType.APPEAL_STATUS_CHANGE.toString)
    .and[SampleStatusChange](EventType.SAMPLE_STATUS_CHANGE.toString)
    .and[SampleReturnChange](EventType.SAMPLE_RETURN_CHANGE.toString)
    .and[AppealAdded](EventType.APPEAL_ADDED.toString)
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

  def readsMap[E, B](implicit erds: Reads[E], brds: Reads[B]): JsValue => JsResult[Map[E, B]] = (js: JsValue) => {
    val maprds: Reads[Map[String, B]] = Reads.mapReads[B]
    Json.fromJson[Map[String, B]](js)(maprds).map(_.map {
      case (key: String, value: B) => erds.reads(JsString(key)).get -> value
    })
  }

  def writesMap[E, B](implicit ewrts: Writes[E], bwrts: Writes[B]): Map[E, B] => JsObject = (map: Map[E, B]) =>
    Json.toJson(map.map {
      case (group, value) => group.toString -> value
    }).as[JsObject]

  def formatMap[E, B](implicit efmt: Format[E], bfmt: Format[B]): OFormat[Map[E, B]] = OFormat(
    read = readsMap(efmt, bfmt),
    write = writesMap(efmt, bfmt)
  )

}
