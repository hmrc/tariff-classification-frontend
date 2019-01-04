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
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditPayloadType._
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Operator, Queue}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AuditService @Inject()(auditConnector: DefaultAuditConnector) {

  def auditCaseReleased(oldCase: Case, updatedCase: Case, queue: Queue, operator: Operator)
                       (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(CaseReleased, Map(
      "reference" -> updatedCase.reference,
      "newStatus" -> updatedCase.status.toString,
      "previousStatus" -> oldCase.status.toString,
      "queue" -> queue.name,
      "operatorId" -> operator.id
    ))
  }

  def auditCaseCompleted(oldCase: Case, updatedCase: Case, operator: Operator)
                        (implicit hc: HeaderCarrier): Unit = {
    sendExplicitAuditEvent(CaseCompleted, Map(
      "reference" -> updatedCase.reference,
      "newStatus" -> updatedCase.status.toString,
      "previousStatus" -> oldCase.status.toString,
      "operatorId" -> operator.id
    ))
  }

  private def sendExplicitAuditEvent(auditEventType: String, auditPayload: Map[String, String])
                                    (implicit hc: uk.gov.hmrc.http.HeaderCarrier): Unit = {

    auditConnector.sendExplicitAudit(auditType = auditEventType, detail = auditPayload)
  }

}

object AuditPayloadType {

  val CaseReleased = "CaseReleased"
  val CaseCompleted = "CaseCompleted"
}
