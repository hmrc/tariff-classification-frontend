/*
 * Copyright 2022 HM Revenue & Customs
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
import models.SampleSend.SampleSend
import models._
import models.forms.SampleSendForm
import models.request.AuthenticatedRequest
import play.api.data.Form
import play.api.mvc._
import play.twirl.api.Html
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.WithDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.change_sample_send

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SampleWhoSendingController @Inject()(
  override val verify: RequestActions,
  override val caseService: CasesService,
  val change_sample_send: change_sample_send,
  mcc: MessagesControllerComponents,
  override implicit val config: AppConfig
) extends FrontendController(mcc)
    with StatusChangeAction[Option[SampleSend]] with WithDefaultFormBinding {

  override protected val requiredPermission: Permission = Permission.EDIT_SAMPLE

  override protected val form: Form[Option[SampleSend]] = SampleSendForm.form

  override protected def status(c: Case): Option[SampleSend] = c.sample.whoIsSending

  override protected def chooseStatusView(
    c: Case,
    notFilledForm: Form[Option[SampleSend]],
    options: Option[String] = None
  )(implicit request: AuthenticatedRequest[_]): Html =
    change_sample_send(c, notFilledForm)

  override def chooseStatus(reference: String, options: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(requiredPermission)).async { implicit request =>
      getCaseAndRenderView(
        reference,
        c => successful(chooseStatusView(c, form))
      )
    }

  override protected def update(c: Case, sampleSender: Option[SampleSend], operator: Operator)(
    implicit hc: HeaderCarrier): Future[Case] =
    caseService.updateWhoSendSample(c, sampleSender, operator)

  override protected def onSuccessRedirect(reference: String): Call =
    controllers.routes.CaseController.sampleDetails(reference)
}
