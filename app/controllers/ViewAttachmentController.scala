/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import cats.data.OptionT
import config.AppConfig
import models.Permission
import models.response.FileMetadata
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.FileStoreService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.view_attachment_unavailable

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding

import java.net.URLEncoder
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewAttachmentController @Inject() (
  verify: RequestActions,
  fileService: FileStoreService,
  mcc: MessagesControllerComponents,
  val view_attachment_unavailable: view_attachment_unavailable,
  implicit val appConfig: AppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with WithUnsafeDefaultFormBinding {

  /*
   * This method is used to encode the filename in the Content-Disposition header
   * url encode the filename and browser will decoded based on the charset you specify
   * if no filename is provided, generate a random one
   */
  private def getEncodedFilenameHeader(fileSubmitted: FileMetadata): String =
    fileSubmitted.fileName
      .map(fileName => s"filename*=UTF-8''${URLEncoder.encode(fileName, "UTF-8")}")
      .getOrElse(UUID.randomUUID().toString)

  def get(reference: String, id: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.VIEW_CASES))
      .async { implicit request =>
        fileService.getFileMetadata(id).flatMap {
          case meta @ Some(fileSubmitted: FileMetadata) =>
            val fileStoreResponse = for {
              url     <- OptionT.fromOption[Future](fileSubmitted.url)
              content <- OptionT(fileService.downloadFile(url))
            } yield Ok
              .streamed(content, None, fileSubmitted.mimeType)
              .withHeaders(
                "Content-Disposition" -> getEncodedFilenameHeader(fileSubmitted)
              )
            fileStoreResponse.getOrElse(NotFound(view_attachment_unavailable(meta)))

          case None =>
            Future.successful(NotFound(view_attachment_unavailable(None)))
        }
      }
}
