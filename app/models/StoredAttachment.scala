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

package models

import java.time.Instant
import models.response.FileMetadata
import models.response.ScanStatus.ScanStatus
import play.twirl.api.Html
import utils.Dates

case class StoredAttachment(
  id: String,
  public: Boolean,
  operator: Option[Operator],
  url: Option[String],
  fileName: String,
  mimeType: String,
  scanStatus: Option[ScanStatus],
  timestamp: Instant,
  description: Option[String],
  shouldPublishToRulings: Boolean
) {

  def isImage: Boolean =
    mimeType match {
      case "image/png"  => true
      case "image/jpeg" => true
      case "image/gif"  => true
      case _            => false
    }

  def formattedDate: String =
    Dates.format(this.timestamp)

  def nameOfOperator: String = {
    val name = operator.map(op => op.name.map(name => name.trim).getOrElse("")).getOrElse("")
    name
  }

  def delimiterBetweenNameAndRole: Html =
    if (nameOfOperator.trim.isEmpty || roleOfOperator.trim.isEmpty) {
      Html("")
    } else {
      Html("<br>")
    }

  def roleOfOperator: String = {
    val role = operator.map(op => op.role.toString).getOrElse("")
    role
  }
}

object StoredAttachment {
  def apply(attachment: Attachment, metadata: FileMetadata): StoredAttachment = {
    require(attachment.id == metadata.id, "Cannot combine different attachments")
    StoredAttachment(
      id          = attachment.id,
      public      = attachment.public,
      operator    = attachment.operator,
      timestamp   = attachment.timestamp,
      url         = metadata.url,
      fileName    = metadata.fileName.getOrElse("New Attachment"),
      mimeType    = metadata.mimeType.getOrElse("FileType"),
      scanStatus  = metadata.scanStatus,
      description = attachment.description,
      shouldPublishToRulings = attachment.shouldPublishToRulings
    )
  }
}
