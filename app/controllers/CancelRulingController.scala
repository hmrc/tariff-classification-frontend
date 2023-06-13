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

import config.AppConfig
import connector.DataCacheConnector
import controllers.v2.UpscanErrorHandling
import models._
import models.forms.{CancelRulingForm, UploadAttachmentForm}
import models.request.{AuthenticatedCaseRequest, FileStoreInitiateRequest}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.JsonFormatters._
import views.html.{cancel_ruling_email, cancel_ruling_reason, confirm_cancel_ruling}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CancelRulingController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  fileService: FileStoreService,
  dataCacheConnector: DataCacheConnector,
  mcc: MessagesControllerComponents,
  val cancel_ruling_reason: cancel_ruling_reason,
  val cancel_ruling_email: cancel_ruling_email,
  val confirm_cancel_ruling: confirm_cancel_ruling,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with UpscanErrorHandling
    with WithUnsafeDefaultFormBinding {

  private val CancellationCacheKey = "cancellation"
  private def cacheKey(reference: String) =
    s"cancel_ruling-$reference"

  def getCancelRulingReason(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.CANCEL_CASE)) {
      implicit request => Ok(cancel_ruling_reason(request.`case`, CancelRulingForm.form))
    }

  def postCancelRulingReason(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.CANCEL_CASE))
      .async { implicit request =>
        CancelRulingForm.form
          .bindFromRequest()
          .fold(
            formWithErrors => successful(BadRequest(cancel_ruling_reason(request.`case`, formWithErrors))),
            cancellation => {
              val userAnswers        = UserAnswers(cacheKey(reference))
              val updatedUserAnswers = userAnswers.set(CancellationCacheKey, cancellation)
              dataCacheConnector
                .save(updatedUserAnswers.cacheMap)
                .map(_ => Redirect(routes.CancelRulingController.getCancelRulingEmail(reference)))
            }
          )
      }

  private def renderCancelRulingEmail(
    fileId: Option[String]   = None,
    uploadForm: Form[String] = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Html] = {
    val uploadFileId = fileId.getOrElse(UUID.randomUUID().toString)

    val fileUploadSuccessRedirect =
      appConfig.host + controllers.routes.CancelRulingController
        .cancelRuling(request.`case`.reference, uploadFileId)
        .path

    val fileUploadErrorRedirect =
      appConfig.host + controllers.routes.CancelRulingController
        .getCancelRulingEmail(request.`case`.reference, Some(uploadFileId))
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
      .map(initiateResponse => cancel_ruling_email(request.`case`, uploadForm, initiateResponse))
  }

  def getCancelRulingEmail(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.CANCEL_CASE))
      .async { implicit request =>
        handleUploadErrorAndRender(uploadForm => renderCancelRulingEmail(fileId, uploadForm))
      }

  def cancelRuling(reference: String, fileId: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.CANCEL_CASE) andThen
      verify.requireCaseData(reference, cacheKey(reference))).async { implicit request =>
      request.userAnswers
        .get[RulingCancellation](CancellationCacheKey)
        .map { rulingCancellation =>
          for {
            _ <- casesService
                  .cancelRuling(
                    request.`case`,
                    CancelReason.withName(rulingCancellation.cancelReason),
                    Attachment(id = fileId, operator = Some(request.operator)),
                    rulingCancellation.note,
                    request.operator
                  )

            _ <- dataCacheConnector.remove(request.userAnswers.cacheMap)

          } yield Redirect(routes.CancelRulingController.confirmCancelRuling(reference))
        }
        .getOrElse {
          successful(Redirect(routes.SecurityController.unauthorized()))
        }
    }

  def confirmCancelRuling(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES))(implicit request => Ok(confirm_cancel_ruling(request.`case`)))
}
