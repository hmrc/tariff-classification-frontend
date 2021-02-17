/*
 * Copyright 2021 HM Revenue & Customs
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

import base.SpecBase
import models.AppealStatus.AppealStatus
import models.CancelReason.CancelReason
import models.CaseStatus._
import models.{CaseStatus => _, _}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import utils.Cases._

import java.time.Instant
import scala.concurrent.ExecutionContext

class AuditServiceTest extends SpecBase with BeforeAndAfterEach {

  private val connector = mock[DefaultAuditConnector]

  private val service = new AuditService(connector)

  private val operator = Operator("operator-id")

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
  }

  "Service 'audit case created'" should {
    val original = aLiabilityCase(withReference("ref"))

    "Delegate to connector" in {
      service.auditCaseCreated(original, operator)

      val payload = caseCreatedAudit(
        caseReference = "ref",
        operatorId    = operator.id,
        comment       = "Liability case created"
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseCreated"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case assigned'" should {

    "Delegate to connector" in {
      val c = btiCaseExample.copy(reference = "ref", status = OPEN, assignee = Some(Operator("assignee-id")))

      service.auditOperatorAssigned(c, operator)

      val payload = Map[String, String](
        "caseReference" -> "ref",
        "operatorId"    -> operator.id,
        "assigneeId"    -> "assignee-id"
      )

      verify(connector)
        .sendExplicitAudit(refEq("caseAssigned"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }

    "Delegate to connector - when assignee empty" in {
      val c = btiCaseExample.copy(reference = "ref", status = OPEN, assignee = None)

      service.auditOperatorAssigned(c, operator)

      val payload = Map[String, String](
        "caseReference" -> "ref",
        "operatorId"    -> operator.id,
        "assigneeId"    -> "None"
      )

      verify(connector)
        .sendExplicitAudit(refEq("caseAssigned"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case released'" should {
    val original = btiCaseExample.copy(reference = "ref", status = NEW)
    val updated  = btiCaseExample.copy(reference = "ref", status = OPEN)
    val queue    = Queue("queue-id", "queue-slug", "queue-name")

    "Delegate to connector" in {
      service.auditCaseReleased(original, updated, queue, operator)

      val payload = caseChangeAudit(
        caseReference  = "ref",
        newStatus      = OPEN,
        previousStatus = NEW,
        operatorId     = operator.id
      ) + ("queue" -> queue.name)

      verify(connector)
        .sendExplicitAudit(refEq("caseReleased"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit queue assigned'" should {

    val q = Queue("queue-id", "queue-slug", "queue-name")

    "Delegate to connector" in {
      val c = btiCaseExample
        .copy(reference = "ref", status = OPEN, queueId = Some(q.id), assignee = Some(Operator("assignee-id")))

      service.auditQueueReassigned(c, operator, q)

      val payload = Map[String, String](
        "caseReference" -> "ref",
        "operatorId"    -> operator.id,
        "queue"         -> "queue-name"
      )

      verify(connector)
        .sendExplicitAudit(refEq("queueReassigned"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case completed'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated  = btiCaseExample.copy(reference = "ref", status = COMPLETED)

    "Delegate to connector" in {
      service.auditCaseCompleted(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference  = "ref",
        newStatus      = COMPLETED,
        previousStatus = OPEN,
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseCompleted"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit cancel ruling'" should {
    val original = aCase(withReference("ref"), withStatus(COMPLETED))
    val updated = aCase(
      withReference("ref"),
      withStatus(CANCELLED),
      withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED, applicationForExtendedUse = true)))
    )

    "Delegate to connector" in {
      service.auditRulingCancelled(original, updated, operator)

      val payload = caseCancelAudit(
        caseReference = "ref",
        cancelReason  = CancelReason.ANNULLED,
        operatorId    = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("rulingCancelled"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case referred'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated  = btiCaseExample.copy(reference = "ref", status = REFERRED)

    "Delegate to connector" in {
      service.auditCaseReferred(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference  = "ref",
        newStatus      = REFERRED,
        previousStatus = OPEN,
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseReferred"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case rejected'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated  = btiCaseExample.copy(reference = "ref", status = REJECTED)

    "Delegate to connector" in {
      service.auditCaseRejected(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference  = "ref",
        newStatus      = REJECTED,
        previousStatus = OPEN,
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseRejected"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case suspended'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated  = btiCaseExample.copy(reference = "ref", status = SUSPENDED)

    "Delegate to connector" in {
      service.auditCaseSuspended(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference  = "ref",
        newStatus      = SUSPENDED,
        previousStatus = OPEN,
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseSuspended"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case suppressed'" should {
    val original = btiCaseExample.copy(reference = "ref", status = NEW)
    val updated  = btiCaseExample.copy(reference = "ref", status = SUPPRESSED)

    "Delegate to connector" in {
      service.auditCaseSuppressed(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference  = "ref",
        newStatus      = SUPPRESSED,
        previousStatus = NEW,
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseSuppressed"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case reopened' when a case is referred" should {
    val original = btiCaseExample.copy(reference = "ref", status = REFERRED)
    val updated  = btiCaseExample.copy(reference = "ref", status = OPEN)

    "Delegate to connector" in {
      service.auditCaseReOpened(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference  = "ref",
        newStatus      = OPEN,
        previousStatus = REFERRED,
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseReopened"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case reopened' when a case is suspended" should {
    val original = btiCaseExample.copy(reference = "ref", status = SUSPENDED)
    val updated  = btiCaseExample.copy(reference = "ref", status = OPEN)

    "Delegate to connector" in {
      service.auditCaseReOpened(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference  = "ref",
        newStatus      = OPEN,
        previousStatus = SUSPENDED,
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseReopened"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case appeal added'" should {
    val original = aCase(withReference("ref"))
    val appeal   = Appeal(id = "id", status = AppealStatus.IN_PROGRESS, `type` = AppealType.REVIEW)

    "Delegate to connector" in {
      service.auditCaseAppealAdded(original, appeal, operator)

      val payload = appealAddAudit(
        caseReference = "ref",
        appeal        = appeal,
        operatorId    = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseAppealAdded"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case appeal status changed'" should {
    val original  = aCase(withReference("ref"))
    val appeal    = Appeal(id = "id", status = AppealStatus.IN_PROGRESS, `type` = AppealType.REVIEW)
    val newStatus = AppealStatus.DISMISSED

    "Delegate to connector" in {
      service.auditCaseAppealStatusChange(original, appeal, newStatus, operator)

      val payload = appealStatusChangeAudit(
        caseReference = "ref",
        appeal        = appeal,
        newStatus     = newStatus,
        operatorId    = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseAppealStatusChange"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit extended use change'" should {
    val original = aCase(
      withReference("ref"),
      withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED, applicationForExtendedUse = true)))
    )
    val updated  = aCase(withReference("ref"), withoutDecision())
    val operator = Operator("operator-id")

    "Delegate to connector" in {
      service.auditCaseExtendedUseChange(original, updated, operator)

      val payload = extendedUseChangeAudit(
        caseReference  = "ref",
        newStatus      = false,
        previousStatus = true,
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseExtendedUseChange"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit note'" should {
    val c       = btiCaseExample.copy(reference = "ref", status = OPEN)
    val comment = "this is my note"

    "Delegate to connector" in {
      service.auditNote(c, comment, operator)

      val payload = Map(
        "caseReference" -> c.reference,
        "operatorId"    -> operator.id,
        "note"          -> comment
      )
      verify(connector).sendExplicitAudit(refEq("caseNote"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case keyword added'" should {
    val c       = btiCaseExample.copy(reference = "ref", status = COMPLETED)
    val keyword = "PHONE"

    "Delegate to connector" in {
      service.auditCaseKeywordAdded(c, keyword, operator)

      val payload = caseKeywordAudit(c.reference, keyword, operator.id)

      verify(connector)
        .sendExplicitAudit(refEq("caseKeywordAdded"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case keyword removed'" should {
    val c       = btiCaseExample.copy(reference = "ref", status = COMPLETED)
    val keyword = "PHONE"

    "Delegate to connector" in {
      service.auditCaseKeywordRemoved(c, keyword, operator)

      val payload = caseKeywordAudit(c.reference, keyword, operator.id)

      verify(connector)
        .sendExplicitAudit(refEq("caseKeywordRemoved"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit sample status changed'" should {
    val original = aCase(withReference("ref"), withSampleStatus(None))
    val updated  = aCase(withReference("ref"), withSampleStatus(Some(SampleStatus.AWAITING)))
    val operator = Operator("operator-id")

    "Delegate to connector" in {
      service.auditSampleStatusChange(original, updated, operator)

      val payload = sampleStatusChangeAudit(
        caseReference  = "ref",
        newStatus      = "AWAITING",
        previousStatus = "None",
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseSampleStatusChange"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit sample return changed'" should {
    val original = aCase(withReference("ref"))
    val updated  = aCase(withReference("ref"), withSample(Sample(returnStatus = Some(SampleReturn.YES))))
    val operator = Operator("operator-id")

    "Delegate to connector" in {
      service.auditSampleReturnChange(original, updated, operator)

      val payload = sampleReturnChangeAudit(
        caseReference  = "ref",
        newStatus      = "YES",
        previousStatus = "None",
        operatorId     = operator.id
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseSampleReturnChange"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit message'" should {
    val message = "this is my message"
    val corrCase = correspondenceCaseExample.copy(
      reference   = "ref",
      status      = OPEN,
      application = correspondenceExample.copy(messagesLogged = List(Message("operator name", Instant.now, message)))
    )

    val miscCase = miscellaneousCaseExample.copy(
      reference   = "ref",
      status      = OPEN,
      application = miscExample.copy(messagesLogged = List(Message("operator name", Instant.now, message)))
    )

    "Delegate to connector for correspondence" in {
      service.auditAddMessage(corrCase, operator)

      val payload = Map(
        "caseReference" -> corrCase.reference,
        "operatorId"    -> operator.id,
        "message"       -> corrCase.application.asCorrespondence.messagesLogged.reverse.last.message
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseMessage"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }

    "Delegate to connector for miscellaneous" in {
      service.auditAddMessage(miscCase, operator)

      val payload = Map(
        "caseReference" -> miscCase.reference,
        "operatorId"    -> operator.id,
        "message"       -> miscCase.application.asMisc.messagesLogged.reverse.last.message
      )
      verify(connector)
        .sendExplicitAudit(refEq("caseMessage"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit User Updated'" in {
    val operatorToUpdate = Operator("PID")

    service.auditUserUpdated(operatorToUpdate, operator)

    val payload = Map(
      "operatorToUpdate" -> operatorToUpdate.id,
      "operatorUpdating"       -> operator.id
    )
    verify(connector)
      .sendExplicitAudit(refEq("userUpdated"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
  }

  private def caseCreatedAudit(caseReference: String, operatorId: String, comment: String): Map[String, String] =
    Map[String, String](
      "caseReference" -> caseReference,
      "operatorId"    -> operatorId,
      "comment"       -> comment
    )

  private def caseChangeAudit(
    caseReference: String,
    newStatus: CaseStatus,
    previousStatus: CaseStatus,
    operatorId: String
  ): Map[String, String] =
    Map[String, String](
      "caseReference"  -> caseReference,
      "operatorId"     -> operatorId,
      "newStatus"      -> newStatus.toString,
      "previousStatus" -> previousStatus.toString
    )

  private def caseCancelAudit(
    caseReference: String,
    cancelReason: CancelReason,
    operatorId: String
  ): Map[String, String] =
    Map[String, String](
      "caseReference"  -> caseReference,
      "operatorId"     -> operatorId,
      "newStatus"      -> "CANCELLED",
      "previousStatus" -> "COMPLETED",
      "cancelReason"   -> cancelReason.toString
    )

  private def caseKeywordAudit(caseReference: String, keyword: String, operatorId: String): Map[String, String] =
    Map[String, String](
      "caseReference" -> caseReference,
      "operatorId"    -> operatorId,
      "keyword"       -> keyword
    )

  private def appealAddAudit(caseReference: String, appeal: Appeal, operatorId: String): Map[String, String] =
    Map[String, String](
      "caseReference" -> caseReference,
      "operatorId"    -> operatorId,
      "appealType"    -> appeal.`type`.toString,
      "appealStatus"  -> appeal.status.toString
    )

  private def appealStatusChangeAudit(
    caseReference: String,
    appeal: Appeal,
    newStatus: AppealStatus,
    operatorId: String
  ): Map[String, String] =
    Map[String, String](
      "caseReference"        -> caseReference,
      "operatorId"           -> operatorId,
      "appealType"           -> appeal.`type`.toString,
      "previousAppealStatus" -> appeal.status.toString,
      "newAppealStatus"      -> newStatus.toString
    )

  private def extendedUseChangeAudit(
    caseReference: String,
    newStatus: Boolean,
    previousStatus: Boolean,
    operatorId: String
  ): Map[String, String] =
    Map[String, String](
      "caseReference"             -> caseReference,
      "operatorId"                -> operatorId,
      "newExtendedUseStatus"      -> newStatus.toString,
      "previousExtendedUseStatus" -> previousStatus.toString
    )

  private def sampleStatusChangeAudit(
    caseReference: String,
    newStatus: String,
    previousStatus: String,
    operatorId: String
  ): Map[String, String] =
    Map[String, String](
      "caseReference"        -> caseReference,
      "operatorId"           -> operatorId,
      "newSampleStatus"      -> newStatus,
      "previousSampleStatus" -> previousStatus
    )

  private def sampleReturnChangeAudit(
    caseReference: String,
    newStatus: String,
    previousStatus: String,
    operatorId: String
  ): Map[String, String] =
    Map[String, String](
      "caseReference"        -> caseReference,
      "operatorId"           -> operatorId,
      "newSampleReturn"      -> newStatus,
      "previousSampleReturn" -> previousStatus
    )
}
