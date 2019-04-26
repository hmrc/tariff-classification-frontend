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

import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.UploadAttachmentForm
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService}
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class AttachmentsController @Inject()(verify: RequestActions,
                                      casesService: CasesService,
                                      fileService: FileStoreService,
                                      val messagesApi: MessagesApi,
                                      implicit val appConfig: AppConfig,
                                      implicit val mat: Materializer) extends FrontendController with I18nSupport {

  private lazy val form: Form[String] = UploadAttachmentForm.form

  def attachmentsDetails(reference: String): Action[AnyContent] = verify.authenticate.async { implicit request =>
    getCaseAndRenderView(reference, CaseDetailPage.ATTACHMENTS, renderView(_, form))
  }

  private def renderView(c: Case, uploadForm: Form[String])
                        (implicit hc: HeaderCarrier, request: Request[_]): Future[Html] = {

    for {
      attachments <- fileService.getAttachments(c)
      letter <- fileService.getLetterOfAuthority(c)
    } yield {
      val (applicantFiles, nonApplicantFiles) = attachments.partition(_.operator.isEmpty)
      views.html.partials.attachments_details(c, uploadForm, applicantFiles, letter, nonApplicantFiles)
    }

  }

  private def getCaseAndRenderView(reference: String, page: CaseDetailPage, toHtml: Case => Future[Html])
                                  (implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).flatMap {
      case Some(c: Case) => toHtml(c).map(html => Ok(views.html.case_details(c, page, html)))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }

  def uploadAttachment(reference: String): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] =
    (verify.authenticate andThen verify.mustHave[AuthenticatedRequest](Permission.ADD_ATTACHMENT))
      .async(parse.maxLength(appConfig.fileUploadMaxSize, parse.multipartFormData)) {

        implicit request =>

          request.body match {
            case Left(MaxSizeExceeded(_)) => renderErrors(reference, messagesApi("cases.attachment.upload.error.restrictionSize"))
            case Right(multipartForm) =>
              multipartForm match {
                case file: MultipartFormData[TemporaryFile] if file.files.nonEmpty => uploadAndSave(reference, file)
                case _ => renderErrors(reference, messagesApi("cases.attachment.upload.error.mustSelect"))
              }
          }
      }

  private def uploadAndSave(reference: String, multiPartFormData: MultipartFormData[TemporaryFile])
                           (implicit hc: HeaderCarrier, request: AuthenticatedRequest[_]) = {

    multiPartFormData.file("file-input") match {
      case Some(filePart) if filePart.filename.isEmpty => renderErrors(reference, messagesApi("cases.attachment.upload.error.mustSelect"))
      case Some(filePart) if hasInvalidContentType(filePart) => renderErrors(reference, messagesApi("cases.attachment.upload.error.fileType"))
      case Some(filePart) =>
        val fileUpload = FileUpload(filePart.ref, filePart.filename, filePart.contentType.getOrElse(throw new IllegalArgumentException("Missing file type")))
        casesService.getOne(reference).flatMap {
          case Some(c: Case) =>
            casesService.addAttachment(c, fileUpload, request.operator)
              .flatMap(_ => successful(Redirect(routes.AttachmentsController.attachmentsDetails(reference))))
          case _ =>
            successful(Ok(views.html.case_not_found(reference)))
        }
      case _ => renderErrors(reference, "expected type file on the form")
    }
  }

  private def renderErrors(reference: String, errorMessage: String)
                          (implicit hc: HeaderCarrier, req: Request[_]): Future[Result] = {
    getCaseAndRenderView(
      reference,
      CaseDetailPage.ATTACHMENTS,
      {
        val errors = Seq(FormError("file-input", errorMessage))
        val formWithErrors = form.copy(errors = errors)
        renderView(_, formWithErrors)
      }
    )
  }

  private def hasInvalidContentType: MultipartFormData.FilePart[TemporaryFile] => Boolean = { f =>
    f.contentType match {
      case Some(c: String) if appConfig.fileUploadMimeTypes.contains(c) => false
      case _ => true
    }
  }

}
