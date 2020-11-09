/*
 * Copyright 2020 HM Revenue & Customs
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
import config.AppConfig
import javax.inject.Singleton
import metrics.HasMetrics
import play.api.libs.json.{Format, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import models.{Email, EmailTemplate}
import scala.concurrent.{ExecutionContext, Future}
import utils.Base64Utils
import utils.JsonFormatters.emailTemplateFormat
import uk.gov.hmrc.http.HttpReads.Implicits._

@Singleton
class EmailConnector @Inject() (
  configuration: AppConfig,
  client: HttpClient,
  val metrics: Metrics
)(implicit ec: ExecutionContext)
    extends HasMetrics {

  def send[E >: Email[Any]](e: E)(implicit hc: HeaderCarrier, writes: Writes[E]): Future[Unit] =
    withMetricsTimerAsync("send-email") { _ =>
      val url = s"${configuration.emailUrl}/hmrc/email"
      client.POST[E, Unit](url = url, body = e)
    }

  def generate[T](e: Email[T])(implicit hc: HeaderCarrier, writes: Format[T]): Future[EmailTemplate] =
    withMetricsTimerAsync("generate-email") { _ =>
      val url = s"${configuration.emailRendererUrl}/templates/${e.templateId}"
      client.POST[Map[String, T], EmailTemplate](url, Map("parameters" -> e.parameters)).map(decodingContent)
    }

  private def decodingContent: EmailTemplate => EmailTemplate = { t: EmailTemplate =>
    t.copy(
      plain = Base64Utils.decode(t.plain),
      html  = Base64Utils.decode(t.html)
    )
  }

}
