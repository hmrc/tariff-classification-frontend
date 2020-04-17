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
import models.forms.SampleReturnForm
import javax.inject.{Inject, Singleton}
import models.SampleReturn.SampleReturn
import models._
import play.api.data.Form
import play.api.mvc._
import play.twirl.api.Html
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import controllers.Tab._

import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SampleReturnController @Inject()(
  override val verify: RequestActions,
  override val caseService: CasesService,
  mcc: MessagesControllerComponents,
  override implicit val config: AppConfig
) extends FrontendController(mcc) with StatusChangeAction[Option[SampleReturn]] {

  override protected val requiredPermission: Permission = Permission.EDIT_SAMPLE

  override protected val form: Form[Option[SampleReturn]] = SampleReturnForm.form

  override protected def status(c: Case): Option[SampleReturn] = c.sample.returnStatus

  override protected def chooseStatusView(c: Case, notFilledForm: Form[Option[SampleReturn]], options: Option[String] = None)
                                         (implicit request: Request[_]): Html = {
    views.html.change_sample_return(c, notFilledForm)
  }

  override def chooseStatus(reference: String, options: Option[String] = None): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(requiredPermission)).async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(chooseStatusView(c, form))
    )
  }

  override protected def update(c: Case, status: Option[SampleReturn], operator: Operator)
                               (implicit hc: HeaderCarrier): Future[Case] = {
    caseService.updateSampleReturn(c, status, operator)
  }

  override protected def onSuccessRedirect(reference: String, isV2Liability: Boolean): Call = {
    if(isV2Liability) controllers.v2.routes.LiabilityController.displayLiability(reference).withFragment(SAMPLE_TAB)
    else routes.CaseController.sampleDetails(reference)
  }

}
