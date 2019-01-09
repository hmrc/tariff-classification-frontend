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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.response.FileMetadata
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.fileMetaDataFormat

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FileStoreConnector @Inject()(appConfig: AppConfig, http: HttpClient) {

  def get(attachments: Seq[Attachment])(implicit headerCarrier: HeaderCarrier): Future[Seq[FileMetadata]] = {
    if (attachments.isEmpty) {
      Future.successful(Seq.empty)
    } else {
      val query = s"?${attachments.map(att => s"id=${att.id}").mkString("&")}"
      http.GET[Seq[FileMetadata]](s"${appConfig.fileStoreUrl}/binding-tariff-filestore/file$query")
    }
  }

  def get(attachment: Attachment)(implicit headerCarrier: HeaderCarrier): Future[Option[FileMetadata]] = {
    http.GET[Option[FileMetadata]](s"${appConfig.fileStoreUrl}/binding-tariff-filestore/file/${attachment.id}")
  }

}
