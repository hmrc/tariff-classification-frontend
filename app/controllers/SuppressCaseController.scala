/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.{Inject, Singleton}
import models.CaseStatus.SUPPRESSED
import models.Permission
import models.forms.AddNoteForm
import models.request.AuthenticatedCaseRequest
import play.api.data.Form
import play.api.libs.Files
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SuppressCaseController @Inject()(
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc) with RenderCaseAction with ExtractableFile {

  private val form: Form[String] = AddNoteForm.getForm("suppress")

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  def getSuppressCase(reference: String, activeTab: Option[ActiveTab]): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.SUPPRESS_CASE)).async { implicit request =>
      getCaseAndRenderView(reference, c => successful(views.html.suppress_case(c, form, activeTab)))
    }

  def postSuppressCase(reference: String, activeTab: Option[ActiveTab]): Action[MultipartFormData[Files.TemporaryFile]] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.SUPPRESS_CASE))
      .async(parse.multipartFormData) { implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]] =>

        extractFile(key = "email")(
          onFileValid = validFile => {
            form.bindFromRequest().fold(
              formWithErrors =>
                getCaseAndRenderView(reference, c => successful(views.html.suppress_case(c, formWithErrors))),
              note => {
                validateAndRedirect(
                  casesService.suppressCase(_, validFile, note, request.operator)
                    .map(c => routes.SuppressCaseController.confirmSuppressCase(c.reference))
                )
              }
            )
          },

          onFileTooLarge = () => {
            val error = request2Messages(implicitly)("status.change.upload.error.restrictionSize")
            form.bindFromRequest().fold(
              formWithErrors => getCaseAndRenderEmailError(reference, formWithErrors, error, activeTab),
              note => getCaseAndRenderEmailError(reference, form.fill(note), error, activeTab)
            )
          },

          onFileInvalidType = () => {
            val error = request2Messages(implicitly)("status.change.upload.error.fileType")
            form.bindFromRequest().fold(
              formWithErrors => getCaseAndRenderEmailError(reference, formWithErrors, error, activeTab),
              note => getCaseAndRenderEmailError(reference, form.fill(note), error, activeTab)
            )
          },

          onFileMissing = () => {
            val error = request2Messages(implicitly)("status.change.upload.error.mustSelect")
            form.bindFromRequest().fold(
              formWithErrors => getCaseAndRenderEmailError(reference, formWithErrors, error, activeTab),
              note => getCaseAndRenderEmailError(reference, form.fill(note), error, activeTab))

          }
        )
  }

  private def getCaseAndRenderEmailError(
                                          reference: String,
                                          form: Form[String],
                                          error: String,
                                          activeTab: Option[ActiveTab]
                                        )(implicit request: AuthenticatedCaseRequest[_]): Future[Result] =
    getCaseAndRenderView(
      reference,
      c => successful(views.html.suppress_case(c, form.withError("email", error), activeTab))
    )

  def confirmSuppressCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)).async { implicit request =>
      renderView(c => c.status == SUPPRESSED, c => successful(views.html.confirm_supressed_case(c)))
    }

}
