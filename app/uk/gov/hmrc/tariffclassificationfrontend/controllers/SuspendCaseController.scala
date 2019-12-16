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
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.{OPEN, SUSPENDED}
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SuspendCaseController @Inject()(verify: RequestActions,
                                      casesService: CasesService,
                                      val messagesApi: MessagesApi,
                                      implicit val appConfig: AppConfig) extends RenderCaseAction with ExtractableFile {

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService
  private val form: Form[String] = AddNoteForm.getForm("suspend")

  def getSuspendCase(reference: String, activeTab: Option[String]): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.SUSPEND_CASE)).async { implicit request =>
    validateAndRenderView(c => successful(views.html.suspend_case(c, form, activeTab.map(ActiveTab(_)))))
  }

  def postSuspendCase(reference: String, activeTab: Option[String]): Action[MultipartFormData[Files.TemporaryFile]] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.SUSPEND_CASE)).async(parse.multipartFormData) { implicit request =>
    extractFile(key = "email")(
      onFileValid = validFile => {
        form.bindFromRequest().fold(
          formWithErrors =>
            getCaseAndRenderView(reference, c => successful(views.html.suspend_case(c, formWithErrors, activeTab.map(ActiveTab(_))))),
          note => {
            validateAndRedirect(casesService.suspendCase(_, validFile, note, request.operator).map(c => routes.SuspendCaseController.confirmSuspendCase(c.reference)))
          }
        )
      },

      onFileTooLarge = () => {
        val error = messagesApi("status.change.upload.error.restrictionSize")
        form.bindFromRequest().fold(
          formWithErrors => getCaseAndRenderEmailError(reference, formWithErrors, error, activeTab),
          note => getCaseAndRenderEmailError(reference, form.fill(note), error, activeTab)
        )
      },

      onFileInvalidType = () => {
        val error = messagesApi("status.change.upload.error.fileType")
        form.bindFromRequest().fold(
          formWithErrors => getCaseAndRenderEmailError(reference, formWithErrors, error, activeTab),
          note => getCaseAndRenderEmailError(reference, form.fill(note), error, activeTab)
        )
      },

      onFileMissing = () => {
        val error = messagesApi("status.change.upload.error.mustSelect")
        form.bindFromRequest().fold(
          formWithErrors => getCaseAndRenderEmailError(reference, formWithErrors, error, activeTab),
          note => getCaseAndRenderEmailError(reference, form.fill(note), error, activeTab))

      }
    )
  }

  private def getCaseAndRenderEmailError(reference: String, form: Form[String], error: String, activeTab: Option[String])(implicit request: AuthenticatedCaseRequest[_]): Future[Result] = getCaseAndRenderView(
    reference,
    c => successful(views.html.suspend_case(c, form.withError("email", error), activeTab.map(ActiveTab(_))))
  )

  def confirmSuspendCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)).async { implicit request =>
      renderView(c => c.status == SUSPENDED, c => successful(views.html.confirm_suspended(c)))
    }
}
