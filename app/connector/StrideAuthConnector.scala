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

import com.google.inject.Inject
import com.kenshoo.play.metrics.Metrics
import javax.inject.Singleton
import metrics.HasMetrics
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.CorePost
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class StrideAuthConnector @Inject() (
  client: HttpClient,
  servicesConfig: ServicesConfig,
  val metrics: Metrics
) extends PlayAuthConnector
    with HasMetrics {
  override val serviceUrl: String = servicesConfig.baseUrl("auth")
  override def http: CorePost     = client

  override def authorise[A](
    predicate: Predicate,
    retrieval: Retrieval[A]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    withMetricsTimerAsync("stride-authorise")(_ => super.authorise(predicate, retrieval))
}
