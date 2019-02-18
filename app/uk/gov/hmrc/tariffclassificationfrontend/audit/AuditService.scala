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

package uk.gov.hmrc.tariffclassificationfrontend.audit

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Operator, Queue}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AuditService @Inject()(auditConnector: DefaultAuditConnector) {

  import AuditPayloadType._

  def auditCaseReferred(oldCase: Case, updatedCase: Case, operator: Operator)
                       (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseReferred,
      auditPayload = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )
  }

  def auditCaseRejected(oldCase: Case, updatedCase: Case, operator: Operator)
                       (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseRejected,
      auditPayload = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )
  }

  def auditCaseSuspended(oldCase: Case, updatedCase: Case, operator: Operator)
                        (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseSuspended,
      auditPayload = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )
  }

  def auditCaseReOpened(oldCase: Case, updatedCase: Case, operator: Operator)
                       (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseReopened,
      auditPayload = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )
  }

  def auditCaseReleased(oldCase: Case, updatedCase: Case, queue: Queue, operator: Operator)
                       (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseReleased,
      auditPayload = statusChangeAuditPayload(oldCase, updatedCase, operator) + ("queue" -> queue.name)
    )
  }

  def auditCaseCompleted(oldCase: Case, updatedCase: Case, operator: Operator)
                        (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseCompleted,
      auditPayload = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )
  }

  def auditCaseSuppressed(oldCase: Case, updatedCase: Case, operator: Operator)
                         (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseSuppressed,
      auditPayload = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )
  }

  def auditRulingCancelled(oldCase: Case, updatedCase: Case, operator: Operator)
                          (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = RulingCancelled,
      auditPayload = statusChangeAuditPayload(oldCase, updatedCase, operator)
        + ("cancelReason" -> cancelReason(updatedCase))
    )
  }

  def auditNote(c: Case, note: String, operator: Operator)
               (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseNote,
      auditPayload = baseAuditPayload(c, operator) + ("note" -> note)
    )
  }

  def auditCaseKeywordAdded(c: Case, keyword: String, operator: Operator)
                           (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseKeywordAdded,
      auditPayload = keywordAuditPayload(c, keyword, operator)
    )
  }

  def auditCaseKeywordRemoved(c: Case, keyword: String, operator: Operator)
                             (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseKeywordRemoved,
      auditPayload = keywordAuditPayload(c, keyword, operator)
    )
  }

  def auditCaseAppealChange(oldCase: Case, updatedCase: Case, operator: Operator)
                           (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseAppealChange,
      auditPayload = baseAuditPayload(updatedCase, operator) + (
        "newAppealStatus" -> appealStatus(updatedCase),
        "previousAppealStatus" -> appealStatus(oldCase)
      )
    )
  }

  def auditCaseReviewChange(oldCase: Case, updatedCase: Case, operator: Operator)
                           (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseReviewChange,
      auditPayload = baseAuditPayload(updatedCase, operator) + (
        "newReviewStatus" -> reviewStatus(updatedCase),
        "previousReviewStatus" -> reviewStatus(oldCase)
      )
    )
  }

  def auditCaseExtendedUseChange(oldCase: Case, updatedCase: Case, operator: Operator)
                                (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseExtendedUseChange,
      auditPayload = baseAuditPayload(updatedCase, operator) + (
        "newExtendedUseStatus" -> extendedUseStatus(updatedCase),
        "previousExtendedUseStatus" -> extendedUseStatus(oldCase)
      )
    )
  }

  private def statusChangeAuditPayload(oldCase: Case, updatedCase: Case, operator: Operator): Map[String, String] = {
    baseAuditPayload(updatedCase, operator) + (
      "newStatus" -> updatedCase.status.toString,
      "previousStatus" -> oldCase.status.toString
    )
  }

  private def keywordAuditPayload(c: Case, keyword: String, operator: Operator): Map[String, String] = {
    baseAuditPayload(c, operator) + ("keyword" -> keyword)
  }

  private def baseAuditPayload(c: Case, operator: Operator): Map[String, String] = {
    Map(
      "caseReference" -> c.reference,
      "operatorId" -> operator.id
    )
  }

  private def sendExplicitAuditEvent(auditEventType: String, auditPayload: Map[String, String])
                                    (implicit hc: HeaderCarrier): Unit = {
    auditConnector.sendExplicitAudit(auditType = auditEventType, detail = auditPayload)
  }

  private def appealStatus: Case => String = {
    _.decision.flatMap(_.appeal).map(_.status.toString).getOrElse("None")
  }

  private def reviewStatus: Case => String = {
    _.decision.flatMap(_.review).map(_.status.toString).getOrElse("None")
  }

  private def extendedUseStatus: Case => String = {
    _.decision.flatMap(_.cancellation).exists(_.applicationForExtendedUse).toString
  }

  private def cancelReason: Case => String = {
    _.decision.flatMap(_.cancellation).map(_.reason.toString).getOrElse("None")
  }

}

object AuditPayloadType {

  val CaseNote = "caseNote"

  val CaseKeywordAdded = "caseKeywordAdded"
  val CaseKeywordRemoved = "caseKeywordRemoved"

  val CaseReopened = "caseReopened"
  val CaseReferred = "caseReferred"
  val CaseRejected = "caseRejected"
  val CaseSuspended = "caseSuspended"
  val CaseReleased = "caseReleased"
  val CaseCompleted = "caseCompleted"
  val CaseSuppressed = "caseSuppressed"
  val RulingCancelled = "rulingCancelled"

  val CaseExtendedUseChange = "caseExtendedUseChange"
  val CaseAppealChange = "caseAppealChange"
  val CaseReviewChange = "caseReviewChange"

}
