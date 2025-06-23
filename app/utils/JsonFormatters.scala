/*
 * Copyright 2025 HM Revenue & Customs
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

package utils

import cats.data.NonEmptySeq
import models.LiabilityStatus.LiabilityStatus
import models._
import models.reporting._
import models.request.NewEventRequest
import models.response.{FileMetadata, ScanStatus}
import play.api.libs.json._

import scala.util.Try

object JsonFormatters {
  implicit def formatNonEmptySeq[A: Format]: Format[NonEmptySeq[A]] = Format(
    Reads.list[A].filter(JsonValidationError("error.empty"))(_.nonEmpty).map(NonEmptySeq.fromSeqUnsafe(_)),
    Writes.seq[A].contramap(_.toSeq)
  )

  implicit val role: Format[Role.Value]                           = EnumJson.format(Role)
  implicit val liabilityStatus: Format[LiabilityStatus]           = EnumJson.format(LiabilityStatus)
  implicit val formatReferralReason: Format[ReferralReason.Value] = EnumJson.format(ReferralReason)
  implicit val rejectedReason: Format[RejectReason.Value]         = EnumJson.format(RejectReason)
  implicit val miscCaseType: Format[MiscCaseType.Value]           = EnumJson.format(MiscCaseType)
  implicit val formatApplicationType: Format[ApplicationType] = Format(
    Reads.of[String].filter(ApplicationType.values.map(_.name).contains(_)).map(ApplicationType.withName),
    Writes.of[String].contramap(_.name)
  )
  implicit val formatPermission: Format[Permission] = Format[Permission](
    Reads(json =>
      json
        .validate[JsString]
        .flatMap(js =>
          Permission.from(js.value) match {
            case Some(permission: Permission) => JsSuccess(permission)
            case _                            => JsError()
          }
        )
    ),
    Writes[Permission](v => JsString(v.toString))
  )

  implicit val instantRange: Format[InstantRange]                        = Json.format[InstantRange]
  implicit val formatRepaymentClaim: OFormat[RepaymentClaim]             = Json.format[RepaymentClaim]
  implicit val formatAddress: OFormat[Address]                           = Json.format[Address]
  implicit val formatTraderContactDetails: OFormat[TraderContactDetails] = Json.format[TraderContactDetails]
  implicit val operatorFormat: Format[Operator]              = Json.using[Json.WithDefaultValues].format[Operator]
  implicit val formatNewUserRequest: OFormat[NewUserRequest] = Json.using[Json.WithDefaultValues].format[NewUserRequest]
  implicit val scanStatusFormat: Format[ScanStatus.Value]    = EnumJson.format(ScanStatus)
  implicit val appealStatusFormat: Format[AppealStatus.Value] = EnumJson.format(AppealStatus)
  implicit val sampleStatusFormat: Format[SampleStatus.Value] = EnumJson.format(SampleStatus)
  implicit val sampleReturnFormat: Format[SampleReturn.Value] = EnumJson.format(SampleReturn)
  implicit val sampleSendFormat: Format[SampleSend.Value]     = EnumJson.format(SampleSend)
  implicit val appealTypeFormat: Format[AppealType.Value]     = EnumJson.format(AppealType)
  implicit val cancelReasonFormat: Format[CancelReason.Value] = EnumJson.format(CancelReason)
  implicit val caseStatusFormat: Format[CaseStatus.Value]     = EnumJson.format(CaseStatus)
  implicit val attachmentFormat: OFormat[Attachment]          = Json.using[Json.WithDefaultValues].format[Attachment]
  implicit val appealFormat: OFormat[Appeal]                  = Json.format[Appeal]
  implicit val cancellationFormat: OFormat[Cancellation]      = Json.using[Json.WithDefaultValues].format[Cancellation]
  implicit val contactFormat: OFormat[Contact]                = Json.format[Contact]
  implicit val eoriDetailsFormat: OFormat[EORIDetails]        = Json.format[EORIDetails]
  implicit val decisionFormat: OFormat[Decision]              = Json.using[Json.WithDefaultValues].format[Decision]
  implicit val sampleFormat: OFormat[Sample]                  = Json.format[Sample]
  implicit val agentDetailsFormat: OFormat[AgentDetails]      = Json.format[AgentDetails]
  implicit val messageLoggedFormat: OFormat[Message]          = Json.format[Message]
  implicit val keywordFormat: OFormat[Keyword]                = Json.format[Keyword]
  implicit val liabilityOrderFormat: OFormat[LiabilityOrder]  = Json.format[LiabilityOrder]
  implicit val correspondenceFormat: OFormat[CorrespondenceApplication] = Json.format[CorrespondenceApplication]
  implicit val miscFormat: OFormat[MiscApplication]                     = Json.format[MiscApplication]
  implicit val btiApplicationFormat: OFormat[BTIApplication] = Json.using[Json.WithDefaultValues].format[BTIApplication]
  implicit val applicationFormat: Format[Application] = Union
    .from[Application]("type")
    .and[BTIApplication](ApplicationType.ATAR.name)
    .and[LiabilityOrder](ApplicationType.LIABILITY.name)
    .and[CorrespondenceApplication](ApplicationType.CORRESPONDENCE.name)
    .and[MiscApplication](ApplicationType.MISCELLANEOUS.name)
    .format

  implicit val caseFormat: OFormat[Case]                         = Json.using[Json.WithDefaultValues].format[Case]
  implicit val newCaseFormat: OFormat[NewCaseRequest]            = Json.format[NewCaseRequest]
  implicit val newKeywordFormat: OFormat[NewKeywordRequest]      = Json.format[NewKeywordRequest]
  implicit val formatCaseStatusChange: OFormat[CaseStatusChange] = Json.format[CaseStatusChange]
  implicit val formatCancellationCaseStatusChange: OFormat[CancellationCaseStatusChange] =
    Json.format[CancellationCaseStatusChange]
  implicit val formatReferralCaseStatusChange: OFormat[ReferralCaseStatusChange] = Json.format[ReferralCaseStatusChange]
  implicit val formatRejectCaseStatusChange: OFormat[RejectCaseStatusChange]     = Json.format[RejectCaseStatusChange]
  implicit val formatCompletedCaseStatusChange: OFormat[CompletedCaseStatusChange] =
    Json.format[CompletedCaseStatusChange]
  implicit val formatAppealStatusChange: OFormat[AppealStatusChange]           = Json.format[AppealStatusChange]
  implicit val formatSampleStatusChange: OFormat[SampleStatusChange]           = Json.format[SampleStatusChange]
  implicit val formatSampleReturnChange: OFormat[SampleReturnChange]           = Json.format[SampleReturnChange]
  implicit val formatSampleSendChange: OFormat[SampleSendChange]               = Json.format[SampleSendChange]
  implicit val formatAppealAdded: OFormat[AppealAdded]                         = Json.format[AppealAdded]
  implicit val formatExtendedUseStatusChange: OFormat[ExtendedUseStatusChange] = Json.format[ExtendedUseStatusChange]
  implicit val formatAssignmentChange: OFormat[AssignmentChange]               = Json.format[AssignmentChange]
  implicit val formatQueueChange: OFormat[QueueChange]                         = Json.format[QueueChange]
  implicit val formatCaseCreated: OFormat[CaseCreated]                         = Json.format[CaseCreated]
  implicit val formatExpertAdviceReceived: OFormat[ExpertAdviceReceived]       = Json.format[ExpertAdviceReceived]
  implicit val formatNote: OFormat[Note]                                       = Json.format[Note]
  implicit val fileMetaDataFormat: OFormat[FileMetadata]                       = Json.format[FileMetadata]
  implicit val formatEventDetail: Format[Details] = Union
    .from[Details]("type")
    .and[CaseStatusChange](EventType.CASE_STATUS_CHANGE.toString)
    .and[CancellationCaseStatusChange](EventType.CASE_CANCELLATION.toString)
    .and[ReferralCaseStatusChange](EventType.CASE_REFERRAL.toString)
    .and[RejectCaseStatusChange](EventType.CASE_REJECTED.toString)
    .and[CompletedCaseStatusChange](EventType.CASE_COMPLETED.toString)
    .and[AppealStatusChange](EventType.APPEAL_STATUS_CHANGE.toString)
    .and[SampleStatusChange](EventType.SAMPLE_STATUS_CHANGE.toString)
    .and[SampleReturnChange](EventType.SAMPLE_RETURN_CHANGE.toString)
    .and[SampleSendChange](EventType.SAMPLE_SEND_CHANGE.toString)
    .and[AppealAdded](EventType.APPEAL_ADDED.toString)
    .and[ExtendedUseStatusChange](EventType.EXTENDED_USE_STATUS_CHANGE.toString)
    .and[AssignmentChange](EventType.ASSIGNMENT_CHANGE.toString)
    .and[QueueChange](EventType.QUEUE_CHANGE.toString)
    .and[Note](EventType.NOTE.toString)
    .and[CaseCreated](EventType.CASE_CREATED.toString)
    .and[ExpertAdviceReceived](EventType.EXPERT_ADVICE_RECEIVED.toString)
    .format

  implicit val eventFormat: OFormat[Event] = Json.using[Json.WithDefaultValues].format[Event]
  implicit val newEventRequestFormat: OFormat[NewEventRequest] =
    Json.using[Json.WithDefaultValues].format[NewEventRequest]

  implicit val formatCaseHeader: OFormat[CaseHeader]                 = Json.format[CaseHeader]
  implicit val formatCaseKeyword: OFormat[CaseKeyword]               = Json.format[CaseKeyword]
  implicit val formatManageKeywordsData: OFormat[ManageKeywordsData] = Json.format[ManageKeywordsData]
  implicit val emailCompleteParamsFormat: OFormat[CaseCompletedEmailParameters] =
    Json.format[CaseCompletedEmailParameters]
  implicit val emailCompleteFormat: OFormat[CaseCompletedEmail] = Json.format[CaseCompletedEmail]
  implicit val emailFormat: Format[Email[?]] = Union
    .from[Email[?]]("templateId")
    .and[CaseCompletedEmail](EmailType.COMPLETE.toString)
    .format
  implicit val emailTemplateFormat: OFormat[EmailTemplate] = Json.format[EmailTemplate]

  implicit val formatPseudoCaseStatus: Format[PseudoCaseStatus.Value] = EnumJson.format(PseudoCaseStatus)

  implicit val formatNumberField: OFormat[NumberField]                   = Json.format[NumberField]
  implicit val formatStatusField: OFormat[StatusField]                   = Json.format[StatusField]
  implicit val formatLiabilityStatusField: OFormat[LiabilityStatusField] = Json.format[LiabilityStatusField]
  implicit val formatCaseTypeField: OFormat[CaseTypeField]               = Json.format[CaseTypeField]
  implicit val formatChapterField: OFormat[ChapterField]                 = Json.format[ChapterField]
  implicit val formatDateField: OFormat[DateField]                       = Json.format[DateField]
  implicit val formatStringField: OFormat[StringField]                   = Json.format[StringField]
  implicit val formatDaysSinceField: OFormat[DaysSinceField]             = Json.format[DaysSinceField]

  implicit val formatReportField: Format[ReportField[?]] = Union
    .from[ReportField[?]]("type")
    .and[NumberField](ReportFieldType.Number.name)
    .and[StatusField](ReportFieldType.Status.name)
    .and[LiabilityStatusField](ReportFieldType.LiabilityStatus.name)
    .and[CaseTypeField](ReportFieldType.CaseType.name)
    .and[ChapterField](ReportFieldType.Chapter.name)
    .and[DateField](ReportFieldType.Date.name)
    .and[StringField](ReportFieldType.String.name)
    .and[DaysSinceField](ReportFieldType.DaysSince.name)
    .format

  implicit val formatNumberResultField: OFormat[NumberResultField] = Json.format[NumberResultField]
  implicit val formatStatusResultField: OFormat[StatusResultField] = Json.format[StatusResultField]
  implicit val formatLiabilityStatusResultField: OFormat[LiabilityStatusResultField] =
    Json.format[LiabilityStatusResultField]
  implicit val formatCaseTypeResultField: OFormat[CaseTypeResultField] = Json.format[CaseTypeResultField]
  implicit val formatDateResultField: OFormat[DateResultField]         = Json.format[DateResultField]
  implicit val formatStringResultField: OFormat[StringResultField]     = Json.format[StringResultField]

  implicit val formatReportResultField: Format[ReportResultField[?]] = Union
    .from[ReportResultField[?]]("type")
    .and[NumberResultField](ReportFieldType.Number.name)
    .and[StatusResultField](ReportFieldType.Status.name)
    .and[LiabilityStatusResultField](ReportFieldType.LiabilityStatus.name)
    .and[CaseTypeResultField](ReportFieldType.CaseType.name)
    .and[DateResultField](ReportFieldType.Date.name)
    .and[StringResultField](ReportFieldType.String.name)
    .format

  // Add this before the formatSimpleResultGroup and formatCaseResultGroup declarations
  implicit val numberResultFieldListWrites: Writes[List[NumberResultField]] =
    Writes.list[NumberResultField].contramap(identity)

  implicit val formatSimpleResultGroup: OFormat[SimpleResultGroup] = Json.format[SimpleResultGroup]
  implicit val formatCaseResultGroup: OFormat[CaseResultGroup]     = Json.format[CaseResultGroup]

  implicit val readResultGroup: Reads[ResultGroup] =
    (__ \ "cases").readNullable[List[Case]].flatMap {
      case Some(_) => formatCaseResultGroup.widen[ResultGroup]
      case None    => formatSimpleResultGroup.widen[ResultGroup]
    }

  implicit val writeResultGroup: OWrites[ResultGroup] = OWrites[ResultGroup] {
    case caseResult: CaseResultGroup     => formatCaseResultGroup.writes(caseResult)
    case simpleResult: SimpleResultGroup => formatSimpleResultGroup.writes(simpleResult)
  }

  implicit val formatResultGroup: OFormat[ResultGroup] = OFormat(readResultGroup, writeResultGroup)

  implicit val formatQueueResultGroup: OFormat[QueueResultGroup] = Json.format[QueueResultGroup]

  implicit val formatCaseReferral: OFormat[CaseReferral]             = Json.format[CaseReferral]
  implicit val formatCaseRejection: OFormat[CaseRejection]           = Json.format[CaseRejection]
  implicit val formatRulingCancellation: OFormat[RulingCancellation] = Json.format[RulingCancellation]
}

object EnumJson {

  import scala.language.implicitConversions

  private def enumReads[E <: Enumeration](customEnum: E): Reads[customEnum.Value] = {
    case JsString(s) =>
      Try(JsSuccess(customEnum.withName(s))).recover { case _: NoSuchElementException =>
        JsError(
          s"Expected an enumeration of type: '${customEnum.getClass.getSimpleName}', but it does not contain the name: '$s'"
        )
      }.get

    case _ => JsError("String value is expected")
  }

  implicit def enumWrites[E <: Enumeration, V <: Enumeration#Value]: Writes[V] =
    Writes((v: V) => JsString(v.toString))

  implicit def format[E <: Enumeration](customEnum: E): Format[customEnum.Value] =
    Format(enumReads(customEnum), enumWrites[E, customEnum.Value])

}
