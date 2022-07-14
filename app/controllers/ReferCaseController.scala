/*
 * Copyright 2022 HM Revenue & Customs
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
import models.forms.{ReferCaseForm, UploadAttachmentForm}
import models.request.{AuthenticatedCaseRequest, FileStoreInitiateRequest}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.JsonFormatters._
import views.html.{confirm_refer_case, refer_case_email, refer_case_reason}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReferCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  fileService: FileStoreService,
  dataCacheConnector: DataCacheConnector,
  mcc: MessagesControllerComponents,
  val refer_case_reason: refer_case_reason,
  val refer_case_email: refer_case_email,
  val confirm_refer_case: confirm_refer_case,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with UpscanErrorHandling with WithDefaultFormBinding {

  private val ReferralCacheKey = "referral"
  private def cacheKey(reference: String) =
    s"refer_case-$reference"

  def getReferCaseReason(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.REFER_CASE)) {
      implicit request => Ok(refer_case_reason(request.`case`, ReferCaseForm.form))
    }

  //TODO Form binding needs reworking, currently optional fields generate multiple errors for the refferTo field
  def postReferCaseReason(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.REFER_CASE))
      .async { implicit request =>
        ReferCaseForm.form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              successful(BadRequest(refer_case_reason(request.`case`, formWithErrors)))
            } ,
            referral => {
              val userAnswers        = UserAnswers(cacheKey(reference))
              val updatedUserAnswers = userAnswers.set(ReferralCacheKey, referral)
              dataCacheConnector
                .save(updatedUserAnswers.cacheMap)
                .map(_ => Redirect(routes.ReferCaseController.getReferCaseEmail(reference)))
            }
          )
      }

  def renderReferCaseEmail(
    fileId: Option[String]   = None,
    uploadForm: Form[String] = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Html] = {
    val uploadFileId = fileId.getOrElse(UUID.randomUUID().toString)

    val fileUploadSuccessRedirect =
      appConfig.host + controllers.routes.ReferCaseController
        .referCase(request.`case`.reference, uploadFileId)
        .path

    val fileUploadErrorRedirect =
      appConfig.host + controllers.routes.ReferCaseController
        .getReferCaseEmail(request.`case`.reference, Some(uploadFileId))
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
      .map(initiateResponse => refer_case_email(request.`case`, uploadForm, initiateResponse))
  }

  def getReferCaseEmail(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.REFER_CASE))
      .async(implicit request => handleUploadErrorAndRender(uploadForm => renderReferCaseEmail(fileId, uploadForm)))

  def referCase(reference: String, fileId: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.REFER_CASE) andThen
      verify.requireCaseData(reference, cacheKey(reference))).async { implicit request =>
      request.userAnswers
        .get[CaseReferral](ReferralCacheKey)
        .map { referral =>
          val referredTo =
            if (referral.referredTo.equalsIgnoreCase("Other")) {
              referral.referManually.getOrElse(referral.referredTo)
            } else {
              referral.referredTo
            }

          val referralReasons =
            referral.reasons.filter(_ => referredTo.equalsIgnoreCase("Applicant"))

          for {
            _ <- casesService
                  .referCase(
                    request.`case`,
                    referredTo,
                    referralReasons,
                    Attachment(id = fileId, operator = Some(request.operator)),
                    referral.note,
                    request.operator
                  )

            _ <- dataCacheConnector.remove(request.userAnswers.cacheMap)

          } yield Redirect(routes.ReferCaseController.confirmReferCase(reference))
        }
        .getOrElse {
          successful(Redirect(routes.SecurityController.unauthorized()))
        }
    }

  def confirmReferCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)) { implicit request =>
      Ok(confirm_refer_case(request.`case`))
    }
}
