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

import com.google.inject.Inject
import javax.inject.Singleton
import play.api.libs.json.{Format, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.{Email, EmailTemplate}
import uk.gov.hmrc.tariffclassificationfrontend.utils.Base64Utils
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.emailTemplateFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EmailConnector @Inject()(configuration: AppConfig, client: HttpClient) {

  def send[E >: Email[Any]](e: E)(implicit hc: HeaderCarrier, writes: Writes[E]): Future[Unit] = {
    val url = s"${configuration.emailUrl}/hmrc/email"
    client.POST(url = url, body = e).map(_ => ())
  }

  def generate[T](e: Email[T])(implicit hc: HeaderCarrier, writes: Format[T]): Future[EmailTemplate] = {
    val url = s"${configuration.emailRendererUrl}/templates/${e.templateId}"
    client.POST[Map[String, T], EmailTemplate](url, Map("parameters" -> e.parameters)).map(decodingContent)
  }

  private def decodingContent: EmailTemplate => EmailTemplate = { t: EmailTemplate =>
    t.copy(
      plain = Base64Utils.decode(t.plain),
      html = Base64Utils.decode(t.html)
    )
  }

}
