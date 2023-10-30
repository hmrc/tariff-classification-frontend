/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.{RedirectController, RequestActions, Tab}
import models.EventType._
import models.forms._
import models.request._
import models.viewmodels.atar._
import models.viewmodels.correspondence.{CaseDetailsViewModel, ContactDetailsTabViewModel}
import models.viewmodels.{AttachmentsTabViewModel => _, _}
import models.{ApplicationType, Case, EventType, NoPagination}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import service._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.v2.correspondence_view

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class CorrespondenceController @Inject() (
  verify: RequestActions,
  eventsService: EventsService,
  queuesService: QueuesService,
  fileService: FileStoreService,
  mcc: MessagesControllerComponents,
  redirectController: RedirectController,
  val correspondenceView: correspondence_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with UpscanErrorHandling
    with I18nSupport {

  def displayCorrespondence(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.CORRESPONDENCE =>
          handleUploadErrorAndRender(uploadForm => renderView(fileId = fileId, uploadForm = uploadForm))
        case _ =>
          Future.successful(redirectController.redirectApplication(reference, fileId))
      }
    }

  def renderView(
    fileId: Option[String]               = None,
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    messageForm: Form[MessageFormData]   = MessageForm.form,
    uploadForm: Form[String]             = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Html] = {
    val correspondenceCase: Case = request.`case`
    val uploadFileId             = fileId.getOrElse(UUID.randomUUID().toString)

    val correspondenceViewModel = CaseViewModel.fromCase(correspondenceCase, request.operator)
    val caseDetailsTab          = CaseDetailsViewModel.fromCase(correspondenceCase)
    val contactDetailsTab       = ContactDetailsTabViewModel.fromCase(correspondenceCase)
    val messagesTab             = MessagesTabViewModel.fromCase(correspondenceCase)
    val activeNavTab =
      PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(
        correspondenceCase.status,
        correspondenceCase.assignee.exists(_.id == request.operator.id)
      )

    val fileUploadSuccessRedirect =
      appConfig.host + controllers.routes.CaseController.addAttachment(correspondenceCase.reference, uploadFileId).path

    val fileUploadErrorRedirect =
      appConfig.host + routes.CorrespondenceController
        .displayCorrespondence(correspondenceCase.reference, Some(uploadFileId))
        .withFragment(Tab.ATTACHMENTS_TAB.name)
        .path

    for {
      allEvents <- eventsService
                    .getFilteredEvents(correspondenceCase.reference, NoPagination(), Some(EventType.allEvents))
      queues      <- queuesService.getAll
      attachments <- fileService.getAttachments(correspondenceCase)
      activityTab = ActivityViewModel
        .fromCase(correspondenceCase, allEvents.filterNot(event => isSampleEvents(event.details.`type`)), queues)
      attachmentsTab = AttachmentsTabViewModel.fromCase(correspondenceCase, attachments)
      sampleTab = SampleStatusTabViewModel(
        correspondenceCase.reference,
        correspondenceCase.sample,
        allEvents.filter(event => isSampleEvents(event.details.`type`))
      )
      initiateResponse <- fileService.initiate(
                           FileStoreInitiateRequest(
                             id              = Some(uploadFileId),
                             successRedirect = Some(fileUploadSuccessRedirect),
                             errorRedirect   = Some(fileUploadErrorRedirect),
                             maxFileSize     = appConfig.fileUploadMaxSize
                           )
                         )
    } yield correspondenceView(
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
  }
}
