/*
 * Copyright 2024 HM Revenue & Customs
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
import models.EventType.isSampleEvents
import models.forms._
import models.request._
import models.viewmodels.atar._
import models.viewmodels.miscellaneous.DetailsViewModel
import models.viewmodels.{AttachmentsTabViewModel => _, _}
import models.{ApplicationType, Case, EventType, NoPagination}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import service._
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
  redirectService: RedirectService,
  val miscellaneousView: miscellaneous_view,
  implicit val appConfig: AppConfig
)(using ec: ExecutionContext)
    extends FrontendController(mcc)
    with UpscanErrorHandling
    with I18nSupport {

  def displayMiscellaneous(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.MISCELLANEOUS =>
          handleUploadErrorAndRender(uploadForm => renderView(fileId = fileId, uploadForm = uploadForm))
        case _ =>
          Future.successful(redirectService.redirectApplication(reference, fileId))
      }
    }

  def renderView(
    fileId: Option[String] = None,
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    messageForm: Form[MessageFormData] = MessageForm.form,
    uploadForm: Form[String] = UploadAttachmentForm.form
  )(using request: AuthenticatedCaseRequest[_]): Future[Html] = {

    val miscellaneousCase: Case = request.`case`
    val uploadFileId            = fileId.getOrElse(UUID.randomUUID().toString)

    val miscellaneousViewModel = CaseViewModel.fromCase(miscellaneousCase, request.operator)
    val caseDetailsTab         = DetailsViewModel.fromCase(miscellaneousCase)
    val messagesTab            = MessagesTabViewModel.fromCase(miscellaneousCase)
    val activeNavTab =
      PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(
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
      allEvents <- eventsService
                     .getFilteredEvents(miscellaneousCase.reference, NoPagination(), Some(EventType.allEvents))
      queues <- queuesService.getAll
      activityTab =
        ActivityViewModel
          .fromCase(miscellaneousCase, allEvents.filterNot(event => isSampleEvents(event.details.`type`)), queues)
      sampleTab = SampleStatusTabViewModel(
                    miscellaneousCase.reference,
                    miscellaneousCase.sample,
                    allEvents.filter(event => isSampleEvents(event.details.`type`))
                  )
      attachments <- fileService.getAttachments(miscellaneousCase)
      attachmentsTab = AttachmentsTabViewModel.fromCase(miscellaneousCase, attachments)
      initiateResponse <- fileService.initiate(
                            FileStoreInitiateRequest(
                              id = Some(uploadFileId),
                              successRedirect = Some(fileUploadSuccessRedirect),
                              errorRedirect = Some(fileUploadErrorRedirect),
                              maxFileSize = appConfig.fileUploadMaxSize
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
}
