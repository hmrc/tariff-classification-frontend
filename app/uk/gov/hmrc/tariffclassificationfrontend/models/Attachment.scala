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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.net.URL
import java.time.ZonedDateTime

import org.apache.commons.io.FilenameUtils

import scala.util.{Success, Try}

case class Attachment
(
  application: Boolean,
  public: Boolean,
  url: String,
  mimeType: String,
  timestamp: ZonedDateTime
) {

  def isImage: Boolean = {
    mimeType match {
      case "image/png" => true
      case "image/jpeg" => true
      case "image/gif" => true
      case _ => false
    }
  }

  def name: Option[String] = {
    Try(new URL(url)) match {
      case Success(u: URL) if u.getPath.nonEmpty => Some(FilenameUtils.getName(u.getPath))
      case _ => None
    }
  }

}
