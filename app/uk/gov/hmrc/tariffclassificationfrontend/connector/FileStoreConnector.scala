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

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.google.inject.Inject
import javax.inject.Singleton
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.response.FilestoreResponse
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.fileMetaDataFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FileStoreConnector @Inject()(appConfig: AppConfig, http: HttpClient,  ws: WSClient) {

  def get(attachments: Seq[Attachment])(implicit headerCarrier: HeaderCarrier): Future[Seq[FilestoreResponse]] = {
    if (attachments.isEmpty) {
      Future.successful(Seq.empty)
    } else {
      val query = s"?${attachments.map(att => s"id=${att.id}").mkString("&")}"
      http.GET[Seq[FilestoreResponse]](s"${appConfig.fileStoreUrl}/file$query")
    }
  }

  def get(attachment: Attachment)(implicit headerCarrier: HeaderCarrier): Future[Option[FilestoreResponse]] = {
    http.GET[Option[FilestoreResponse]](s"${appConfig.fileStoreUrl}/file/${attachment.id}")
  }

  def upload(file: MultipartFormData.FilePart[TemporaryFile])
            (implicit hc: HeaderCarrier): Future[FilestoreResponse] = {

    val filePart: MultipartFormData.Part[Source[ByteString, Future[IOResult]]] = FilePart(
      "file",
      file.filename,
      file.contentType,
      FileIO.fromPath(file.ref.file.toPath)
    )

    ws.url(s"${appConfig.fileStoreUrl}/file")
      .post(Source(List(filePart)))
      .map( response => Json.fromJson[FilestoreResponse](Json.parse(response.body)).get)

  }

  def publish(file: Attachment)(implicit hc: HeaderCarrier): Future[FilestoreResponse] = {
    http.POSTEmpty[FilestoreResponse](s"${appConfig.fileStoreUrl}/file/${file.id}/publish")
  }

}
