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

package connector

import com.kenshoo.play.metrics.Metrics
import config.AppConfig
import javax.inject.{Inject, Singleton}
import metrics.HasMetrics
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._

@Singleton
class RulingConnector @Inject() (
  configuration: AppConfig,
  http: AuthenticatedHttpClient,
  val metrics: Metrics
)(implicit ec: ExecutionContext)
    extends HasMetrics {

  def notify(id: String)(implicit hc: HeaderCarrier): Future[Unit] =
    withMetricsTimerAsync("notify-rulings-frontend") { _ =>
      http.POSTEmpty[Unit](s"${configuration.rulingUrl}/search-for-advance-tariff-rulings/ruling/$id")
    }
}
