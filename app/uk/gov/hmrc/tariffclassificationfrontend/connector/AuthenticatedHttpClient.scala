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

class AuthenticatedHttpClient @Inject()(auditConnector: AuditConnector, wsClient: WSClient, actorSystem: ActorSystem)
                                       (implicit val config: AppConfig)
  extends DefaultHttpClient(config.runModeConfiguration, auditConnector, wsClient, actorSystem)
    with InjectAuthHeader {

  override def doGet(url: String)
                    (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    super.doGet(url)(addAuth)
  }

  override def doPost[A](url: String, body: A, headers: Seq[(String, String)])
                        (implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = {
    super.doPost(url, body, headers)(rds, addAuth)
  }

  override def doFormPost(url: String, body: Map[String, Seq[String]])
                         (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    super.doFormPost(url, body)(addAuth)
  }

  override def doPostString(url: String, body: String, headers: Seq[(String, String)])
                           (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    super.doPostString(url, body, headers)(addAuth)
  }

  override def doEmptyPost[A](url: String)
                             (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    super.doEmptyPost(url)(addAuth)
  }

  override def doPut[A](url: String, body: A)
                       (implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = {
    super.doPut(url, body)(rds, addAuth)
  }

  override def doDelete(url: String)
                       (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    super.doDelete(url)(addAuth)
  }

  override def doPatch[A](url: String, body: A)
                         (implicit rds: Writes[A], hc: HeaderCarrier): Future[HttpResponse] = {
    super.doPatch(url, body)(rds, addAuth)
  }

}

trait InjectAuthHeader {

  private val headerName: String = "X-Api-Token"

  def addAuth(implicit config: AppConfig, hc: HeaderCarrier): HeaderCarrier = {
    hc.headers.toMap.get(headerName) match {
      case Some(_) => hc
      case _ => hc.withExtraHeaders(headerName -> config.apiToken)
    }
  }

}
