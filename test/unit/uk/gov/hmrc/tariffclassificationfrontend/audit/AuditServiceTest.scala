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
import uk.gov.hmrc.tariffclassificationfrontend.models.{Appeal, AppealStatus, Operator, Queue}
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.tariffclassificationfrontend.utils.Cases._

import scala.concurrent.ExecutionContext

class AuditServiceTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val connector = mock[DefaultAuditConnector]

  private val service = new AuditService(connector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
  }

  "Service 'audit case released'" should {
    val original = btiCaseExample.copy(reference = "ref", status = NEW)
    val updated = btiCaseExample.copy(reference = "ref", status = OPEN)
    val queue = Queue("queue-id", "queue-slug", "queue-name")
    val operator = Operator("operator-id")

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

  "Service 'audit case completed'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated = btiCaseExample.copy(reference = "ref", status = COMPLETED)
    val operator = Operator("operator-id")

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

  "Service 'audit case referred'" should {
    val original = btiCaseExample.copy(reference = "ref", status = OPEN)
    val updated = btiCaseExample.copy(reference = "ref", status = REFERRED)
    val operator = Operator("operator-id")

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


  "Service 'audit case reopen' when a case is referred" should {
    val original = btiCaseExample.copy(reference = "ref", status = REFERRED)
    val updated = btiCaseExample.copy(reference = "ref", status = OPEN)
    val operator = Operator("operator-id")

    "Delegate to connector" in {
      service.auditCaseReOpen(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = OPEN,
        previousStatus = REFERRED,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseReopened"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case appeal change'" should {
    val original = aCase(withReference("ref"), withDecision(appeal = Some(Appeal(AppealStatus.IN_PROGRESS))))
    val updated = aCase(withReference("ref"), withoutDecision())
    val operator = Operator("operator-id")

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

  "Service 'audit case reopen' when a case is suspended" should {
    val original = btiCaseExample.copy(reference = "ref", status = SUSPENDED)
    val updated = btiCaseExample.copy(reference = "ref", status = OPEN)
    val operator = Operator("operator-id")

    "Delegate to connector" in {
      service.auditCaseReOpen(original, updated, operator)

      val payload = caseChangeAudit(
        caseReference = "ref",
        newStatus = OPEN,
        previousStatus = SUSPENDED,
        operatorId = operator.id
      )
      verify(connector).sendExplicitAudit(refEq("caseReopened"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  private def caseChangeAudit(caseReference: String, newStatus: CaseStatus, previousStatus: CaseStatus, operatorId: String): Map[String, String] = {
    Map[String, String](
      "caseReference" -> caseReference,
      "newStatus" -> newStatus.toString,
      "previousStatus" -> previousStatus.toString,
      "operatorId" -> operatorId
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

}
