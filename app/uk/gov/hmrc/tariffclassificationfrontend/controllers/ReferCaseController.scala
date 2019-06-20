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
import play.api.i18n.MessagesApi
import play.api.libs.Files
import play.api.mvc._
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.ReferCaseForm
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.hmrc.tariffclassificationfrontend.models.ReferralReason.ReferralReason
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class ReferCaseController @Inject()(verify: RequestActions,
                                    casesService: CasesService,
                                    val messagesApi: MessagesApi,
                                    implicit val appConfig: AppConfig) extends RenderCaseAction with ExtractableFile with PrefixErrorsInForm[CaseReferral] {

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

    val myForm = (checkReasonIsSelected andThen checkedOtherCommentNotEmpty) (ReferCaseForm.form.bindFromRequest())

    def failWithFormError(error: String): Future[Result] = {
      myForm.fold(
        formWithErrors => getCaseAndRenderErrors(reference, formWithErrors, error),
        referral => getCaseAndRenderErrors(reference, myForm.fill(referral), error)
      )
    }

    def whoIsReferredTo: CaseReferral => String = { c =>
      c.referredTo match {
        case "Other" => c.referManually.getOrElse(c.referredTo)
        case referredTo => referredTo
      }
    }

    def sanityCheckReasons: CaseReferral => Seq[ReferralReason] = { c =>
      c.referredTo match {
        case "Applicant" => c.reasons.map(ReferralReason.withName)
        case _ => Seq.empty
      }
    }

    extractFile(key = "email")(
      onFileValid = validFile => {
        myForm.fold(
          formWithErrors =>
            getCaseAndRenderView(reference, c => successful(views.html.refer_case(c, formWithErrors))),
          referral => {
            validateAndRedirect(casesService.referCase(_, whoIsReferredTo(referral), sanityCheckReasons(referral), validFile,
              referral.note, request.operator).map(c => routes.ReferCaseController.confirmReferCase(c.reference)))
          }
        )
      },
      onFileTooLarge = () => failWithFormError(messagesApi("status.change.upload.error.restrictionSize")),
      onFileInvalidType = () => failWithFormError(messagesApi("status.change.upload.error.fileType")),
      onFileMissing = () => failWithFormError(messagesApi("status.change.upload.error.mustSelect"))
    )
  }

  private def checkReasonIsSelected: PartialFunction[Form[CaseReferral], Form[CaseReferral]] = {
    case f if f.data.get("referredTo").contains("Applicant") && (f.data.get("reasons[0]").isEmpty && f.data.get("reasons[1]").isEmpty) =>
      prefixErrorInForm(f,  FormError("reasons","Select why you are referring this case"))
    case f => f
  }

  private def checkedOtherCommentNotEmpty: PartialFunction[Form[CaseReferral], Form[CaseReferral]] = {
    case f if f.data.get("referredTo").contains("Other") && f.data.getOrElse("referManually", "").isEmpty =>
      prefixErrorInForm(f,  FormError("referManually","Enter who you are referring this case to"))
    case f => f
  }

  private def getCaseAndRenderErrors(reference: String, form: Form[CaseReferral], specificProblem: String)
                                    (implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]]): Future[Result] =
    getCaseAndRenderView(reference, c => successful(views.html.refer_case(c, form.withError("email", specificProblem))))

}
