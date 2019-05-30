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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import play.api.libs.Files
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{MultipartFormData, Result}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.FileUpload
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedCaseRequest

import scala.concurrent.Future

trait ExtractableFile {

  val appConfig: AppConfig

  protected def extractFile(key: String)(
    onFileValid: FileUpload => Future[Result],
    onFileTooLarge: () => Future[Result],
    onFileInvalidType: () => Future[Result],
    onFileMissing: () => Future[Result]
  )(implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]]): Future[Result] = request.body.file(key).filter(_.filename.nonEmpty).filter(_.contentType.isDefined) match {
    case Some(file) if !hasValidContentType(file) => onFileInvalidType()
    case Some(file) if !hasValidFileSize(file) => onFileTooLarge()
    case Some(file) => onFileValid(FileUpload(file.ref, file.filename, file.contentType.get))
    case None => onFileMissing()
  }

  private def hasValidContentType(f: MultipartFormData.FilePart[TemporaryFile]): Boolean = f.contentType match {
    case Some(c: String) if appConfig.fileUploadMimeTypes.contains(c) => true
    case _ => false
  }

  private def hasValidFileSize(f: MultipartFormData.FilePart[TemporaryFile]): Boolean = f.ref.file.length <= appConfig.fileUploadMaxSize
}
