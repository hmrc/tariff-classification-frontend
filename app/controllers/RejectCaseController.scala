/*
 * Copyright 2024 HM Revenue & Customs
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
import connector.DataCacheConnector
import controllers.v2.UpscanErrorHandling
import models.forms.{RejectCaseForm, UploadAttachmentForm}
import models.request.{AuthenticatedCaseRequest, FileStoreInitiateRequest}
import models.{Attachment, CaseRejection, Permission, UserAnswers}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.JsonFormatters._
import views.html.{confirm_rejected, reject_case_email, reject_case_reason}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RejectCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  fileService: FileStoreService,
  dataCacheConnector: DataCacheConnector,
  mcc: MessagesControllerComponents,
  val reject_case_reason: reject_case_reason,
  val reject_case_email: reject_case_email,
  val confirm_rejected: confirm_rejected,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with UpscanErrorHandling
    with WithUnsafeDefaultFormBinding {

  private val RejectionCacheKey = "rejection"
  private def cacheKey(reference: String) =
    s"reject_case-$reference"

  def getRejectCaseReason(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.REJECT_CASE)) { implicit request =>
      Ok(reject_case_reason(request.`case`, RejectCaseForm.form))
    }

  def postRejectCaseReason(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.REJECT_CASE)).async { implicit request =>
      RejectCaseForm.form
        .bindFromRequest()
        .fold(
          formWithErrors => successful(BadRequest(reject_case_reason(request.`case`, formWithErrors))),
          caseRejection => {
            val userAnswers        = UserAnswers(cacheKey(reference))
            val updatedUserAnswers = userAnswers.set(RejectionCacheKey, caseRejection)
            dataCacheConnector
              .save(updatedUserAnswers.cacheMap)
              .map(_ => Redirect(routes.RejectCaseController.getRejectCaseEmail(reference)))
          }
        )
    }

  private def renderRejectCaseEmail(
    fileId: Option[String]   = None,
    uploadForm: Form[String] = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Html] = {
    val uploadFileId = fileId.getOrElse(UUID.randomUUID().toString)

    val fileUploadSuccessRedirect =
      appConfig.host + controllers.routes.RejectCaseController
        .rejectCase(request.`case`.reference, uploadFileId)
        .path

    val fileUploadErrorRedirect =
      appConfig.host + controllers.routes.RejectCaseController
        .getRejectCaseEmail(request.`case`.reference, Some(uploadFileId))
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
      .map(initiateResponse => reject_case_email(request.`case`, uploadForm, initiateResponse))
  }

  def getRejectCaseEmail(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.REJECT_CASE))
      .async(implicit request => handleUploadErrorAndRender(uploadForm => renderRejectCaseEmail(fileId, uploadForm)))

  def rejectCase(reference: String, fileId: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.REJECT_CASE) andThen
      verify.requireCaseData(reference, cacheKey(reference))).async { implicit request =>
      request.userAnswers
        .get[CaseRejection](RejectionCacheKey)
        .map { caseRejection =>
          for {
            _ <- casesService
                  .rejectCase(
                    request.`case`,
                    caseRejection.reason,
                    Attachment(id = fileId, operator = Some(request.operator)),
                    caseRejection.note,
                    request.operator
                  )

            _ <- dataCacheConnector.remove(request.userAnswers.cacheMap)

          } yield Redirect(routes.RejectCaseController.confirmRejectCase(reference))
        }
        .getOrElse {
          successful(Redirect(routes.SecurityController.unauthorized()))
        }
    }

  def confirmRejectCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES))(implicit request => Ok(confirm_rejected(request.`case`)))
}
