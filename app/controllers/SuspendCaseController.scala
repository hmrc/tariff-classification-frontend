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

import java.util.UUID
import javax.inject.{Inject, Singleton}

import connector.DataCacheConnector
import config.AppConfig
import controllers.v2.UpscanErrorHandling
import models.{Attachment, Permission, UserAnswers}
import models.forms.{AddNoteForm, UploadAttachmentForm}
import models.request.{AuthenticatedCaseRequest, FileStoreInitiateRequest}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.successful

@Singleton
class SuspendCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  fileService: FileStoreService,
  dataCacheConnector: DataCacheConnector,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with UpscanErrorHandling {

  private val NoteCacheKey = "note"
  private def cacheKey(reference: String) =
    s"suspend_case-$reference"

  def getSuspendCaseReason(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.SUSPEND_CASE)) { implicit request =>
      Ok(views.html.suspend_case_reason(request.`case`, AddNoteForm.getForm("suspend")))
    }

  def postSuspendCaseReason(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.SUSPEND_CASE))
      .async { implicit request =>
        AddNoteForm
          .getForm("suspend")
          .bindFromRequest()
          .fold(
            formWithErrors => successful(BadRequest(views.html.suspend_case_reason(request.`case`, formWithErrors))),
            note => {
              val userAnswers        = UserAnswers(cacheKey(reference))
              val updatedUserAnswers = userAnswers.set(NoteCacheKey, note)
              dataCacheConnector
                .save(updatedUserAnswers.cacheMap)
                .map(_ => Redirect(routes.SuspendCaseController.getSuspendCaseEmail(reference)))
            }
          )
      }

  def renderSuspendCaseEmail(
    fileId: Option[String]   = None,
    uploadForm: Form[String] = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Html] = {
    val uploadFileId = fileId.getOrElse(UUID.randomUUID().toString)

    val fileUploadSuccessRedirect =
      appConfig.host + controllers.routes.SuspendCaseController
        .suspendCase(request.`case`.reference, uploadFileId)
        .path

    val fileUploadErrorRedirect =
      appConfig.host + controllers.routes.SuspendCaseController
        .getSuspendCaseEmail(request.`case`.reference, Some(uploadFileId))
        .path

    fileService
      .initiate(
        FileStoreInitiateRequest(
          id              = Some(uploadFileId),
          successRedirect = Some(fileUploadSuccessRedirect),
          errorRedirect   = Some(fileUploadErrorRedirect),
          maxFileSize     = appConfig.fileUploadMaxSize
        )
      )
      .map(initiateResponse => views.html.suspend_case_email(request.`case`, uploadForm, initiateResponse))
  }

  def getSuspendCaseEmail(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.SUSPEND_CASE))
      .async(implicit request => handleUploadErrorAndRender(uploadForm => renderSuspendCaseEmail(fileId, uploadForm)))

  def suspendCase(reference: String, fileId: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.SUSPEND_CASE) andThen
      verify.requireCaseData(reference, cacheKey(reference))).async { implicit request =>
      request.userAnswers
        .get[String](NoteCacheKey)
        .map { note =>
          for {
            _ <- casesService
                  .suspendCase(
                    request.`case`,
                    Attachment(id = fileId, operator = Some(request.operator)),
                    note,
                    request.operator
                  )

            _ <- dataCacheConnector.remove(request.userAnswers.cacheMap)

          } yield Redirect(routes.SuspendCaseController.confirmSuspendCase(reference))
        }
        .getOrElse {
          successful(Redirect(routes.SecurityController.unauthorized()))
        }
    }

  def confirmSuspendCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)) { implicit request =>
      Ok(views.html.confirm_suspended(request.`case`))
    }
}
