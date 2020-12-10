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

import akka.stream.Materializer
import config.AppConfig
import models.forms.{MandatoryBooleanForm, UploadAttachmentForm}
import javax.inject.{Inject, Singleton}
import models._
import models.request.AuthenticatedRequest
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.twirl.api.Html
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.CaseDetailPage
import views.CaseDetailPage.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class AttachmentsController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  fileService: FileStoreService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig,
  implicit val mat: Materializer
) extends FrontendController(mcc)
    with RenderCaseAction
    with I18nSupport {

  private lazy val form: Form[String] = UploadAttachmentForm.form

  override protected val config: AppConfig         = appConfig
  override protected val caseService: CasesService = casesService

  private val tabStartIndexForAttachments = 4000

  private val removeAttachmentForm: Form[Boolean] = MandatoryBooleanForm.form("remove_attachment")

  def attachmentsDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      getCaseAndRenderView(reference, CaseDetailPage.ATTACHMENTS, renderView(_, form))
    }

  def removeAttachment(reference: String, fileId: String, fileName: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(
      Permission.REMOVE_ATTACHMENTS
    )).async { implicit request =>
      validateAndRenderView(c => successful(views.html.remove_attachment(c, removeAttachmentForm, fileId, fileName)))
    }

  def confirmRemoveAttachment(reference: String, fileId: String, fileName: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(
      Permission.REMOVE_ATTACHMENTS
    )).async { implicit request =>
      removeAttachmentForm
        .bindFromRequest()
        .fold(
          errors => validateAndRenderView(c => successful(views.html.remove_attachment(c, errors, fileId, fileName))), {
            case true => {
              getCaseAndRespond(
                reference,
                caseService
                  .removeAttachment(_, fileId)
                  .map(_ => Redirect(routes.AttachmentsController.attachmentsDetails(reference)))
              )
            }
            case _ => successful(Redirect(routes.AttachmentsController.attachmentsDetails(reference)))
          }
        )

    }

  private def renderView(
    c: Case,
    uploadForm: Form[String]
  )(implicit hc: HeaderCarrier, request: AuthenticatedRequest[_]): Future[Html] =
    for {
      attachments <- fileService.getAttachments(c)
    } yield {
      val (applicantFiles, nonApplicantFiles) = attachments.partition(_.operator.isEmpty)
      views.html.partials
        .attachments_details(c, uploadForm, applicantFiles, nonApplicantFiles, tabStartIndexForAttachments)
    }

  private def getCaseAndRenderView(reference: String, page: CaseDetailPage, toHtml: Case => Future[Html])(
    implicit request: AuthenticatedRequest[_]
  ): Future[Result] =
    casesService.getOne(reference).flatMap {
      case Some(c: Case) =>
        toHtml(c).map(html => Ok(views.html.case_details(c, page, html, Some(ActiveTab.Attachments))))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }

  def uploadAttachment(reference: String): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.ADD_ATTACHMENT))
      .async(parse.maxLength(appConfig.fileUploadMaxSize.toLong, parse.multipartFormData)) { implicit request =>
        request.body match {
          case Left(MaxSizeExceeded(_)) =>
            renderErrors(reference, request2Messages(implicitly)("cases.attachment.upload.error.restrictionSize"))
          case Right(multipartForm) =>
            multipartForm match {
              case file: MultipartFormData[TemporaryFile] if file.files.nonEmpty => uploadAndSave(reference, file)
              case _ =>
                renderErrors(reference, request2Messages(implicitly)("cases.attachment.upload.error.mustSelect"))
            }
        }
      }

  private def uploadAndSave(
    reference: String,
    multiPartFormData: MultipartFormData[TemporaryFile]
  )(implicit hc: HeaderCarrier, request: AuthenticatedRequest[_]) =
    multiPartFormData.file("file-input") match {
      case Some(filePart) if filePart.filename.isEmpty =>
        renderErrors(reference, request2Messages(implicitly)("cases.attachment.upload.error.mustSelect"))
      case Some(filePart) if hasInvalidContentType(filePart) =>
        renderErrors(reference, request2Messages(implicitly)("cases.attachment.upload.error.fileType"))
      case Some(filePart) =>
        val fileUpload = FileUpload(
          filePart.ref,
          filePart.filename,
          filePart.contentType.getOrElse(throw new IllegalArgumentException("Missing file type"))
        )
        casesService.getOne(reference).flatMap {
          case Some(c: Case) =>
            casesService
              .addAttachment(c, fileUpload, request.operator)
              .flatMap(_ => successful(Redirect(routes.AttachmentsController.attachmentsDetails(reference))))
          case _ =>
            successful(Ok(views.html.case_not_found(reference)))
        }
      case _ => renderErrors(reference, "expected type file on the form")
    }

  private def renderErrors(
    reference: String,
    errorMessage: String
  )(implicit hc: HeaderCarrier, req: AuthenticatedRequest[_]): Future[Result] =
    getCaseAndRenderView(
      reference,
      CaseDetailPage.ATTACHMENTS, {
        val errors         = Seq(FormError("file-input", errorMessage))
        val formWithErrors = form.copy(errors = errors)
        renderView(_, formWithErrors)
      }
    )

  private def hasInvalidContentType: MultipartFormData.FilePart[TemporaryFile] => Boolean = { f =>
    f.contentType match {
      case Some(c: String) if appConfig.fileUploadMimeTypes.contains(c) => false
      case _                                                            => true
    }
  }

}
