/*
 * Copyright 2024 HM Revenue & Customs
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

import com.codahale.metrics.MetricRegistry
import config.AppConfig
import metrics.HasMetrics
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RulingConnector @Inject() (
  configuration: AppConfig,
  http: HttpClientV2,
  val metrics: MetricRegistry
)(implicit ec: ExecutionContext)
    extends HasMetrics
    with InjectAuthHeader {

  def notify(id: String)(implicit hc: HeaderCarrier): Future[Unit] =
    withMetricsTimerAsync("notify-rulings-frontend") { _ =>
      val fullURL = s"${configuration.rulingUrl}/search-for-advance-tariff-rulings/ruling/$id"
      http
        .post(url"$fullURL")
        .setHeader(authHeaders(configuration): _*)
        .execute[HttpResponse]
        .map(_ => ())
    }
}
