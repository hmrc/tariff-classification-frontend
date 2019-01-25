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

import javax.inject.{Inject, Singleton}
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.UploadAttachmentFormData
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
class AttachmentsController @Inject()(authenticatedAction: AuthenticatedAction,
                                      casesService: CasesService,
                                      fileService: FileStoreService,
                                      val messagesApi: MessagesApi,
                                      implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private lazy val form: Form[UploadAttachmentFormData] = UploadAttachmentFormData.form

  def attachmentsDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, CaseDetailPage.ATTACHMENTS, c => renderView(c, form))
  }

  private def renderView(c: Case, pForm: Form[_])
                        (implicit hc: HeaderCarrier, request: Request[_]): Future[Html] = {

    for {
      attachments <- fileService.getAttachments(c)
      letter <- fileService.getLetterOfAuthority(c)
    } yield {
      val (applicantFiles, nonApplicantFiles) = attachments.partition(_.operator.isEmpty)
      views.html.partials.attachments_details(c, pForm, applicantFiles, letter, nonApplicantFiles)
    }

  }

  private def renderErrors(reference: String, errorMessage: Option[String])
                          (implicit hc: HeaderCarrier, req: Request[_]): Future[Result] = {
    getCaseAndRenderView(
      reference,
      CaseDetailPage.ATTACHMENTS,
      c => {
        val errors = errorMessage match {
          case Some(s) => Seq(FormError("file-input", s))
          case _ => Seq.empty
        }
        val formWithErrors = form.copy(errors = errors)
        renderView(c, formWithErrors)
      }
    )
  }

  private def uploadAndSave(reference: String, file: MultipartFormData.FilePart[TemporaryFile])
                           (implicit hc: HeaderCarrier, request: AuthenticatedRequest[_]) = {

    casesService.getOne(reference).flatMap {
      case Some(c: Case) =>
        fileService.upload(file).flatMap {
          case fileStored: FileStoreAttachment =>
            val attachments = c.attachments :+ Attachment(id = fileStored.id, operator = Some(request.operator))
            val caseToUpdate = c.copy(attachments = attachments)
            casesService.updateCase(caseToUpdate)
              .flatMap( _ => successful(Redirect(routes.AttachmentsController.attachmentsDetails(reference))))
          case _ => renderErrors(reference, Some("file service has failed while uploading."))
        }
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }

  def uploadAttachment(reference: String): Action[MultipartFormData[TemporaryFile]] =
    authenticatedAction.async(parse.multipartFormData) { implicit request =>

      val attachment = request.body.file("file-input").filter(_.filename.nonEmpty)

      attachment match {
        case Some(file) =>
          valid(file) match {
            case Right(validFile) => uploadAndSave(reference, validFile)
            case Left(errorMessage) => renderErrors(reference, Some(errorMessage))
          }
        case _ => renderErrors(reference, Some("no file provided."))
      }
    }

  private val maxFileSize = 10485760

  private def valid(file: MultipartFormData.FilePart[TemporaryFile]): Either[String, MultipartFormData.FilePart[TemporaryFile]] = {
    if (file.ref.file.length() > maxFileSize) {
      Left(messagesApi("cases.attachment.upload.restrictionSize"))
    } else {
      Right(file)
    }
  }


  private def getCaseAndRenderView(reference: String, page: CaseDetailPage, toHtml: Case => Future[Html])
                                  (implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).flatMap {
      case Some(c: Case) => toHtml(c).map(html => Ok(views.html.case_details(c, page, html)))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }
}
