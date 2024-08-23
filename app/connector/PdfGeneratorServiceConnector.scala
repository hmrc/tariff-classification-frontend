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
import models.PdfFile
import org.apache.pekko.stream.Materializer
import play.api.http.Status.OK
import play.twirl.api.Html
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future.failed
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PdfGeneratorServiceConnector @Inject() (
  configuration: AppConfig,
  http: HttpClientV2,
  val metrics: MetricRegistry
)(using ec: ExecutionContext, mat: Materializer)
    extends HasMetrics {

  private lazy val fullURL: String       = s"${configuration.pdfGeneratorUrl}/pdf-generator-service/generate"
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  def generatePdf(html: Html): Future[PdfFile] =
    withMetricsTimerAsync("generate-pdf") { _ =>
      http
        .post(url"$fullURL")
        .withBody(Map("html" -> Seq(html.toString)))
        .stream[HttpResponse]
        .flatMap { response =>
          response.status match {
            case OK => sourceToPdfFile(response)
            case _  => failed(new RuntimeException(s"Error calling pdf-generator-service - ${response.body}"))
          }
        }
    }

  private def sourceToPdfFile(response: HttpResponse)(using ec: ExecutionContext): Future[PdfFile] =
    response.bodyAsSource.runReduce(_ ++ _).map(byteString => PdfFile(byteString.toArray))

}
