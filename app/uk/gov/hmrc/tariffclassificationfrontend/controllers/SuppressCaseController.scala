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
import uk.gov.hmrc.tariffclassificationfrontend.forms.AddNoteForm
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.{NEW, SUPPRESSED}
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SuppressCaseController @Inject()(verify: RequestActions,
                                       casesService: CasesService,
                                       val messagesApi: MessagesApi,
                                       implicit val appConfig: AppConfig) extends RenderCaseAction with ExtractableFile {

  private val form: Form[String] = AddNoteForm.getForm("suppress")

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  override protected def redirect: String => Call = routes.CaseController.applicationDetails
  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = true

  def getSuppressCase(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(Permission.SUPPRESS_CASE)).async { implicit request =>
    getCaseAndRenderView(reference, c => successful(views.html.suppress_case(c, form)))
  }

  def postSuppressCase(reference: String): Action[MultipartFormData[Files.TemporaryFile]] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.SUPPRESS_CASE))
      .async(parse.multipartFormData) { implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]] =>

        extractFile(key = "email")(
          onFileValid = validFile => {
            form.bindFromRequest().fold(
              formWithErrors =>
                getCaseAndRenderView(reference, c => successful(views.html.suppress_case(c, formWithErrors))),
              note => {
                validateAndRedirect(casesService.suppressCase(_, validFile, note, request.operator).map(c => routes.SuppressCaseController.confirmSuppressCase(c.reference)))
              }
            )
          },

          onFileTooLarge = () => {
            val error = messagesApi("status.change.upload.error.restrictionSize")
            form.bindFromRequest().fold(
              formWithErrors => getCaseAndRenderEmailError(reference, formWithErrors, error),
              note => getCaseAndRenderEmailError(reference, form.fill(note), error)
            )
          },

          onFileInvalidType = () => {
            val error = messagesApi("status.change.upload.error.fileType")
            form.bindFromRequest().fold(
              formWithErrors => getCaseAndRenderEmailError(reference, formWithErrors, error),
              note => getCaseAndRenderEmailError(reference, form.fill(note), error)
            )
          },

          onFileMissing = () => {
            val error = messagesApi("status.change.upload.error.mustSelect")
            form.bindFromRequest().fold(
              formWithErrors => getCaseAndRenderEmailError(reference, formWithErrors, error),
              note => getCaseAndRenderEmailError(reference, form.fill(note), error))

          }
        )
  }

  private def getCaseAndRenderEmailError(reference: String, form: Form[String], error: String)(implicit request: AuthenticatedCaseRequest[_]): Future[Result] = getCaseAndRenderView(
    reference,
    c => successful(views.html.suppress_case(c, form.withError("email", error)))
  )

  def confirmSuppressCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)).async { implicit request =>
      renderView(c => c.status == SUPPRESSED, c => successful(views.html.confirm_supressed_case(c)))
    }

}
