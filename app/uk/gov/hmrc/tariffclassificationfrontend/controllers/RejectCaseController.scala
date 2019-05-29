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
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, FileUpload, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

//noinspection ScalaStyle
@Singleton
class RejectCaseController @Inject()(verify: RequestActions,
                                     casesService: CasesService,
                                     val messagesApi: MessagesApi,
                                     implicit val appConfig: AppConfig) extends RenderCaseAction {

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService
  private val form: Form[String] = AddNoteForm.form

  def getRejectCase(reference: String): Action[AnyContent] = (verify.authenticated
    andThen verify.casePermissions(reference)
    andThen verify.mustHave(Permission.REJECT_CASE)).async { implicit request =>
    validateAndRenderView(c => successful(views.html.reject_case(c, form)))
  }


  def confirmRejectCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.REJECT_CASE)).async { implicit request =>
      renderView(c => c.status == REJECTED, c => successful(views.html.confirm_rejected(c)))
    }

  def postRejectCase(reference: String): Action[MultipartFormData[Files.TemporaryFile]] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(Permission.REJECT_CASE)).async(parse.multipartFormData) { implicit request: AuthenticatedCaseRequest[MultipartFormData[Files.TemporaryFile]] =>
    // Get the file from the request (filename check ensures it has been selected)
    request.body.file("file-input").filter(_.filename.nonEmpty).filter(_.contentType.isDefined) match {
      case Some(file) =>
        form.bindFromRequest().fold(
          errors =>
            getCaseAndRenderView(reference, c => successful(views.html.reject_case(c, errors))),
          note => {
            val fileUpload = FileUpload(file.ref, file.filename, file.contentType.get)
            validateAndRedirect(casesService.rejectCase(_, fileUpload, note, request.operator).map(c => routes.RejectCaseController.confirmRejectCase(reference)))
          }
        )

      case None =>
        def getCaseAndRenderErrors(form: Form[String]): Future[Result] = getCaseAndRenderView(reference,
          c => successful(views.html.reject_case(c, form.withError("file-input", "You must upload an email"))))

        form.bindFromRequest().fold(
          errors => getCaseAndRenderErrors(errors),
          note => getCaseAndRenderErrors(form.fill(note))
        )
    }
  }

  override protected def redirect: String => Call = routes.CaseController.applicationDetails

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = c.status == OPEN

}
