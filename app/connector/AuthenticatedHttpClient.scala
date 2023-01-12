/*
 * Copyright 2023 HM Revenue & Customs
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

package connector

import akka.actor.ActorSystem
import config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Writes
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatedHttpClient @Inject() (
  configuration: Configuration,
  httpAuditing: HttpAuditing,
  wsClient: WSClient,
  config: AppConfig,
  actorSystem: ActorSystem
) extends DefaultHttpClient(configuration, httpAuditing, wsClient, actorSystem) {

  def addAuth(implicit hc: HeaderCarrier): Seq[(String, String)] = {

    val headerName: String = "X-Api-Token"

    hc.headers(Seq(headerName)) match {
      case header @ Seq(_) => header
      case _               => Seq(headerName -> config.apiToken)
    }
  }

  override def doGet(
    url: String,
    headers: Seq[(String, String)]
  )(implicit ec: ExecutionContext): Future[HttpResponse] =
    super.doGet(url, headers)

  override def doPost[A](
    url: String,
    body: A,
    headers: Seq[(String, String)]
  )(implicit writes: Writes[A], ec: ExecutionContext): Future[HttpResponse] =
    super.doPost(url, body, headers)(writes, ec)

  override def doEmptyPost[A](
    url: String,
    headers: Seq[(String, String)]
  )(implicit ec: ExecutionContext): Future[HttpResponse] =
    super.doEmptyPost(url, headers)

  override def doPut[A](
    url: String,
    body: A,
    headers: Seq[(String, String)]
  )(implicit rds: Writes[A], ec: ExecutionContext): Future[HttpResponse] =
    super.doPut(url, body, headers)

  override def doDelete(
    url: String,
    headers: Seq[(String, String)]
  )(implicit ec: ExecutionContext): Future[HttpResponse] =
    super.doDelete(url, headers)
}
