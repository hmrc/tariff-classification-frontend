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
    getAttachments(Seq(c)).map(group => group.getOrElse(c, Seq.empty))
  }

  def getAttachments(cases: Seq[Case])(implicit hc: HeaderCarrier): Future[Map[Case, Seq[StoredAttachment]]] = {
    val caseByFileId: Map[String, Case] = cases.foldLeft(Map[String, Case]())((existing, c) => existing ++ c.attachments.map(_.id -> c))
    val attachmentsById: Map[String, Attachment] = cases.flatMap(_.attachments).map(a => a.id -> a).toMap

    def groupingByCase: Seq[FileMetadata] => Map[Case, Seq[StoredAttachment]] = { files =>
        val group: Map[Case, Seq[StoredAttachment]] = files.map { file =>
          caseByFileId.get(file.id) -> attachmentsById.get(file.id).map(StoredAttachment(_, file))
        } filter {
          // Select only attachments where the File is linked to a Case & Attachment
          case (c: Option[Case], att: Option[StoredAttachment]) => c.isDefined && att.isDefined
        } map {
          case (c: Option[Case], att: Option[StoredAttachment]) => (c.get, att.get)
        } groupBy (_._1) map {
          case (c: Case, seq: Seq[(Case, StoredAttachment)]) => (c, seq.map(_._2))
        }

        // Log an error for any attachments which arent in the response
        val idsFound: Set[String] = group.flatMap(_._2).map(_.id).toSet
        attachmentsById.keys.filterNot(idsFound.contains).foreach { id =>
          Logger.error(s"Published file [$id] was not found in the Filestore")
        }

        // The Map currently only contains Cases which have >=1 attachments.
        // Add in the cases with 0 attachments
        val missing = cases.filterNot(group.contains)
        group ++ missing.map(_ -> Seq.empty)
    }

    connector.get(cases.flatMap(_.attachments)) map groupingByCase
  }

  def getLetterOfAuthority(c: Case)(implicit hc: HeaderCarrier): Future[Option[StoredAttachment]] = {
    if (c.application.isBTI) {
      c.application.asBTI.agent.flatMap(_.letterOfAuthorisation) match {
        case Some(attachment: Attachment) =>
          connector
            .get(attachment)
            .map { file =>
              Logger.error(s"Agent Letter of Authority [${attachment.id}] was present on Case [${c.reference}] but it didn't exist in the FileStore")
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
