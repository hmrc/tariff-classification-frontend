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
import config.AppConfig
import metrics.HasMetrics
import models.{Email, EmailTemplate}
import play.api.libs.json.{Format, Json, Writes}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.Base64Utils
import utils.JsonFormatters.emailTemplateFormat
import play.api.libs.ws.writeableOf_JsValue
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailConnector @Inject() (
  configuration: AppConfig,
  client: HttpClientV2,
  val metrics: MetricRegistry
)(implicit ec: ExecutionContext)
    extends HasMetrics {

  def send[E >: Email[?]](email: E)(implicit hc: HeaderCarrier, writes: Writes[E]): Future[Unit] =
    withMetricsTimerAsync("send-email") { _ =>
      val fullURL = s"${configuration.emailUrl}/hmrc/email"

      client
        .post(url"$fullURL")
        .withBody(Json.toJson(email))
        .execute[HttpResponse](using throwOnFailure(readEitherOf(using readRaw)), ec)
        .map(_ => ())
    }

  def generate[T](email: Email[T])(implicit hc: HeaderCarrier, writes: Format[T]): Future[EmailTemplate] =
    withMetricsTimerAsync("generate-email") { _ =>
      val fullURL = s"${configuration.emailRendererUrl}/templates/${email.templateId}"

      client
        .post(url"$fullURL")
        .withBody(Json.obj("parameters" -> Json.toJson(email.parameters)))
        .execute[EmailTemplate]
        .map(decodingContent)
    }

  private def decodingContent: EmailTemplate => EmailTemplate = (t: EmailTemplate) =>
    t.copy(
      plain = Base64Utils.decode(t.plain),
      html = Base64Utils.decode(t.html)
    )

}
