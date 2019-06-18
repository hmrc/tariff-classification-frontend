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
import uk.gov.hmrc.tariffclassificationfrontend.forms.SampleStatusForm
import uk.gov.hmrc.tariffclassificationfrontend.models.SampleStatus.SampleStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, EventsService}
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SampleController @Inject()(override val verify: RequestActions,
                                  override val caseService: CasesService,
                                  eventsService: EventsService,
                                  override val messagesApi: MessagesApi,
                                  override implicit val config: AppConfig) extends StatusChangeAction[Option[SampleStatus]] {

  override protected val requiredPermission: Permission = Permission.EDIT_SAMPLE

  override protected def redirect: String => Call = routes.CaseController.get

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    true //No constraints on when the case is valid
  }

  override protected val form: Form[Option[SampleStatus]] = SampleStatusForm.form

  override protected def status(c: Case): Option[SampleStatus] = c.sample.status

  protected def chooseStatusView(c: Case, notFilledForm: Form[Option[SampleStatus]], options: Option[String])
                                         (implicit request: Request[_]): Html = {
    if(options.contains("liability"))
      views.html.change_liablity_sending_sample(c, notFilledForm)
    else
      views.html.change_sample_status(c, notFilledForm)
  }

  override def chooseStatus(reference: String, options: Option[String]): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen
    verify.mustHave(requiredPermission)).async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(chooseStatusView(c, form, options))
    )
  }

  override protected def update(c: Case, status: Option[SampleStatus], operator: Operator)
                               (implicit hc: HeaderCarrier): Future[Case] = {
    caseService.updateSampleStatus(c, status, operator)
  }

  override protected def onSuccessRedirect(reference: String): Call = routes.CaseController.sampleDetails(reference)

  def sampleDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => {
        for {
          events <- eventsService.getFilteredEvents(c.reference, NoPagination(),Some(EventType.sampleEvents))
        } yield views.html.case_details(c, CaseDetailPage.SAMPLE_DETAILS, views.html.partials.sample.sample_details(c, events))
      }
    )
  }
}
