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

  def auditCaseReOpen(oldCase: Case, updatedCase: Case, operator: Operator)
                     (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = CaseReopen,
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

  def auditRulingCancelled(oldCase: Case, updatedCase: Case, operator: Operator)
                          (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(
      auditEventType = RulingCancelled,
      auditPayload = statusChangeAuditPayload(oldCase, updatedCase, operator)
    )
  }

  private def statusChangeAuditPayload(oldCase: Case, updatedCase: Case, operator: Operator): Map[String, String] = {
    Map(
      "caseReference" -> updatedCase.reference,
      "newStatus" -> updatedCase.status.toString,
      "previousStatus" -> oldCase.status.toString,
      "operatorId" -> operator.id
    )
  }

  private def sendExplicitAuditEvent(auditEventType: String, auditPayload: Map[String, String])
                                    (implicit hc: HeaderCarrier): Unit = {

    auditConnector.sendExplicitAudit(auditType = auditEventType, detail = auditPayload)
  }

}

object AuditPayloadType {

  val CaseReopen = "caseReopened"
  val CaseReferred = "caseReferred"
  val CaseReleased = "caseReleased"
  val CaseCompleted = "caseCompleted"
  val RulingCancelled = "rulingCancelled"
}
