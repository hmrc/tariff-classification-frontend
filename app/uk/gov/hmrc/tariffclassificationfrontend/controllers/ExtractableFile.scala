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
