/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors

import com.codahale.metrics.MetricRegistry
import com.google.inject.Inject
import metrics.HasMetrics
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StrideAuthConnector @Inject() (
  client: HttpClientV2,
  servicesConfig: ServicesConfig,
  val metrics: MetricRegistry
) extends PlayAuthConnector
    with HasMetrics {
  override val serviceUrl: String         = servicesConfig.baseUrl("auth")
  override def httpClientV2: HttpClientV2 = client

  override def authorise[A](
    predicate: Predicate,
    retrieval: Retrieval[A]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    withMetricsTimerAsync("stride-authorise")(_ => super.authorise(predicate, retrieval))
}
