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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Call, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.SampleReturnForm
import uk.gov.hmrc.tariffclassificationfrontend.models.SampleReturn.SampleReturn
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SampleReturnController @Inject()(override val verify: RequestActions,
                                       override val caseService: CasesService,
                                       override val messagesApi: MessagesApi,
                                       override implicit val config: AppConfig) extends StatusChangeAction[Option[SampleReturn]] {

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

  override protected def onSuccessRedirect(reference: String): Call = routes.CaseController.sampleDetails(reference)

}
