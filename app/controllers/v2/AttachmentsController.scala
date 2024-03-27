/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.v2

import org.apache.pekko.stream.Materializer
import config.AppConfig
import controllers.{RenderCaseAction, RequestActions}
import models._
import models.forms.RemoveAttachmentForm
import models.viewmodels.CaseHeaderViewModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.CasesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.v2.remove_attachment
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AttachmentsController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  mcc: MessagesControllerComponents,
  remove_attachment: remove_attachment,
  implicit val appConfig: AppConfig,
  implicit val mat: Materializer
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with RenderCaseAction
    with I18nSupport {

  private lazy val removeAttachmentForm: Form[Boolean] = RemoveAttachmentForm.form
  override protected val config: AppConfig             = appConfig
  override protected val caseService: CasesService     = casesService

  def removeAttachment(reference: String, fileId: String, fileName: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(
      Permission.REMOVE_ATTACHMENTS
    )).async { implicit request =>
      validateAndRenderView { c =>
        val header = CaseHeaderViewModel.fromCase(c)
        Future(remove_attachment(header, removeAttachmentForm, fileId, fileName))
      }
    }

  def confirmRemoveAttachment(reference: String, fileId: String, fileName: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(
      Permission.REMOVE_ATTACHMENTS
    )).async { implicit request =>
      removeAttachmentForm
        .bindFromRequest()
        .fold(
          errors =>
            validateAndRenderView { c =>
              val header = CaseHeaderViewModel.fromCase(c)
              Future(remove_attachment(header, errors, fileId, fileName))
            }, {
            case true =>
              getCaseAndRespond(
                reference,
                caseService
                  .removeAttachment(_, fileId)
                  .map(_ => Redirect(controllers.routes.CaseController.attachmentsDetails(reference)))
              )
            case _ =>
              Future(
                Redirect(controllers.routes.CaseController.attachmentsDetails(reference))
              )
          }
        )

    }
}
