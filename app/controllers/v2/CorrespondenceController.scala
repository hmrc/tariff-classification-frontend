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

package controllers.v2

import java.util.UUID
import javax.inject.{Inject, Singleton}

import config.AppConfig
import controllers.{RequestActions, Tab}
import models.forms._
import models.request._
import models.response.FileStoreInitiateResponse
import models.viewmodels.atar._
import models.viewmodels.correspondence.{CaseDetailsViewModel, ContactDetailsTabViewModel}
import models.viewmodels.{ActivityViewModel, CaseViewModel, GatewayCasesTab, MessagesTabViewModel, MyCasesTab, OpenCasesTab, PrimaryNavigationViewModel, SampleStatusTabViewModel}
import models.{Case, CaseStatus, EventType, NoPagination}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CorrespondenceController @Inject() (
  verify: RequestActions,
  eventsService: EventsService,
  queuesService: QueuesService,
  fileService: FileStoreService,
  mcc: MessagesControllerComponents,
  val correspondenceView: views.html.v2.correspondence_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with UpscanErrorHandling
    with I18nSupport {

  def displayCorrespondence(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      val uploadFileId = fileId.getOrElse(UUID.randomUUID().toString)
      handleUploadErrorAndRender(uploadForm => renderView(fileId = uploadFileId, uploadForm = uploadForm))
    }

  def renderView(
    fileId: String = UUID.randomUUID().toString,
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    messageForm: Form[MessageFormData]   = MessageForm.form,
    uploadForm: Form[String]             = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    val correspondenceCase: Case         = request.`case`
    val correspondenceViewModel          = CaseViewModel.fromCase(correspondenceCase, request.operator)
    val caseDetailsTab                   = CaseDetailsViewModel.fromCase(correspondenceCase)
    val contactDetailsTab                = ContactDetailsTabViewModel.fromCase(correspondenceCase)
    val messagesTab                      = MessagesTabViewModel.fromCase(correspondenceCase)
    val attachmentsTabViewModel          = getAttachmentTab(correspondenceCase)
    val activityTabViewModel             = getActivityTab(correspondenceCase)
    val storedAttachments                = fileService.getAttachments(correspondenceCase)
    val correspondenceSampleTabViewModel = getSampleTab(correspondenceCase)
    val activeNavTab = PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(
      correspondenceCase.status,
      correspondenceCase.assignee.exists(_.id == request.operator.id)
    )

    val fileUploadSuccessRedirect =
      appConfig.host + routes.AttachmentsController.handleUploadSuccess(correspondenceCase.reference, fileId).path
    val fileUploadErrorRedirect =
      appConfig.host + routes.CorrespondenceController.displayCorrespondence(correspondenceCase.reference, Some(fileId)).withFragment(Tab.ATTACHMENTS_TAB.name).path

    for {
      attachmentsTab <- attachmentsTabViewModel
      activityTab    <- activityTabViewModel
      attachments    <- storedAttachments
      sampleTab      <- correspondenceSampleTabViewModel
      initiateResponse <- fileService.initiate(
                           FileStoreInitiateRequest(
                             id              = Some(fileId),
                             successRedirect = Some(fileUploadSuccessRedirect),
                             errorRedirect   = Some(fileUploadErrorRedirect),
                             maxFileSize     = appConfig.fileUploadMaxSize
                           )
                         )
    } yield Ok(
      correspondenceView(
        correspondenceViewModel,
        caseDetailsTab,
        contactDetailsTab,
        messagesTab,
        messageForm,
        sampleTab,
        attachmentsTab,
        uploadForm,
        initiateResponse,
        activityTab,
        activityForm,
        attachments,
        activeNavTab
      )
    )
  }

  private def getSampleTab(correspondenceCase: Case)(implicit request: AuthenticatedRequest[_]) =
    eventsService.getFilteredEvents(correspondenceCase.reference, NoPagination(), Some(EventType.sampleEvents)).map {
      sampleEvents => SampleStatusTabViewModel(correspondenceCase.reference, correspondenceCase.sample, sampleEvents)
    }

  private def getAttachmentTab(correspondenceCase: Case)(implicit hc: HeaderCarrier): Future[AttachmentsTabViewModel] =
    fileService
      .getAttachments(correspondenceCase)
      .map(attachments => AttachmentsTabViewModel.fromCase(correspondenceCase, attachments))

  private def getActivityTab(
    correspondenceCase: Case
  )(implicit request: AuthenticatedRequest[_]): Future[ActivityViewModel] =
    for {
      events <- eventsService
                 .getFilteredEvents(correspondenceCase.reference, NoPagination(), Some(EventType.nonSampleEvents))
      queues <- queuesService.getAll
    } yield ActivityViewModel.fromCase(correspondenceCase, events, queues)

}
