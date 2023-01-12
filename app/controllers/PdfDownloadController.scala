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
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{case_not_found, document_not_found, ruling_not_found}
import javax.inject.Inject
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding

import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

class PdfDownloadController @Inject() (
  authenticatedAction: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  fileStore: FileStoreService,
  caseService: CasesService,
  implicit val appConfig: AppConfig,
  val case_not_found: case_not_found,
  val ruling_not_found: ruling_not_found,
  val document_not_found: document_not_found
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with WithUnsafeDefaultFormBinding {

  def getRulingPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference).flatMap {
      case Some(cse) =>
        cse.decision match {
          case Some(decision) =>
            val pdfResult = for {
              pdf      <- OptionT.fromOption[Future](decision.decisionPdf)
              meta     <- OptionT(fileStore.getFileMetadata(pdf.id))
              url      <- OptionT.fromOption[Future](meta.url)
              fileName <- OptionT.fromOption[Future](meta.fileName)
              content  <- OptionT(fileStore.downloadFile(url))
            } yield Ok
              .streamed(content, None, meta.mimeType)
              .withHeaders(
                "Content-Disposition" -> s"attachment; filename=$fileName"
              )

            val messages     = request.messages
            val documentType = messages("errors.document-not-found.ruling-certificate")
            pdfResult.getOrElse(NotFound(document_not_found(documentType, reference)))

          case None =>
            successful(NotFound(ruling_not_found(reference)))
        }

      case None =>
        successful(NotFound(case_not_found(reference)))
    }
  }

  def getLetterPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference).flatMap {
      case Some(cse) =>
        cse.decision match {
          case Some(decision) =>
            val pdfResult = for {
              pdf      <- OptionT.fromOption[Future](decision.letterPdf)
              meta     <- OptionT(fileStore.getFileMetadata(pdf.id))
              url      <- OptionT.fromOption[Future](meta.url)
              fileName <- OptionT.fromOption[Future](meta.fileName)
              content  <- OptionT(fileStore.downloadFile(url))
            } yield Ok
              .streamed(content, None, meta.mimeType)
              .withHeaders(
                "Content-Disposition" -> s"attachment; filename=$fileName"
              )

            val messages     = request.messages
            val documentType = messages("errors.document-not-found.ruling-certificate")
            pdfResult.getOrElse(NotFound(document_not_found(documentType, reference)))

          case None =>
            successful(NotFound(ruling_not_found(reference)))
        }

      case None =>
        successful(NotFound(case_not_found(reference)))
    }
  }

  def applicationPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference).flatMap {
      case Some(cse) =>
        val pdfResult = for {
          pdf      <- OptionT.fromOption[Future](cse.application.asATAR.applicationPdf)
          meta     <- OptionT(fileStore.getFileMetadata(pdf.id))
          url      <- OptionT.fromOption[Future](meta.url)
          fileName <- OptionT.fromOption[Future](meta.fileName)
          content  <- OptionT(fileStore.downloadFile(url))
        } yield Ok
          .streamed(content, None, meta.mimeType)
          .withHeaders(
            "Content-Disposition" -> s"attachment; filename=$fileName"
          )

        val messages     = request.messages
        val documentType = messages("errors.document-not-found.application")
        pdfResult.getOrElse(NotFound(document_not_found(documentType, reference)))

      case None =>
        successful(NotFound(case_not_found(reference)))
    }
  }
}
