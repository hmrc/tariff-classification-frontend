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

package controllers.v2

import akka.stream.Materializer
import config.AppConfig
import controllers.{RenderCaseAction, RequestActions}
import javax.inject.{Inject, Singleton}
import models._
import models.forms.{RemoveAttachmentForm, UploadAttachmentForm}
import models.request.AuthenticatedCaseRequest
import models.viewmodels.CaseHeaderViewModel
import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class AttachmentsController @Inject()(
                                       verify: RequestActions,
                                       casesService: CasesService,
                                       fileService: FileStoreService,
                                       mcc: MessagesControllerComponents,
                                       liabilityController: LiabilityController,
                                       remove_attachment: views.html.v2.remove_attachment,
                                       implicit val appConfig: AppConfig,
                                       implicit val mat: Materializer
                                     ) extends FrontendController(mcc) with RenderCaseAction with I18nSupport {

  private lazy val removeAttachmentForm: Form[Boolean] = RemoveAttachmentForm.form
  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  def removeAttachment(reference: String, fileId: String, fileName: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.REMOVE_ATTACHMENTS)).async { implicit request =>
      validateAndRenderView(
        c => {
          val header = CaseHeaderViewModel.fromCase(c)
          successful(remove_attachment(header, removeAttachmentForm, fileId, fileName))
        }
      )
    }

  def confirmRemoveAttachment(reference: String, fileId: String, fileName: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.REMOVE_ATTACHMENTS)).async { implicit request =>
      removeAttachmentForm.bindFromRequest().fold(
        errors => {
          validateAndRenderView(c => {
            val header = CaseHeaderViewModel.fromCase(c)

            successful(remove_attachment(header, errors, fileId, fileName))
          })
        },
        {
          case true =>
            getCaseAndRespond(reference,
              caseService.removeAttachment(_, fileId)
                .map(_ => Redirect(controllers.v2.routes.LiabilityController.displayLiability(reference))))
          case _ =>
            successful(Redirect(controllers.v2.routes.LiabilityController.displayLiability(reference)))
        }
      )

    }

  def uploadAttachment(reference: String): Action[Either[MaxSizeExceeded, MultipartFormData[TemporaryFile]]] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.ADD_ATTACHMENT))
      .async(parse.maxLength(appConfig.fileUploadMaxSize, parse.multipartFormData)) {

        implicit request =>

          request.body match {
            case Left(MaxSizeExceeded(_)) =>
              renderAttachmentsErrors(reference, request2Messages(implicitly)("cases.attachment.upload.error.restrictionSize"))
            case Right(multipartForm) =>
              multipartForm match {
                case file: MultipartFormData[TemporaryFile] if file.files.nonEmpty =>
                  uploadAndSave(reference, file)
                case _ =>
                  renderAttachmentsErrors(reference, request2Messages(implicitly)("cases.attachment.upload.error.mustSelect"))
              }
          }
      }

  private def uploadAndSave(reference: String, multiPartFormData: MultipartFormData[TemporaryFile])
                           (implicit hc: HeaderCarrier, request: AuthenticatedCaseRequest[_]): Future[Result] = {

    multiPartFormData.file("file-input") match {
      case Some(filePart) if filePart.filename.isEmpty =>
        renderAttachmentsErrors(reference, request2Messages(implicitly)("cases.attachment.upload.error.mustSelect"))
      case Some(filePart) if hasInvalidContentType(filePart) =>
        renderAttachmentsErrors(reference, request2Messages(implicitly)("cases.attachment.upload.error.fileType"))
      case Some(filePart) =>
        val fileUpload = FileUpload.fromFilePart(filePart)
        casesService.getOne(reference).flatMap {
          case Some(c: Case) =>
            casesService.addAttachment(c, fileUpload, request.operator)
              .map(_ => Redirect(controllers.v2.routes.LiabilityController.displayLiability(reference)))
          case _ =>
            successful(Ok(views.html.case_not_found(reference)))
        }
      case _ =>
        renderAttachmentsErrors(reference, "expected type file on the form")
    }
  }

  private def hasInvalidContentType: MultipartFormData.FilePart[TemporaryFile] => Boolean = { f =>
    f.contentType match {
      case Some(c: String) if appConfig.fileUploadMimeTypes.contains(c) => false
      case _ => true
    }
  }

  private def renderAttachmentsErrors(reference: String, errorMessage: String)
                                     (implicit hc: HeaderCarrier, request: AuthenticatedCaseRequest[_]): Future[Result] = {
    val errors = Seq(FormError("file-input", errorMessage))
    val formWithErrors = UploadAttachmentForm.form.copy(errors = errors)
    liabilityController.buildLiabilityView(uploadAttachmentForm = formWithErrors)
  }

}
