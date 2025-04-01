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
import models._
import models.request.FileStoreInitiateRequest
import models.response._
import org.apache.pekko.stream.scaladsl.{FileIO, Source}
import org.apache.pekko.stream.{IOResult, Materializer}
import org.apache.pekko.util.ByteString
import play.api.libs.json.Json
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.JsonFormatters.fileMetaDataFormat
import play.api.libs.ws.writeableOf_JsValue
import play.api.libs.ws.bodyWritableOf_Multipart
import uk.gov.hmrc.http.client.readStreamHttpResponse
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileStoreConnector @Inject() (
  appConfig: AppConfig,
  http: HttpClientV2,
  val metrics: MetricRegistry
)(implicit mat: Materializer, ec: ExecutionContext)
    extends HasMetrics
    with InjectAuthHeader {

  private val ParamLength = 42 // A 36-char UUID plus &id= and some wiggle room
  private val BatchSize   = ((appConfig.maxUriLength - appConfig.fileStoreUrl.length) / ParamLength).intValue()

  private def makeQuery(ids: Seq[String]): String = {
    val query = s"?${ids.map("id=" + _).mkString("&")}"
    s"${appConfig.fileStoreUrl}/file$query"
  }

  def get(attachments: Seq[Attachment])(implicit hc: HeaderCarrier): Future[Seq[FileMetadata]] =
    withMetricsTimerAsync("get-file-metadata") { _ =>
      if (attachments.isEmpty) {
        Future.successful(Seq.empty)
      } else {
        Source(attachments.map(_.id).toList)
          .grouped(BatchSize)
          .mapAsyncUnordered(Runtime.getRuntime.availableProcessors()) { ids =>
            http
              .get(url"${makeQuery(ids)}")
              .setHeader(authHeaders(appConfig)(hc)*)
              .execute[Seq[FileMetadata]]
          }
          .runFold(Seq.empty[FileMetadata]) { case (acc, next) =>
            acc ++ next
          }
      }
    }

  def get(attachmentId: String)(implicit hc: HeaderCarrier): Future[Option[FileMetadata]] =
    withMetricsTimerAsync("get-file-metadata-by-id") { _ =>
      val fullURL = s"${appConfig.fileStoreUrl}/file/$attachmentId"

      http
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig)(hc)*)
        .execute[Option[FileMetadata]]
    }

  def initiate(request: FileStoreInitiateRequest)(implicit hc: HeaderCarrier): Future[FileStoreInitiateResponse] =
    withMetricsTimerAsync("initiate-file-upload") { _ =>
      val fullURL = s"${appConfig.fileStoreUrl}/file/initiate"

      http
        .post(url"$fullURL")
        .setHeader(authHeaders(appConfig)(hc)*)
        .withBody(Json.toJson(request))
        .execute[FileStoreInitiateResponse]
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

      http
        .post(url"${appConfig.fileStoreUrl}/file")
        .setHeader(authHeaders(appConfig)(hc)*)
        .withBody(Source(List(filePart, dataPart)))
        .execute[FileMetadata]
    }

  def downloadFile(fileURL: String)(implicit hc: HeaderCarrier): Future[Option[Source[ByteString, ?]]] =
    withMetricsTimerAsync("download-file") { _ =>
      http
        .get(url"$fileURL")
        .setHeader(authHeaders(appConfig)*)
        .stream[HttpResponse]
        .flatMap { response =>
          if (response.status / 100 == 2) {
            Future.successful(Some(response.bodyAsSource))
          } else if (response.status / 100 > 4) {
            Future.failed(new RuntimeException("Unable to retrieve file from filestore"))
          } else {
            Future.successful(None)
          }
        }
    }

  def delete(fileId: String)(implicit hc: HeaderCarrier): Future[Unit] =
    withMetricsTimerAsync("delete-file") { _ =>
      val fullURL = s"${appConfig.fileStoreUrl}/file/$fileId"

      http
        .delete(url"$fullURL")
        .setHeader(authHeaders(appConfig)(hc)*)
        .execute[HttpResponse](using throwOnFailure(readEitherOf(using readRaw)), ec)
        .map(_ => ())
    }

}
