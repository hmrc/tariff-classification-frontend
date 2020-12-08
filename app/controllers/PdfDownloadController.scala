/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class PdfDownloadController @Inject() (
  authenticatedAction: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  fileStore: FileStoreService,
  caseService: CasesService,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  def getRulingPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference).flatMap {
      case Some(cse) =>
        val pdfResult = for {
          decision <- OptionT.fromOption[Future](cse.decision)
          pdf <- OptionT.fromOption[Future](decision.decisionPdf)
          meta <- OptionT(fileStore.getFileMetadata(pdf.id))
          url <- OptionT.fromOption[Future](meta.url)
          content <- OptionT(fileStore.downloadFile(url))
        } yield Ok.streamed(content, None, Some(meta.mimeType)).withHeaders(
          "Content-Disposition" -> s"attachment; filename=${meta.fileName}"
        )

        pdfResult.getOrElse(Ok(views.html.ruling_not_found(reference)))

      case None =>
        successful(Ok(views.html.case_not_found(reference)))
    }
  }

  def applicationPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference).flatMap {
      case Some(cse) =>
        val pdfResult = for {
          pdf <- OptionT.fromOption[Future](cse.application.asATAR.applicationPdf)
          meta <- OptionT(fileStore.getFileMetadata(pdf.id))
          url <- OptionT.fromOption[Future](meta.url)
          content <- OptionT(fileStore.downloadFile(url))
        } yield Ok.streamed(content, None, Some(meta.mimeType)).withHeaders(
          "Content-Disposition" -> s"attachment; filename=${meta.fileName}"
        )

        pdfResult.getOrElse(NotFound)

      case None =>
        successful(Ok(views.html.case_not_found(reference)))
    }
  }
}
