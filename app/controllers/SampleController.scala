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
import models.SampleStatus.SampleStatus
import models._
import models.forms.SampleStatusForm
import models.request.AuthenticatedRequest
import play.api.data.Form
import play.api.mvc._
import play.twirl.api.Html
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SampleController @Inject()(
  override val verify: RequestActions,
  override val caseService: CasesService,
  mcc: MessagesControllerComponents,
  override implicit val config: AppConfig
) extends FrontendController(mcc)
    with StatusChangeAction[Option[SampleStatus]] {

  override protected val requiredPermission: Permission = Permission.EDIT_SAMPLE

  override protected val form: Form[Option[SampleStatus]] = SampleStatusForm.form

  override protected def status(c: Case): Option[SampleStatus] = c.sample.status

  protected def chooseStatusView(c: Case, notFilledForm: Form[Option[SampleStatus]], options: Option[String])(
    implicit request: AuthenticatedRequest[_]
  ): Html =
    c.application.`type` match {
      case ApplicationType.LIABILITY =>
        if (options.contains("liability"))
          views.html.change_liablity_sending_sample(c, notFilledForm)
        else
          views.html.change_sample_status(c, notFilledForm)
      case ApplicationType.CORRESPONDENCE =>
        if (options.contains("correspondence"))
          views.html.change_correspondence_sending_sample(c, notFilledForm)
        else
          views.html.change_sample_status(c, notFilledForm)
      case ApplicationType.MISCELLANEOUS =>
        if (options.contains("correspondence"))
          views.html.change_correspondence_sending_sample(c, notFilledForm)
        else
          views.html.change_sample_status(c, notFilledForm)
      case _ => views.html.change_sample_status(c, notFilledForm)
    }

  override def chooseStatus(reference: String, options: Option[String]): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(requiredPermission)).async { implicit request =>
      getCaseAndRenderView(
        reference,
        c => successful(chooseStatusView(c, form, options))
      )
    }

  override protected def update(c: Case, status: Option[SampleStatus], operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] =
    caseService.updateSampleStatus(c, status, operator)

  override protected def onSuccessRedirect(reference: String): Call =
    controllers.routes.CaseController.sampleDetails(reference)
}
