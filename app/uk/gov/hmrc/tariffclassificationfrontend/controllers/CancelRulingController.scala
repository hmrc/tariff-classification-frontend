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
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.Files
import play.api.mvc._
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.CancelRulingForm
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.{CANCELLED, COMPLETED}
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.{CancelReason, RulingCancellation, Case, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful


@Singleton
class CancelRulingController @Inject()(verify: RequestActions,
                                       casesService: CasesService,
                                       val messagesApi: MessagesApi,
                                       implicit val appConfig: AppConfig) extends RenderCaseAction with ExtractableFile {

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    c.status == COMPLETED && (c.hasLiveRuling || c.application.isLiabilityOrder)
  }

  private def cancelRuling(f: Form[RulingCancellation], caseRef: String)
                          (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    getCaseAndRenderView(caseRef, c => successful(views.html.cancel_ruling(c, f)))
  }

  def getCancelRuling(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(Permission.CANCEL_CASE)).async { implicit request =>
    cancelRuling(CancelRulingForm.form, reference)
  }


  def confirmCancelRuling(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.CANCEL_CASE)).async { implicit request =>
      renderView(c => c.status == CANCELLED , c => successful(views.html.confirm_cancel_ruling(c)))
    }

  def postCancelRuling(reference: String): Action[MultipartFormData[Files.TemporaryFile]] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(Permission.CANCEL_CASE)).async(parse.multipartFormData) { implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]] =>

    extractFile(key = "email")(
      onFileValid = validFile => {
        CancelRulingForm.form.bindFromRequest().fold(
          formWithErrors =>
            getCaseAndRenderView(reference, c => successful(views.html.cancel_ruling(c, formWithErrors))),
          rulingCancellation => {
            validateAndRedirect(casesService.cancelRuling(_, CancelReason.withName(rulingCancellation.cancelReason),
              validFile, rulingCancellation.note, request.operator).map(c => routes.CancelRulingController.confirmCancelRuling(c.reference)))
          }
        )
      },

      onFileTooLarge = () => {
        val error = messagesApi("status.change.upload.error.restrictionSize")
        CancelRulingForm.form.bindFromRequest().fold(
          formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
          rulingCancellation => getCaseAndRenderErrors(reference, CancelRulingForm.form.fill(rulingCancellation), error)
        )
      },

      onFileInvalidType = () => {
        val error = messagesApi("status.change.upload.error.fileType")
        CancelRulingForm.form.bindFromRequest().fold(
          formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
          rulingCancellation => getCaseAndRenderErrors(reference, CancelRulingForm.form.fill(rulingCancellation), error)
        )
      },

      onFileMissing = () => {
        val error = messagesApi("status.change.upload.error.mustSelect")
        CancelRulingForm.form.bindFromRequest().fold(
          formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
          rulingCancellation => getCaseAndRenderErrors(reference, CancelRulingForm.form.fill(rulingCancellation), error))

      }
    )
  }

  private def getCaseAndRenderErrors(reference : String, form: Form[RulingCancellation], specificProblem : String)
                                    (implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]]): Future[Result] =
    getCaseAndRenderView(reference, c => successful(views.html.cancel_ruling(c, form.withError("email", specificProblem))))

}
