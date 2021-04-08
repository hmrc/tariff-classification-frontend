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

import akka.stream.{IOResult, Materializer}
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.google.inject.Inject
import com.kenshoo.play.metrics.Metrics
import config.AppConfig

import javax.inject.Singleton
import metrics.HasMetrics
import models._
import models.request.FileStoreInitiateRequest
import models.response._
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import utils.JsonFormatters.fileMetaDataFormat
import uk.gov.hmrc.http.HttpReads.Implicits._
import play.api.libs.json.JsResult

@Singleton
class FileStoreConnector @Inject() (
  appConfig: AppConfig,
  http: AuthenticatedHttpClient,
  ws: WSClient,
  val metrics: Metrics
)(implicit mat: Materializer)
    extends HasMetrics {

  implicit val ec: ExecutionContext = mat.executionContext

  private val ParamLength = 42 // A 36-char UUID plus &id= and some wiggle room
  private val BatchSize   = ((appConfig.maxUriLength - appConfig.fileStoreUrl.length) / ParamLength).intValue()

  private def makeQuery(ids: Seq[String]): String = {
    val query = s"?${ids.map("id=" + _).mkString("&")}"
    s"${appConfig.fileStoreUrl}/file$query"
  }

  def get(attachments: Seq[Attachment])(implicit headerCarrier: HeaderCarrier): Future[Seq[FileMetadata]] =
    withMetricsTimerAsync("get-file-metadata") { _ =>
      if (attachments.isEmpty) {
        Future.successful(Seq.empty)
      } else {
        Source(attachments.map(_.id).toList)
          .grouped(BatchSize)
          .mapAsyncUnordered(Runtime.getRuntime().availableProcessors()) { ids =>
            http.GET[Seq[FileMetadata]](makeQuery(ids))
          }
          .runFold(Seq.empty[FileMetadata]) {
            case (acc, next) => acc ++ next
          }
      }
    }

  def get(attachmentId: String)(implicit headerCarrier: HeaderCarrier): Future[Option[FileMetadata]] =
    withMetricsTimerAsync("get-file-metadata-by-id") { _ =>
      http.GET[Option[FileMetadata]](s"${appConfig.fileStoreUrl}/file/$attachmentId")
    }

  def initiate(request: FileStoreInitiateRequest)(implicit hc: HeaderCarrier): Future[FileStoreInitiateResponse] =
    withMetricsTimerAsync("initiate-file-upload") { _ =>

      Logger.error(s"\n\n initiate-file-upload :" + request + "\n\n")

      http
        .POST[FileStoreInitiateRequest, FileStoreInitiateResponse](s"${appConfig.fileStoreUrl}/file/initiate", request)
    }

  def upload(fileUpload: FileUpload)(implicit hc: HeaderCarrier): Future[FileMetadata] =
    withMetricsTimerAsync("upload-file") { _ =>
      val dataPart: MultipartFormData.DataPart = MultipartFormData.DataPart("publish", "true")

      val filePart: MultipartFormData.Part[Source[ByteString, Future[IOResult]]] = FilePart(
        "file",
        fileUpload.fileName,
        Some(fileUpload.contentType),
        FileIO.fromPath(fileUpload.content.path)
      )

      ws.url(s"${appConfig.fileStoreUrl}/file")
        .withHttpHeaders(hc.headers: _*)
        .withHttpHeaders("X-Api-Token" -> appConfig.apiToken)
        .post(Source(List(filePart, dataPart)))
        .flatMap { response =>
          Future.fromTry {
            JsResult.toTry(Json.fromJson[FileMetadata](Json.parse(response.body)))
          }
        }
    }

  def downloadFile(url: String)(implicit hc: HeaderCarrier): Future[Option[Source[ByteString, _]]] =
    withMetricsTimerAsync("download-file") { _ =>
      val fileStoreResponse = ws
        .url(url)
        .withHttpHeaders(hc.headers: _*)
        .withHttpHeaders("X-Api-Token" -> appConfig.apiToken)
        .get()

      fileStoreResponse.flatMap { response =>
        if (response.status / 100 == 2)
          Future.successful(Some(response.bodyAsSource))
        else if (response.status / 100 > 4)
          Future.failed(new RuntimeException(s"Unable to retrieve file $url from filestore"))
        else
          Future.successful(None)
      }
    }

  def delete(fileId: String)(implicit hc: HeaderCarrier): Future[Unit] =
    withMetricsTimerAsync("delete-file")(_ => http.DELETE[Unit](s"${appConfig.fileStoreUrl}/file/$fileId"))

}
