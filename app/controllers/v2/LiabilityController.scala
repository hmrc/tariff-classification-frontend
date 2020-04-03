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
import controllers.{RequestActions, v2}
import javax.inject.{Inject, Singleton}
import models.TabIndexes.tabIndexFor
import models.forms.{ActivityForm, ActivityFormData}
import models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import models.viewmodels.{ActivityViewModel, AttachmentsTabViewModel, C592ViewModel, LiabilityViewModel}
import models.{Case, Permission, _}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, EventsService, FileStoreService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.CaseDetailPage.ACTIVITY

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class LiabilityController @Inject()(
                                     verify: RequestActions,
                                     casesService: CasesService,
                                     eventsService: EventsService,
                                     queuesService: QueuesService,
                                     fileService: FileStoreService,
                                     mcc: MessagesControllerComponents,
                                     val liability_view: views.html.v2.liability_view,
                                     implicit val appConfig: AppConfig
                                   ) extends FrontendController(mcc) with I18nSupport {

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  def displayLiability(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async {
    implicit request => {
      buildLiabilityView(activityForm)
    }
  }

  private def getAttachmentTab(liabilityCase: Case)(implicit hc: HeaderCarrier): Future[Option[AttachmentsTabViewModel]] = {
    for {
      attachments <- fileService.getAttachments(liabilityCase)
      letter <- fileService.getLetterOfAuthority(liabilityCase)
    } yield {
      val (applicantFiles, nonApplicantFiles) = attachments.partition(_.operator.isEmpty)
      Some(AttachmentsTabViewModel(liabilityCase.reference, applicantFiles, letter, nonApplicantFiles))
    }
  }

  def addNote(reference: String): Action[AnyContent] = (verify.authenticated andThen
    verify.casePermissions(reference) andThen verify.mustHave(Permission.ADD_NOTE)).async { implicit request =>
    def onError: Form[ActivityFormData] => Future[Result] = errorForm => {
      buildLiabilityView(errorForm)
    }

    def onSuccess: ActivityFormData => Future[Result] = validForm => {
      eventsService.addNote(request.`case`, validForm.note, request.operator)
        .map(_ => Redirect(v2.routes.LiabilityController.displayLiability(reference)))
    }

    activityForm.bindFromRequest.fold(onError, onSuccess)
  }

  def buildLiabilityView(form: Form[_])(implicit request: AuthenticatedCaseRequest[AnyContent]): Future[Result] = {
    val liabilityCase: Case = request.`case`
    val liabilityViewModel = LiabilityViewModel.fromCase(liabilityCase, request.operator)

    for {
      tuple <- liabilityViewActivityDetails(liabilityCase.reference)
      attachmentsTab <- getAttachmentTab(liabilityCase)
      c592 = Some(C592ViewModel.fromCase(liabilityCase))
      activityTab = ActivityViewModel.fromCase(liabilityCase, tuple._1, tuple._2)
    } yield {
      Ok(liability_view(liabilityViewModel, c592, attachmentsTab, activityTab, activityForm))
    }
  }

  def liabilityViewActivityDetails(reference: String)(implicit request: AuthenticatedRequest[AnyContent]) = {
    for {
      events <- eventsService.getFilteredEvents(reference, NoPagination(), Some(EventType.values.diff(EventType.sampleEvents)))
      queues <- queuesService.getAll
    } yield (events, queues, tabIndexFor(ACTIVITY))
  }

}
