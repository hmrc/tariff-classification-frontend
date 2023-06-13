/*
 * Copyright 2023 HM Revenue & Customs
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

package audit

import models.AppealStatus.AppealStatus
import models.ApplicationType.{CORRESPONDENCE, MISCELLANEOUS}
import models.ChangeKeywordStatusAction.ChangeKeywordStatusAction
import models._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.DefaultAuditConnector
import utils.JsonFormatters.{caseFormat, operatorFormat}

import javax.inject.{Inject, Singleton}
import scala.annotation.unused
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (auditConnector: DefaultAuditConnector)(implicit ec: ExecutionContext) {

  import AuditPayloadType._

  private lazy val undefined = "None"

  def auditCaseReferred(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseReferred,
      auditPayload   = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )

  def auditCaseRejected(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseRejected,
      auditPayload   = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )

  def auditCaseSuspended(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseSuspended,
      auditPayload   = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )

  def auditCaseReOpened(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseReopened,
      auditPayload   = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )

  def auditCaseReleased(oldCase: Case, updatedCase: Case, queue: Queue, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseReleased,
      auditPayload   = statusChangeAuditPayload(oldCase, updatedCase, operator) + ("queue" -> queue.name)
    )

  def auditQueueReassigned(c: Case, operator: Operator, queue: Queue)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = QueueReassigned,
      auditPayload   = baseAuditPayload(c, operator) + ("queue" -> queue.name)
    )

  def auditCaseCompleted(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseCompleted,
      auditPayload   = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )

  def auditCaseSuppressed(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseSuppressed,
      auditPayload   = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )

  def auditOperatorAssigned(c: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseAssigned,
      auditPayload   = baseAuditPayload(c, operator) + ("assigneeId" -> c.assignee.map(_.id).getOrElse("None"))
    )

  def auditRulingCancelled(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = RulingCancelled,
      auditPayload =
        statusChangeAuditPayload(oldCase, updatedCase, operator) + ("cancelReason" -> cancelReason(updatedCase))
    )

  def auditNote(c: Case, note: String, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseNote,
      auditPayload   = baseAuditPayload(c, operator) + ("note" -> note)
    )

  def auditCaseKeywordAdded(c: Case, keyword: String, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseKeywordAdded,
      auditPayload   = keywordAuditPayload(c, keyword, operator)
    )

  def auditCaseKeywordRemoved(c: Case, keyword: String, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseKeywordRemoved,
      auditPayload   = keywordAuditPayload(c, keyword, operator)
    )

  def auditCaseAppealAdded(c: Case, appeal: Appeal, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseAppealAdded,
      auditPayload = baseAuditPayload(c, operator) +
        ("appealType"   -> appeal.`type`.toString) +
        ("appealStatus" -> appeal.status.toString)
    )

  def auditCaseCreated(c: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseCreated,
      auditPayload   = baseAuditPayload(c, operator) + ("comment" -> "Liability case created")
    )

  def auditCaseUpdated(originalCase: Case, updatedCase: Case, operatorUpdating: Operator)(
    implicit hc: HeaderCarrier
  ): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseUpdated,
      auditPayload = Json.obj(
        "originalCase"     -> Json.toJson(originalCase),
        "operatorUpdating" -> operatorUpdating.id,
        "updatedCase"      -> Json.toJson(updatedCase)
      )
    )

  def auditCaseAppealStatusChange(c: Case, appeal: Appeal, newAppealStatus: AppealStatus, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseAppealStatusChange,
      auditPayload = baseAuditPayload(c, operator) +
        ("appealType"           -> appeal.`type`.toString) +
        ("newAppealStatus"      -> newAppealStatus.toString) +
        ("previousAppealStatus" -> appeal.status.toString)
    )

  def auditSampleStatusChange(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseSampleStatusChange,
      auditPayload = baseAuditPayload(updatedCase, operator) +
        ("newSampleStatus"      -> sampleStatus(updatedCase)) +
        ("previousSampleStatus" -> sampleStatus(oldCase))
    )

  def auditSampleReturnChange(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseSampleReturnChange,
      auditPayload = baseAuditPayload(updatedCase, operator) +
        ("newSampleReturn"      -> sampleReturn(updatedCase)) +
        ("previousSampleReturn" -> sampleReturn(oldCase))
    )

  def auditSampleSendChange(oldCase: Case, updatedCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseSampleSendChange,
      auditPayload = baseAuditPayload(updatedCase, operator) +
        ("newSampleSender"      -> sampleSend(updatedCase)) +
        ("previousSampleSender" -> sampleSend(oldCase))
    )

  def auditCaseExtendedUseChange(oldCase: Case, updatedCase: Case, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Unit =
    sendExplicitAuditEvent(
      auditEventType = CaseExtendedUseChange,
      auditPayload = baseAuditPayload(updatedCase, operator) +
        ("newExtendedUseStatus"      -> extendedUseStatus(updatedCase)) +
        ("previousExtendedUseStatus" -> extendedUseStatus(oldCase))
    )

  def auditAddMessage(updatedCase: Case, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Unit = {
    val messageToAudit = updatedCase.application.`type` match {
      case CORRESPONDENCE => updatedCase.application.asCorrespondence.messagesLogged.head.message
      case MISCELLANEOUS  => updatedCase.application.asMisc.messagesLogged.head.message
    }
    sendExplicitAuditEvent(
      auditEventType = CaseMessage,
      auditPayload   = baseAuditPayload(updatedCase, operator) + ("message" -> messageToAudit)
    )
  }

  def auditUserUpdated(original: Operator, updatedOperator: Operator, operatorUpdating: Operator)(
    implicit hc: HeaderCarrier
  ): Unit =
    sendExplicitAuditEvent(
      auditEventType = UserUpdated,
      auditPayload = Json.obj(
        "originalOperator" -> Json.toJson(original),
        "updatedOperator"  -> Json.toJson(updatedOperator),
        "operatorUpdating" -> operatorUpdating.id
      )
    )

  def auditUserDeleted(oldOperator: Operator, operatorUpdating: Operator)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = UserDeleted,
      auditPayload = Map(
        "operatorUpdating" -> operatorUpdating.id,
        "operatorId"       -> oldOperator.id
      )
    )

  def auditUserCaseMoved(
    refs: List[String],
    user: Option[Operator],
    teamId: String,
    originalUserId: String,
    operatorUpdating: String
  )(implicit hc: HeaderCarrier): Unit = {
    val operatorId: String = user.map(_.id).getOrElse("")
    sendExplicitAuditEvent(
      auditEventType = UserCasesMoved,
      auditPayload = Json.obj(
        "operatorId"       -> originalUserId,
        "team"             -> teamId,
        "newOperatorId"    -> operatorId,
        "caseReferences"   -> Json.toJson(refs),
        "operatorUpdating" -> operatorUpdating
      )
    )
  }

  def auditManagerKeywordCreated(user: Operator, keyword: Keyword, keywordStatusAction: ChangeKeywordStatusAction)(
    implicit hc: HeaderCarrier
  ): Unit = {

    val keywordAction = keywordStatusAction match {
      case ChangeKeywordStatusAction.CREATED => ("keywordCreated", ManagerKeywordCreated)
      case ChangeKeywordStatusAction.APPROVE => ("keywordApproved", ManagerKeywordApproved)
      case ChangeKeywordStatusAction.REJECT  => ("keywordRejected", ManagerKeywordRejected)
    }

    sendExplicitAuditEvent(
      auditEventType = keywordAction._2,
      auditPayload = Map(
        "operatorId"           -> user.id,
        s"${keywordAction._1}" -> keyword.name
      )
    )
  }

  def auditManagerKeywordDeleted(user: Operator, keyword: Keyword)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = ManagerKeywordDeleted,
      auditPayload = Map(
        "operatorId"     -> user.id,
        "keywordDeleted" -> keyword.name
      )
    )

  def auditManagerKeywordRenamed(user: Operator, original: Keyword, updated: Keyword)(
    implicit hc: HeaderCarrier
  ): Unit =
    sendExplicitAuditEvent(
      auditEventType = ManagerKeywordRenamed,
      auditPayload = Map(
        "operatorId"      -> user.id,
        "originalKeyword" -> original.name,
        "updatedKeyword"  -> updated.name
      )
    )

  private def statusChangeAuditPayload(oldCase: Case, updatedCase: Case, operator: Operator): Map[String, String] =
    baseAuditPayload(updatedCase, operator) +
      ("newStatus"      -> updatedCase.status.toString) +
      ("previousStatus" -> oldCase.status.toString)

  private def keywordAuditPayload(c: Case, keyword: String, operator: Operator): Map[String, String] =
    baseAuditPayload(c, operator) + ("keyword" -> keyword)

  private def baseAuditPayload(c: Case, operator: Operator): Map[String, String] =
    Map(
      "caseReference" -> c.reference,
      "operatorId"    -> operator.id
    )

  private def sendExplicitAuditEvent(auditEventType: String, auditPayload: Map[String, String])(
    implicit hc: HeaderCarrier
  ): Unit =
    auditConnector.sendExplicitAudit(auditType = auditEventType, detail = auditPayload)

  private def sendExplicitAuditEvent(auditEventType: String, auditPayload: JsObject)(
    implicit hc: HeaderCarrier
  ): Unit =
    auditConnector.sendExplicitAudit(auditType = auditEventType, detail = auditPayload)

  private def cancelReason: Case => String =
    _.decision flatMap (_.cancellation) map (_.reason.toString) getOrElse undefined

  private def extendedUseStatus: Case => String =
    _.decision.flatMap(_.cancellation).exists(_.applicationForExtendedUse).toString

  private def sampleStatus: Case => String =
    _.sample.status map (_.toString) getOrElse undefined

  private def sampleReturn: Case => String =
    _.sample.returnStatus map (_.toString) getOrElse undefined

  private def sampleSend: Case => String =
    _.sample.whoIsSending map (_.toString) getOrElse undefined
}

object AuditPayloadType {

  val CaseCreated            = "caseCreated"
  val CaseUpdated            = "caseUpdated"
  val CaseReleased           = "caseReleased"
  val CaseAssigned           = "caseAssigned"
  val CaseCompleted          = "caseCompleted"
  val CaseReferred           = "caseReferred"
  val CaseReopened           = "caseReopened"
  val CaseRejected           = "caseRejected"
  val CaseSuspended          = "caseSuspended"
  val CaseSuppressed         = "caseSuppressed"
  val QueueReassigned        = "queueReassigned"
  val RulingCancelled        = "rulingCancelled"
  val CaseKeywordAdded       = "caseKeywordAdded"
  val CaseKeywordRemoved     = "caseKeywordRemoved"
  val CaseAppealAdded        = "caseAppealAdded"
  val CaseAppealStatusChange = "caseAppealStatusChange"
  val CaseExtendedUseChange  = "caseExtendedUseChange"
  val CaseNote               = "caseNote"
  val CaseMessage            = "caseMessage"
  val CaseSampleStatusChange = "caseSampleStatusChange"
  val CaseSampleReturnChange = "caseSampleReturnChange"
  val CaseSampleSendChange   = "caseSampleSendChange"
  val UserUpdated            = "userUpdated"
  val UserDeleted            = "userDeleted"
  val UserCasesMoved         = "userCasesMoved"
  val ManagerKeywordCreated  = "managerKeywordCreated"
  val ManagerKeywordRenamed  = "managerKeywordRenamed"
  val ManagerKeywordDeleted  = "managerKeywordDeleted"
  val ManagerKeywordApproved = "managerKeywordApproved"
  val ManagerKeywordRejected = "managerKeywordRejected"

}
