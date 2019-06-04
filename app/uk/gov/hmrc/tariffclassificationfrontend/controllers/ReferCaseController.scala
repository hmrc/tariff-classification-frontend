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
import uk.gov.hmrc.tariffclassificationfrontend.forms.{CancelRulingForm, MandatoryBooleanForm, ReferCaseForm}
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.{CancelReason, Case, CaseReferral, Permission, RulingCancellation}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class ReferCaseController @Inject()(verify: RequestActions,
                                    casesService: CasesService,
                                    val messagesApi: MessagesApi,
                                    implicit val appConfig: AppConfig) extends RenderCaseAction with ExtractableFile {

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  def getReferCase(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(Permission.REFER_CASE)).async { implicit request =>
    validateAndRenderView(
      c =>
        successful(views.html.refer_case(c, ReferCaseForm.form))
    )
  }

  def confirmReferCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.REFER_CASE)).async { implicit request =>
      renderView(c => c.status == REFERRED, c => successful(views.html.confirm_refer_case(c)))
    }

  def postReferCase(reference: String): Action[MultipartFormData[Files.TemporaryFile]] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(Permission.REFER_CASE)).async(parse.multipartFormData) { implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]] =>

    val myForm = addErrorsToForm(ReferCaseForm.form.bindFromRequest())

    extractFile(key = "email")(
      onFileValid = validFile => {
        myForm.fold(
          formWithErrors =>
            getCaseAndRenderView(reference, c => successful(views.html.refer_case(c, formWithErrors))),
          referral => {
            validateAndRedirect(casesService.referCase(_, referral.referredTo, Seq.empty, validFile,
              referral.note, request.operator).map(c => routes.ReferCaseController.confirmReferCase(c.reference)))
          }
        )
      },

      onFileTooLarge = () => {
        val error = messagesApi("status.change.upload.error.restrictionSize")
        myForm.fold(
          formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
          referral => getCaseAndRenderErrors(reference, myForm.fill(referral), error)
        )
      },

      onFileInvalidType = () => {
        val error = messagesApi("status.change.upload.error.fileType")
        myForm.fold(
          formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
          referral => getCaseAndRenderErrors(reference, myForm.fill(referral), error)
        )
      },

      onFileMissing = () => {
        val error = messagesApi("status.change.upload.error.mustSelect")
        myForm.fold(
          formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
          referral => getCaseAndRenderErrors(reference, myForm.fill(referral), error))

      }
    )
  }

  private def addErrorsToForm(f: Form[CaseReferral]): Form[CaseReferral] = {
    var form = f
    if(f.data.get("referredTo").get == "APPLICANT" && (f.data.get("reasons[0]").isEmpty && f.data.get("reasons[1]").isEmpty)) {
      form = f.withError("reasons","Select a reason you are referring this case")
    }

    if(f.data.get("referredTo").get == "OTHER" && f.data.get("other").getOrElse("").trim.isEmpty) {
      form = f.withError("other","Enter who you are referring this case to")
    }

    form
  }

  private def getCaseAndRenderErrors(reference : String, form: Form[CaseReferral], specificProblem : String)
                                    (implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]]): Future[Result] =
    getCaseAndRenderView(reference, c => successful(views.html.refer_case(c, form.withError("email", specificProblem))))

  override protected def redirect: String => Call = routes.CaseController.applicationDetails

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = c.status == OPEN

}
