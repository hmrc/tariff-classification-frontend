package uk.gov.hmrc.tariffclassificationfrontend.connector

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.libs.json.Writes
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig

import scala.concurrent.Future

class AuthenticatedHttpClient @Inject()(config: AppConfig,
                                        auditConnector: AuditConnector,
                                        wsClient: WSClient,
                                        actorSystem: ActorSystem)
  extends DefaultHttpClient(config.runModeConfiguration, auditConnector, wsClient, actorSystem) {

  private val header: String = "X-Api-Token"
  private val token: String = config.apiToken

  private def addAuth(implicit headerCarrier: HeaderCarrier): HeaderCarrier = headerCarrier.withExtraHeaders(header -> token)

  override def doGet(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = super.doGet(url)(addAuth)

  override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = super.doPost(url, body, headers)(rds, addAuth)

  override def doFormPost(url: String, body: Map[String, Seq[String]])(implicit hc: HeaderCarrier): Future[HttpResponse] = super.doFormPost(url, body)(addAuth)

  override def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[HttpResponse] = super.doPostString(url, body, headers)(addAuth)

  override def doEmptyPost[A](url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = super.doEmptyPost(url)(addAuth)

  override def doPut[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = super.doPut(url, body)(rds, addAuth)

  override def doDelete(url: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = super.doDelete(url)(addAuth)

  override def doPatch[A](url: String, body: A)(implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = super.doPatch(url, body)(rds, addAuth)
}
