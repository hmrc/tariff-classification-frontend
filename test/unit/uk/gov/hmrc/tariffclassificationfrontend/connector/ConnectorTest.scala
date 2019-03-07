package uk.gov.hmrc.tariffclassificationfrontend.connector

import akka.actor.ActorSystem
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.tariffclassificationfrontend.utils.{ResourceFiles, WiremockTestServer}

abstract class ConnectorTest extends UnitSpec with WithFakeApplication with WiremockTestServer with MockitoSugar with ResourceFiles {

  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val actorSystem = ActorSystem.create("test")
  protected val realConfig: AppConfig = fakeApplication.injector.instanceOf[AppConfig]
  protected val authenticatedWSClient = new AuthenticatedHttpClient(realConfig, auditConnector, wsClient, actorSystem)
  protected val standardWSClient = new DefaultHttpClient(realConfig.runModeConfiguration, auditConnector, wsClient, actorSystem)
  protected implicit val headers: HeaderCarrier = HeaderCarrier()

}
