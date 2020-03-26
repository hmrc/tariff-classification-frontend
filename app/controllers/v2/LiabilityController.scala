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

package controllers.v2


import config.AppConfig
import controllers.RequestActions
import javax.inject.{Inject, Singleton}
import models.TabIndexes.tabIndexFor
import models._
import models.forms.{ActivityForm, ActivityFormData}
import models.request.AuthenticatedRequest
import models.viewmodels.LiabilityViewModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, EventsService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.CaseDetailPage.ACTIVITY

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import config.AppConfig
import models.forms._
import javax.inject.{Inject, Singleton}
import models.TabIndexes.tabIndexFor
import models._
import models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.{Html, HtmlFormat}
import service._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.CaseDetailPage._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import config.AppConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
@Singleton
class LiabilityController @Inject()(
                                     verify: RequestActions,
                                     casesService: CasesService,
                                     eventsService: EventsService,
                                     queuesService: QueuesService,
                                     mcc: MessagesControllerComponents,
                                     val liability_view: views.html.v2.liability_view,
                                     implicit val appConfig: AppConfig
                                   ) extends FrontendController(mcc) with I18nSupport {

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  def displayLiability(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async {
    implicit request =>

      liabilityViewActivityDetails(reference).flatMap(
        tuple => Future.successful(Ok(liability_view(
          LiabilityViewModel.fromCase(request.`case`, request.operator),
          tuple._1,
          activityForm,
          tuple._2,
          tuple._3))
        )
      )
  }

  def liabilityViewActivityDetails(reference: String)(implicit request: AuthenticatedRequest[AnyContent]) = {
    for {
      events <- eventsService.getFilteredEvents(reference, NoPagination(), Some(EventType.values.diff(EventType.sampleEvents)))
      queues <- queuesService.getAll
    } yield (events, queues, tabIndexFor(ACTIVITY))
  }
}
