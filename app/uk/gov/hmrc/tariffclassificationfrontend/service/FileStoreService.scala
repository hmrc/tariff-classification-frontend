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

package uk.gov.hmrc.tariffclassificationfrontend.service

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.connector.FileStoreConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.response.FileMetadata

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class FileStoreService @Inject()(connector: FileStoreConnector) {

  def getAttachments(c: Case)(implicit hc: HeaderCarrier): Future[Seq[StoredAttachment]] = {
    val attachmentsById: Map[String, Attachment] = c.attachments.map(a => a.id -> a).toMap
    connector.get(c.attachments) map { files =>
      files map { file =>
        attachmentsById
          .get(file.id)
          .map(StoredAttachment(_, file))
      } filter (_.isDefined) map (_.get)
    }
  }

  def getLetterOfAuthority(c: Case)(implicit hc: HeaderCarrier): Future[Option[StoredAttachment]] = {
    if (c.application.isBTI) {
      c.application.asBTI.agent.flatMap(_.letterOfAuthorisation) match {
        case Some(attachment: Attachment) =>
          connector
            .get(attachment)
            .map { file =>
              Logger.warn(s"Agent Letter of Authority was present on Case [${c.reference}] but it didn't exist in the FileStore")
              file.map(StoredAttachment(attachment, _))
            }
        case _ => successful(None)
      }
    } else {
      successful(None)
    }
  }

  def upload(fileUpload: FileUpload)(implicit hc: HeaderCarrier): Future[FileStoreAttachment] = {
    connector.upload(fileUpload).map(toFileAttachment(fileUpload.content.file.length))
  }

  private def toFileAttachment(size: Long): FileMetadata => FileStoreAttachment = {
    r => FileStoreAttachment(r.id, r.fileName, r.mimeType, size)
  }

}
