package uk.gov.hmrc.tariffclassificationfrontend.audit

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Operator, Queue}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.ExecutionContext

class AuditServiceTest extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val connector = mock[DefaultAuditConnector]

  private val service = new AuditService(connector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(connector)
  }

  "Service 'audit case released'" should {
    val original = Cases.btiCaseExample.copy(reference = "ref", status = CaseStatus.NEW)
    val updated = Cases.btiCaseExample.copy(reference = "ref", status = CaseStatus.OPEN)
    val queue = Queue("queue-id", "queueu-slug", "queue-name")
    val operator = Operator("operator-id")

    "Delegate to connector" in {
      service.auditCaseReleased(original, updated, queue, operator)

      val payload = Map[String, String](
        "reference" -> "ref",
        "newStatus" -> "OPEN",
        "oldStatus" -> "NEW",
        "queue" -> "queue-name",
        "operatorId" -> "operator-id"
      )
      verify(connector).sendExplicitAudit(refEq("CaseReleased"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

  "Service 'audit case completed'" should {
    val original = Cases.btiCaseExample.copy(reference = "ref", status = CaseStatus.OPEN)
    val updated = Cases.btiCaseExample.copy(reference = "ref", status = CaseStatus.COMPLETED)
    val operator = Operator("operator-id")

    "Delegate to connector" in {
      service.auditCaseCompleted(original, updated, operator)

      val payload = Map[String, String](
        "reference" -> "ref",
        "newStatus" -> "COMPLETED",
        "oldStatus" -> "OPEN",
        "operatorId" -> "operator-id"
      )
      verify(connector).sendExplicitAudit(refEq("CaseCompleted"), refEq(payload))(any[HeaderCarrier], any[ExecutionContext])
    }
  }

}
