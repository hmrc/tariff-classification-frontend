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
import models.forms.SampleStatusForm
import javax.inject.{Inject, Singleton}
import models.SampleStatus.SampleStatus
import models._
import play.api.data.Form
import play.api.mvc._
import play.twirl.api.Html
import service.{CasesService, EventsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful
import controllers.Tab._

@Singleton
class SampleController @Inject() (
  override val verify: RequestActions,
  override val caseService: CasesService,
  eventsService: EventsService,
  mcc: MessagesControllerComponents,
  val caseDetailsView: views.html.case_details,
  override implicit val config: AppConfig
) extends FrontendController(mcc)
    with StatusChangeAction[Option[SampleStatus]] {

  override protected val requiredPermission: Permission = Permission.EDIT_SAMPLE

  override protected val form: Form[Option[SampleStatus]] = SampleStatusForm.form

  override protected def status(c: Case): Option[SampleStatus] = c.sample.status

  protected def chooseStatusView(c: Case, notFilledForm: Form[Option[SampleStatus]], options: Option[String])(
    implicit request: Request[_]
  ): Html =
    if (options.contains("liability"))
      views.html.change_liablity_sending_sample(c, notFilledForm)
    else
      views.html.change_sample_status(c, notFilledForm)

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
    controllers.v2.routes.LiabilityController.displayLiability(reference).withFragment(SAMPLE_TAB)

  def sampleDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      getCaseAndRenderView(
        reference,
        c =>
          for {
            events <- eventsService.getFilteredEvents(c.reference, NoPagination(), Some(EventType.sampleEvents))
          } yield caseDetailsView(c, CaseDetailPage.SAMPLE_DETAILS, views.html.partials.sample.sample_details(c, events))
      )
    }
}
