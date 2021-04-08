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

package controllers

import cats.data.OptionT
import config.AppConfig
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.successful

class PdfDownloadController @Inject() (
  authenticatedAction: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  fileStore: FileStoreService,
  caseService: CasesService,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def getRulingPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference).flatMap {
      case Some(cse) =>
        cse.decision match {
          case Some(decision) =>
            val pdfResult = for {
              pdf     <- OptionT.fromOption[Future](decision.decisionPdf)
              meta    <- OptionT(fileStore.getFileMetadata(pdf.id))
              url     <- OptionT.fromOption[Future](meta.url)
              content <- OptionT(fileStore.downloadFile(url))
            } yield Ok
              .streamed(content, None, meta.mimeType)
              .withHeaders(
                "Content-Disposition" -> s"attachment; filename=${meta.fileName.getOrElse("New Attachment")}"
              )

            val messages     = request.messages
            val documentType = messages("errors.document-not-found.ruling-certificate")
            pdfResult.getOrElse(NotFound(views.html.document_not_found(documentType, reference)))

          case None =>
            successful(NotFound(views.html.ruling_not_found(reference)))
        }

      case None =>
        successful(NotFound(views.html.case_not_found(reference)))
    }
  }

  def getLetterPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference).flatMap {
      case Some(cse) =>
        cse.decision match {
          case Some(decision) =>
            val pdfResult = for {
              pdf     <- OptionT.fromOption[Future](decision.letterPdf)
              meta    <- OptionT(fileStore.getFileMetadata(pdf.id))
              url     <- OptionT.fromOption[Future](meta.url)
              content <- OptionT(fileStore.downloadFile(url))
            } yield Ok
              .streamed(content, None, meta.mimeType)
              .withHeaders(
                "Content-Disposition" -> s"attachment; filename=${meta.fileName.getOrElse("New Attachment")}"
              )

            val messages     = request.messages
            val documentType = messages("errors.document-not-found.ruling-certificate")
            pdfResult.getOrElse(NotFound(views.html.document_not_found(documentType, reference)))

          case None =>
            successful(NotFound(views.html.ruling_not_found(reference)))
        }

      case None =>
        successful(NotFound(views.html.case_not_found(reference)))
    }
  }

  def applicationPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference).flatMap {
      case Some(cse) =>
        val pdfResult = for {
          pdf     <- OptionT.fromOption[Future](cse.application.asATAR.applicationPdf)
          meta    <- OptionT(fileStore.getFileMetadata(pdf.id))
          url     <- OptionT.fromOption[Future](meta.url)
          content <- OptionT(fileStore.downloadFile(url))
        } yield Ok
          .streamed(content, None, meta.mimeType)
          .withHeaders(
            "Content-Disposition" -> s"attachment; filename=${meta.fileName.getOrElse("New Attachment")}"
          )

        val messages     = request.messages
        val documentType = messages("errors.document-not-found.application")
        pdfResult.getOrElse(NotFound(views.html.document_not_found(documentType, reference)))

      case None =>
        successful(NotFound(views.html.case_not_found(reference)))
    }
  }
}
