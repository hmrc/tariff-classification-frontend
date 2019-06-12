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
import play.api.mvc.{Action, AnyContent, Call, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.LiabilitySampleForm
import uk.gov.hmrc.tariffclassificationfrontend.models.SampleSending.SampleSending
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{SampleSending, _}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class LiabilitySampleController @Inject()(override val verify: RequestActions,
                                          override val caseService: CasesService,
                                          override val messagesApi: MessagesApi,
                                          override implicit val config: AppConfig) extends StatusChangeAction[Option[SampleSending]] {

  override protected val requiredPermission: Permission.Value = Permission.EDIT_SAMPLE

  override protected def redirect: String => Call = routes.CaseController.trader

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    c.application.isLiabilityOrder
  }

  override protected val form: Form[Option[SampleSending]] = LiabilitySampleForm.form

  override protected def status(c: Case): Option[SampleSending] = c.sample.status match {
    case Some(_) => Some(SampleSending.YES)
    case _ => Some(SampleSending.NO)
  }

  override protected def chooseStatusView(c: Case, notFilledForm: Form[Option[SampleSending]])
                                         (implicit request: Request[_]): Html = {
    views.html.change_liablity_sending_sample(c, notFilledForm)
  }

  override def chooseStatus(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(requiredPermission)).async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(chooseStatusView(c, form))
    )
  }

  override protected def update(c: Case, sendingSample: Option[SampleSending], operator: Operator)
                               (implicit hc: HeaderCarrier): Future[Case] = {
    caseService.updateLiabilitySample(c, sendingSample, operator)
  }

  override protected def onSuccessRedirect(reference: String): Call = routes.CaseController.sampleDetails(reference)

}
