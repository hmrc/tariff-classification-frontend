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
import controllers.{RequestActions, Tab}
import models.forms._
import models.request._
import models.viewmodels.atar._
import models.viewmodels.miscellaneous.DetailsViewModel
import models.viewmodels.{AttachmentsTabViewModel => _, _}
import models.{Case, EventType, NoPagination}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import service._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.v2.miscellaneous_view

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MiscellaneousController @Inject() (
  verify: RequestActions,
  eventsService: EventsService,
  queuesService: QueuesService,
  fileService: FileStoreService,
  mcc: MessagesControllerComponents,
  val miscellaneousView: miscellaneous_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with UpscanErrorHandling
    with I18nSupport {

  def displayMiscellaneous(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      handleUploadErrorAndRender(uploadForm => renderView(fileId = fileId, uploadForm = uploadForm))
    }

  def renderView(
    fileId: Option[String]               = None,
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    messageForm: Form[MessageFormData]   = MessageForm.form,
    uploadForm: Form[String]             = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Html] = {
    val miscellaneousCase: Case = request.`case`
    val uploadFileId            = fileId.getOrElse(UUID.randomUUID().toString)

    val miscellaneousViewModel          = CaseViewModel.fromCase(miscellaneousCase, request.operator)
    val caseDetailsTab                  = DetailsViewModel.fromCase(miscellaneousCase)
    val messagesTab                     = MessagesTabViewModel.fromCase(miscellaneousCase)
    val attachmentsTabViewModel         = getAttachmentTab(miscellaneousCase)
    val activityTabViewModel            = getActivityTab(miscellaneousCase)
    val storedAttachments               = fileService.getAttachments(miscellaneousCase)
    val miscellaneousSampleTabViewModel = getSampleTab(miscellaneousCase)
    val activeNavTab = PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(
      miscellaneousCase.status,
      miscellaneousCase.assignee.exists(_.id == request.operator.id)
    )

    val fileUploadSuccessRedirect =
      appConfig.host + controllers.routes.CaseController.addAttachment(miscellaneousCase.reference, uploadFileId).path

    val fileUploadErrorRedirect =
      appConfig.host + routes.MiscellaneousController
        .displayMiscellaneous(miscellaneousCase.reference, Some(uploadFileId))
        .withFragment(Tab.ATTACHMENTS_TAB.name)
        .path

    for {
      attachmentsTab <- attachmentsTabViewModel
      activityTab    <- activityTabViewModel
      attachments    <- storedAttachments
      sampleTab      <- miscellaneousSampleTabViewModel
      initiateResponse <- fileService.initiate(
                           FileStoreInitiateRequest(
                             id              = Some(uploadFileId),
                             successRedirect = Some(fileUploadSuccessRedirect),
                             errorRedirect   = Some(fileUploadErrorRedirect),
                             maxFileSize     = appConfig.fileUploadMaxSize
                           )
                         )
    } yield miscellaneousView(
      miscellaneousViewModel,
      caseDetailsTab,
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

  private def getSampleTab(miscellaneousCase: Case)(implicit request: AuthenticatedRequest[_]) =
    eventsService.getFilteredEvents(miscellaneousCase.reference, NoPagination(), Some(EventType.sampleEvents)).map {
      sampleEvents => SampleStatusTabViewModel(miscellaneousCase.reference, miscellaneousCase.sample, sampleEvents)
    }

  private def getAttachmentTab(miscellaneousCase: Case)(implicit hc: HeaderCarrier): Future[AttachmentsTabViewModel] =
    fileService
      .getAttachments(miscellaneousCase)
      .map(attachments => AttachmentsTabViewModel.fromCase(miscellaneousCase, attachments))

  private def getActivityTab(
    miscellaneousCase: Case
  )(implicit request: AuthenticatedRequest[_]): Future[ActivityViewModel] =
    for {
      events <- eventsService
                 .getFilteredEvents(miscellaneousCase.reference, NoPagination(), Some(EventType.nonSampleEvents))
      queues <- queuesService.getAll
    } yield ActivityViewModel.fromCase(miscellaneousCase, events, queues)

}
