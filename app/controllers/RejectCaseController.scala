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
import models.forms.{AddNoteForm, RejectCaseForm}

import javax.inject.{Inject, Singleton}
import models.CaseStatus._
import models.{RejectReason, CaseRejection, Permission}
import models.request.AuthenticatedCaseRequest
import play.api.data.Form
import play.api.libs.Files
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class RejectCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with RenderCaseAction
    with ExtractableFile {

  override protected val config: AppConfig         = appConfig
  override protected val caseService: CasesService = casesService

  def getRejectCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.REJECT_CASE)).async { implicit request =>
      validateAndRenderView(c => successful(views.html.reject_case(c, RejectCaseForm.form)))
    }

  def confirmRejectCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)).async { implicit request =>
      renderView(c => c.status == REJECTED, c => successful(views.html.confirm_rejected(c)))
    }

  def postRejectCase(reference: String): Action[MultipartFormData[Files.TemporaryFile]] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.REJECT_CASE)).async(parse.multipartFormData) {
      implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]] =>
        extractFile(key = "file-input")(
          onFileValid = validFile => {
            RejectCaseForm.form
              .bindFromRequest()
              .fold(
                formWithErrors =>
                  getCaseAndRenderView(
                    reference,
                    c => successful(views.html.reject_case(c, formWithErrors))
                  ),
                caseRejection =>
                  validateAndRedirect(
                    casesService
                      .rejectCase(_, RejectReason.withName(caseRejection.reason), validFile, caseRejection.note, request.operator)
                      .map(c => routes.RejectCaseController.confirmRejectCase(c.reference))
                  )
              )
          },
          onFileTooLarge = () => {
            val error = request2Messages(implicitly)("status.change.upload.error.restrictionSize")
            RejectCaseForm.form
              .bindFromRequest()
              .fold(
                formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
                caseRejection => getCaseAndRenderErrors(reference, RejectCaseForm.form.fill(caseRejection), error)
              )
          },
          onFileInvalidType = () => {
            val error = request2Messages(implicitly)("status.change.upload.error.fileType")
            RejectCaseForm.form
              .bindFromRequest()
              .fold(
                formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
                note => getCaseAndRenderErrors(reference, RejectCaseForm.form.fill(note), error)
              )
          },
          onFileMissing = () => {
            val error = request2Messages(implicitly)("status.change.upload.case_reject.error.mustSelect")
            RejectCaseForm.form
              .bindFromRequest()
              .fold(
                formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
                note => getCaseAndRenderErrors(reference, RejectCaseForm.form.fill(note), error)
              )

          }
        )
    }

  private def getCaseAndRenderErrors(reference: String, form: Form[CaseRejection], specificProblem: String)(
    implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]]
  ): Future[Result] =
    getCaseAndRenderView(
      reference,
      c => successful(views.html.reject_case(c, form.withError("file-input", specificProblem)))
    )

}
