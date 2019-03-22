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

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus.AppealStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.CancelReason.CancelReason
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.hmrc.tariffclassificationfrontend.models.ReviewStatus.ReviewStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus => _, _}
import uk.gov.tariffclassificationfrontend.utils.Cases._

import scala.concurrent.ExecutionContext

class AuditServiceTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val connector = mock[DefaultAuditConnector]

  private val service = new AuditService(connector)

  private val operator = Operator("operator-id")

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
  }

  "Service 'audit case assigned'" should {

    "Delegate to connector" in {
      val c = btiCaseExample.copy(reference = "ref", status = OPEN, assignee = Some(Operator("assignee-id")))

      service.auditOperatorAssigned(c, operator)

      val payload = Map[String, String](
        "caseReference" -> "ref",
        "operatorId" -> operator.id,
        "assigneeId" -> "assignee-id"
      )

      verify(connector).sendExplicitAudit(refEq("caseAssigned"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }

    "Delegate to connector - when assignee empty" in {
      val c = btiCaseExample.copy(reference = "ref", status = OPEN, assignee = None)

      service.auditOperatorAssigned(c, operator)

      val payload = Map[String, String](
        "caseReference" -> "ref",
        "operatorId" -> operator.id,
        "assigneeId" -> "None"
      )

      verify(connector).sendExplicitAudit(refEq("caseAssigned"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case released'" should {
    val original = btiCaseExample.copy(reference = "ref", status = NEW)
    val updated = btiCaseExample.copy(reference = "ref", status = OPEN)
    val queue = Queue("queue-id", "queue-slug", "queue-name")

    "Delegate to connector" in {
      service.auditCaseReleased(original, updated, queue, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = OPEN,
        previousStatus = NEW,
        operatorId = operator.id
      ) + ("queue" -> queue.name)

      verify(connector).sendExplicitAudit(refEq("caseReleased"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit queue assigned'" should {

    val q = Queue("queue-id", "queue-slug", "queue-name")

    "Delegate to connector" in {
      val c = btiCaseExample.copy(reference = "ref", status = OPEN, queueId = Some(q.id), assignee = Some(Operator("assignee-id")))

      service.auditQueueReassigned(c, operator, q)

      val payload = Map[String, String](
        "caseReference" -> "ref",
        "operatorId" -> operator.id,
        "queue" -> "queue-name"
      )

      verify(connector).sendExplicitAudit(refEq("queueReassigned"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case completed'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated = btiCaseExample.copy(reference = "ref", status = COMPLETED)

    "Delegate to connector" in {
      service.auditCaseCompleted(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = COMPLETED,
        previousStatus = OPEN,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseCompleted"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit cancel ruling'" should {
    val original = aCase(withReference("ref"), withStatus(COMPLETED))
    val updated = aCase(withReference("ref"), withStatus(CANCELLED),
      withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED, applicationForExtendedUse = true))))

    "Delegate to connector" in {
      service.auditRulingCancelled(original, updated, operator)

      val payload = caseCancelAudit(
        caseReference = "ref",
        cancelReason = CancelReason.ANNULLED,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("rulingCancelled"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case referred'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated = btiCaseExample.copy(reference = "ref", status = REFERRED)

    "Delegate to connector" in {
      service.auditCaseReferred(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = REFERRED,
        previousStatus = OPEN,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseReferred"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case rejected'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated = btiCaseExample.copy(reference = "ref", status = REJECTED)

    "Delegate to connector" in {
      service.auditCaseRejected(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = REJECTED,
        previousStatus = OPEN,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseRejected"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case suspended'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated = btiCaseExample.copy(reference = "ref", status = SUSPENDED)

    "Delegate to connector" in {
      service.auditCaseSuspended(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = SUSPENDED,
        previousStatus = OPEN,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseSuspended"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case suppressed'" should {
    val original = btiCaseExample.copy(reference = "ref", status = NEW)
    val updated = btiCaseExample.copy(reference = "ref", status = SUPPRESSED)

    "Delegate to connector" in {
      service.auditCaseSuppressed(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = SUPPRESSED,
        previousStatus = NEW,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseSuppressed"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case reopened' when a case is referred" should {
    val original = btiCaseExample.copy(reference = "ref", status = REFERRED)
    val updated = btiCaseExample.copy(reference = "ref", status = OPEN)

    "Delegate to connector" in {
      service.auditCaseReOpened(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = OPEN,
        previousStatus = REFERRED,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseReopened"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case reopened' when a case is suspended" should {
    val original = btiCaseExample.copy(reference = "ref", status = SUSPENDED)
    val updated = btiCaseExample.copy(reference = "ref", status = OPEN)

    "Delegate to connector" in {
      service.auditCaseReOpened(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = OPEN,
        previousStatus = SUSPENDED,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseReopened"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case appeal change'" should {
    val original = aCase(withReference("ref"), withDecision(appeal = Some(Appeal(AppealStatus.IN_PROGRESS))))
    val updated = aCase(withReference("ref"), withoutDecision())

    "Delegate to connector" in {
      service.auditCaseAppealChange(original, updated, operator)

      val payload = appealChangeAudit(
        caseReference = "ref",
        newStatus = None,
        previousStatus = Some(AppealStatus.IN_PROGRESS),
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseAppealChange"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case review change'" should {
    val original = aCase(withReference("ref"), withDecision(review = Some(Review(ReviewStatus.IN_PROGRESS))))
    val updated = aCase(withReference("ref"), withoutDecision())

    "Delegate to connector" in {
      service.auditCaseReviewChange(original, updated, operator)

      val payload = reviewChangeAudit(
        caseReference = "ref",
        newStatus = None,
        previousStatus = Some(ReviewStatus.IN_PROGRESS),
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseReviewChange"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit extended use change'" should {
    val original = aCase(withReference("ref"), withDecision(cancellation = Some(Cancellation(CancelReason.ANNULLED, applicationForExtendedUse = true))))
    val updated = aCase(withReference("ref"), withoutDecision())
    val operator = Operator("operator-id")

    "Delegate to connector" in {
      service.auditCaseExtendedUseChange(original, updated, operator)

      val payload = extendedUseChangeAudit(
        caseReference = "ref",
        newStatus = false,
        previousStatus = true,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseExtendedUseChange"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit note'" should {
    val c = btiCaseExample.copy(reference = "ref", status = OPEN)
    val comment = "this is my note"

    "Delegate to connector" in {
      service.auditNote(c, comment, operator)

      val payload = Map(
        "caseReference" -> c.reference,
        "operatorId" -> operator.id,
        "note" -> comment
      )
      verify(connector).sendExplicitAudit(refEq("caseNote"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case keyword added'" should {
    val c = btiCaseExample.copy(reference = "ref", status = COMPLETED)
    val keyword = "PHONE"

    "Delegate to connector" in {
      service.auditCaseKeywordAdded(c, keyword, operator)

      val payload = caseKeywordAudit(c.reference, keyword, operator.id)

      verify(connector).sendExplicitAudit(refEq("caseKeywordAdded"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case keyword removed'" should {
    val c = btiCaseExample.copy(reference = "ref", status = COMPLETED)
    val keyword = "PHONE"

    "Delegate to connector" in {
      service.auditCaseKeywordRemoved(c, keyword, operator)

      val payload = caseKeywordAudit(c.reference, keyword, operator.id)

      verify(connector).sendExplicitAudit(refEq("caseKeywordRemoved"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  private def caseChangeAudit(caseReference: String, newStatus: CaseStatus, previousStatus: CaseStatus, operatorId: String): Map[String, String] = {
    Map[String, String](
      "caseReference" -> caseReference,
      "operatorId" -> operatorId,
      "newStatus" -> newStatus.toString,
      "previousStatus" -> previousStatus.toString
    )
  }

  private def caseCancelAudit(caseReference: String, cancelReason: CancelReason, operatorId: String): Map[String, String] = {
    Map[String, String](
      "caseReference" -> caseReference,
      "operatorId" -> operatorId,
      "newStatus" -> "CANCELLED",
      "previousStatus" -> "COMPLETED",
      "cancelReason" -> cancelReason.toString
    )
  }

  private def caseKeywordAudit(caseReference: String, keyword: String, operatorId: String): Map[String, String] = {
    Map[String, String](
      "caseReference" -> caseReference,
      "operatorId" -> operatorId,
      "keyword" -> keyword
    )
  }

  private def appealChangeAudit(caseReference: String, newStatus: Option[AppealStatus], previousStatus: Option[AppealStatus], operatorId: String): Map[String, String] = {
    Map[String, String](
      "caseReference" -> caseReference,
      "operatorId" -> operatorId,
      "newAppealStatus" -> newStatus.map(_.toString).getOrElse("None"),
      "previousAppealStatus" -> previousStatus.map(_.toString).getOrElse("None")
    )
  }

  private def reviewChangeAudit(caseReference: String, newStatus: Option[ReviewStatus], previousStatus: Option[ReviewStatus], operatorId: String): Map[String, String] = {
    Map[String, String](
      "caseReference" -> caseReference,
      "operatorId" -> operatorId,
      "newReviewStatus" -> newStatus.map(_.toString).getOrElse("None"),
      "previousReviewStatus" -> previousStatus.map(_.toString).getOrElse("None")
    )
  }

  private def extendedUseChangeAudit(caseReference: String, newStatus: Boolean, previousStatus: Boolean, operatorId: String): Map[String, String] = {
    Map[String, String](
      "caseReference" -> caseReference,
      "operatorId" -> operatorId,
      "newExtendedUseStatus" -> newStatus.toString,
      "previousExtendedUseStatus" -> previousStatus.toString
    )
  }

}
