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

import config.AppConfig
import controllers.v2.UpscanErrorHandling
import models.forms.AddNoteForm
import models.request.FileStoreInitiateRequest
import javax.inject.{Inject, Singleton}
import models.CaseStatus.SUSPENDED
import models.Permission
import models.forms.{SuspendCaseForm, UploadAttachmentForm}
import models.request.AuthenticatedCaseRequest
import play.api.data.Form
import play.api.libs.Files
import play.api.libs.json._
import play.api.mvc._
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.JsonFormatters._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.successful
import play.twirl.api.HtmlFormat
import models.request.AuthenticatedRequest
import uk.gov.hmrc.http.HeaderCarrier
import models.CaseSuspension
import views.html.suspend_case
import java.util.UUID

@Singleton
class SuspendCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  fileService: FileStoreService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with RenderCaseAction
    with UpscanErrorHandling {

  override protected val config: AppConfig         = appConfig
  override protected val caseService: CasesService = casesService
  private val uploadForm                           = UploadAttachmentForm.form

  def getSuspendCase(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.SUSPEND_CASE))
      .async(implicit request => handleUploadErrorAndRender(renderView(fileId = fileId, SuspendCaseForm.form, _)))

  def renderView(
    fileId: Option[String] = None,
    suspendForm: Form[CaseSuspension],
    uploadForm: Form[String] = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[HtmlFormat.Appendable] = {
    val caseToSuspend = request.`case`
    val uploadFileId  = fileId.getOrElse(UUID.randomUUID().toString)

    val fileStoreSuccessRedirect =
      appConfig.host + routes.SuspendCaseController.confirmSuspendCase(request.`case`.reference).path

    val fileStoreErrorRedirect =
      appConfig.host + routes.SuspendCaseController.getSuspendCase(request.`case`.reference).path

    fileService
      .initiate(
        FileStoreInitiateRequest(
          id              = Some(uploadFileId),
          // successRedirect = Some(fileStoreSuccessRedirect),
          // errorRedirect   = Some(fileStoreErrorRedirect),
          publishable     = true,
          maxFileSize     = appConfig.fileUploadMaxSize
        )
      )
      .flatMap { initiateResponse =>
        successful(views.html.suspend_case(caseToSuspend, suspendForm, uploadForm, initiateResponse))
      }
  }

  def postSuspendNote(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.SUSPEND_CASE)) {
      implicit request =>
        AddNoteForm
          .getForm("suspend")
          .bindFromRequest()
          .fold(
            form => BadRequest(form.errorsAsJson),
            note => Ok(Json.toJson(note))
          )
    }

  def postSuspendCase(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.SUSPEND_CASE))
      .async { implicit request =>
        SuspendCaseForm.form
          .bindFromRequest()
          .fold(
            formWithErrors => renderView(suspendForm = formWithErrors).map(BadRequest(_)),
            suspension =>
              validateAndRedirect(
                casesService
                  .suspendCase(_, suspension.attachment, suspension.note, request.operator)
                  .map(c => routes.SuspendCaseController.confirmSuspendCase(c.reference))
              )
          )
      }

  def confirmSuspendCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)).async { implicit request =>
      renderView(c => c.status == SUSPENDED, c => successful(views.html.confirm_suspended(c)))
    }
}
