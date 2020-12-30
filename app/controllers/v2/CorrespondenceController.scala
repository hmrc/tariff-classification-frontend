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
import models.forms._
import models.request._
import models.viewmodels.atar._
import models.viewmodels.correspondence.CaseDetailsViewModel
import models.viewmodels.{ActivityViewModel, CaseViewModel, KeywordsTabViewModel}
import models.{Case, EventType, NoPagination}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CorrespondenceController @Inject()(
  verify: RequestActions,
  eventsService: EventsService,
  queuesService: QueuesService,
  fileService: FileStoreService,
  countriesService: CountriesService,
  mcc: MessagesControllerComponents,
  val correspondenceView: views.html.v2.correspondence_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def displayCorrespondence(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async(implicit request => renderView())

  def renderView(
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    uploadForm: Form[String]             = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    val correspondenceCase: Case = request.`case`
    val correspondenceViewModel  = CaseViewModel.fromCase(correspondenceCase, request.operator)
    val countryNames   = countriesService.getAllCountriesById.mapValues(_.countryName)
    val caseDetailsTab = CaseDetailsViewModel.fromCase(correspondenceCase)
//    val goodsTab       = GoodsTabViewModel.fromCase(correspondenceCase)

    val attachmentsTabViewModel = getAttachmentTab(correspondenceCase)
    val activityTabViewModel    = getActivityTab(correspondenceCase)
    val storedAttachments       = fileService.getAttachments(correspondenceCase)

    for {
      attachmentsTab <- attachmentsTabViewModel
      activityTab    <- activityTabViewModel
      attachments    <- storedAttachments
    } yield Ok(
      correspondenceView(
        correspondenceViewModel,
        caseDetailsTab,
        attachmentsTab,
        uploadForm,
        activityTab,
        activityForm,
        attachments
      )
    )
  }

  private def getSampleTab(correspondenceCase: Case)(implicit request: AuthenticatedRequest[_]) =
    eventsService.getFilteredEvents(correspondenceCase.reference, NoPagination(), Some(EventType.sampleEvents)).map { events =>
      SampleTabViewModel.fromCase(correspondenceCase, events)
    }

  private def getAttachmentTab(correspondenceCase: Case)(implicit hc: HeaderCarrier): Future[AttachmentsTabViewModel] =
    fileService.getAttachments(correspondenceCase).map(attachments => AttachmentsTabViewModel.fromCase(correspondenceCase, attachments))

  private def getActivityTab(correspondenceCase: Case)(implicit request: AuthenticatedRequest[_]): Future[ActivityViewModel] =
    for {
      events <- eventsService.getFilteredEvents(correspondenceCase.reference, NoPagination(), Some(EventType.nonSampleEvents))
      queues <- queuesService.getAll
    } yield ActivityViewModel.fromCase(correspondenceCase, events, queues)

}
