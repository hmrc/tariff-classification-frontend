/*
 * Copyright 2025 HM Revenue & Customs
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
import controllers.{RequestActions, Tab, v2}
import models.EventType._
import models._
import models.forms._
import models.forms.v2.LiabilityDetailsForm
import models.request._
import models.viewmodels._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import services._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.v2.{liability_details_edit, liability_view}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LiabilityController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  eventsService: EventsService,
  queuesService: QueuesService,
  fileService: FileStoreService,
  keywordsService: KeywordsService,
  mcc: MessagesControllerComponents,
  liabilityDetailsForm: LiabilityDetailsForm,
  redirectService: RedirectService,
  val liability_view: liability_view,
  val liability_details_edit: liability_details_edit,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with UpscanErrorHandling
    with I18nSupport {

  def displayLiability(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.LIABILITY =>
          handleUploadErrorAndRender(uploadForm => renderView(fileId = fileId, uploadForm = uploadForm))
        case _ =>
          Future.successful(redirectService.redirectApplication(reference, fileId))
      }
    }

  def renderView(
    fileId: Option[String] = None,
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    uploadForm: Form[String] = UploadAttachmentForm.form,
    keywordForm: Form[String] = KeywordForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Html] = {

    val liabilityCase: Case = request.`case`
    val uploadFileId        = fileId.getOrElse(UUID.randomUUID().toString)

    val liabilityViewModel = CaseViewModel.fromCase(liabilityCase, request.operator)
    val rulingViewModel    = Some(RulingViewModel.fromCase(liabilityCase, request.operator.permissions))
    val appealTabViewModel = Some(AppealTabViewModel.fromCase(liabilityCase, request.operator))
    val ownCase            = liabilityCase.assignee.exists(_.id == request.operator.id)
    val activeNavTab = PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(liabilityCase.status, ownCase)

    val fileUploadSuccessRedirect =
      appConfig.host + controllers.routes.CaseController.addAttachment(liabilityCase.reference, uploadFileId).path

    val fileUploadErrorRedirect =
      appConfig.host + routes.LiabilityController
        .displayLiability(liabilityCase.reference, Some(uploadFileId))
        .withFragment(Tab.ATTACHMENTS_TAB.name)
        .path

    for {
      allEvents <- eventsService
                     .getFilteredEvents(
                       liabilityCase.reference,
                       NoPagination(),
                       Some(EventType.values.diff(EventType.sampleEvents))
                     )
      queues         <- queuesService.getAll
      attachmentsTab <- getAttachmentTab(liabilityCase)
      sampleTab = SampleStatusTabViewModel(
                    liabilityCase.reference,
                    liabilityCase.sample,
                    allEvents.filter(event => isSampleEvents(event.details.`type`))
                  )
      activityTab =
        Some(
          ActivityViewModel
            .fromCase(liabilityCase, allEvents.filterNot(event => isSampleEvents(event.details.`type`)), queues)
        )
      c592 = Some(C592ViewModel.fromCase(liabilityCase))
      keywordsTab <-
        keywordsService
          .findAll()
          .map(kws => KeywordsTabViewModel(liabilityCase.reference, liabilityCase.keywords, kws.map(_.name)))
      initiateResponse <- fileService.initiate(
                            FileStoreInitiateRequest(
                              id = Some(uploadFileId),
                              successRedirect = Some(fileUploadSuccessRedirect),
                              errorRedirect = Some(fileUploadErrorRedirect),
                              maxFileSize = appConfig.fileUploadMaxSize
                            )
                          )
    } yield liability_view(
      liabilityViewModel,
      c592,
      rulingViewModel,
      sampleTab,
      activityTab,
      activityForm,
      attachmentsTab,
      uploadForm,
      initiateResponse,
      keywordsTab,
      keywordForm,
      appealTabViewModel,
      activeNavTab
    )
  }

  private def getAttachmentTab(
    liabilityCase: Case
  )(implicit hc: HeaderCarrier): Future[Option[AttachmentsTabViewModel]] =
    for {
      attachments <- fileService.getAttachments(liabilityCase)
      letter      <- fileService.getLetterOfAuthority(liabilityCase)
    } yield Some(AttachmentsTabViewModel(liabilityCase.reference, attachments, letter))

  def editLiabilityDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.EDIT_LIABILITY)).async { implicit request =>
      val activeNavTab = PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(
        request.`case`.status,
        request.`case`.assignee.exists(_.id == request.operator.id)
      )
      Future(
        Ok(
          liability_details_edit(
            request.`case`,
            liabilityDetailsForm.liabilityDetailsForm(request.`case`),
            activeNavTab
          )
        )
      )
    }

  def postLiabilityDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.EDIT_LIABILITY)).async { implicit request =>
      liabilityDetailsForm
        .liabilityDetailsForm(request.`case`)
        .discardingErrors
        .bindFromRequest()
        .fold(
          errorForm => Future(Ok(liability_details_edit(request.`case`, errorForm))),
          updatedCase =>
            casesService
              .updateCase(request.`case`, updatedCase, request.operator)
              .map(_ => Redirect(v2.routes.LiabilityController.displayLiability(reference)))
        )
    }

}
