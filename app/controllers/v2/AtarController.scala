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
import models.EventType.{allEvents, isSampleEvents}
import models._
import models.forms._
import models.request._
import models.viewmodels.atar._
import models.viewmodels.{AppealTabViewModel => _, AttachmentsTabViewModel => _, _}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import service._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.v2.atar_view

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AtarController @Inject() (
  verify: RequestActions,
  eventsService: EventsService,
  queuesService: QueuesService,
  fileService: FileStoreService,
  keywordsService: KeywordsService,
  countriesService: CountriesService,
  decisionForm: DecisionForm,
  mcc: MessagesControllerComponents,
  redirectService: RedirectService,
  val atarView: atar_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with UpscanErrorHandling
    with I18nSupport {

  def displayAtar(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.ATAR =>
          handleUploadErrorAndRender(uploadForm => renderView(fileId = fileId, uploadForm = uploadForm))
        case _ =>
          Future.successful(redirectService.redirectApplication(reference, fileId))
      }
    }

  def renderView(
    fileId: Option[String]               = None,
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    keywordForm: Form[String]            = KeywordForm.form,
    uploadForm: Form[String]             = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Html] = {

    val uploadFileId: String                       = fileId.getOrElse(UUID.randomUUID().toString)
    val atarCase: Case                             = request.`case`
    val atarViewModel: CaseViewModel               = CaseViewModel.fromCase(atarCase, request.operator)
    val countryNames: Map[String, Country]         = countriesService.getAllCountriesById
    val applicantTab: ApplicantTabViewModel        = ApplicantTabViewModel.fromCase(atarCase, countryNames)
    val goodsTab: GoodsTabViewModel                = GoodsTabViewModel.fromCase(atarCase)
    val rulingTab: RulingTabViewModel              = RulingTabViewModel.fromCase(atarCase)
    val rulingForm: Option[Form[DecisionFormData]] = decisionForm.bindFrom(rulingTab.decision)
    val appealTab: Option[AppealTabViewModel]      = AppealTabViewModel.fromCase(atarCase)

    val activeNavTab: PrimaryNavigationTab =
      PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(
        atarCase.status,
        atarCase.assignee.exists(_.id == request.operator.id)
      )

    val fileUploadSuccessRedirect: String =
      appConfig.host + controllers.routes.CaseController.addAttachment(atarCase.reference, uploadFileId).path

    val fileUploadErrorRedirect: String =
      appConfig.host + routes.AtarController
        .displayAtar(atarCase.reference, Some(uploadFileId))
        .withFragment(Tab.ATTACHMENTS_TAB.name)
        .path

    for {
      allEvents <- eventsService
                    .getFilteredEvents(
                      reference      = atarCase.reference,
                      pagination     = NoPagination(),
                      onlyEventTypes = Some(allEvents)
                    )
      initiateResponse <- fileService.initiate(
                           FileStoreInitiateRequest(
                             id              = Some(uploadFileId),
                             successRedirect = Some(fileUploadSuccessRedirect),
                             errorRedirect   = Some(fileUploadErrorRedirect),
                             maxFileSize     = appConfig.fileUploadMaxSize
                           )
                         )
      queues         <- queuesService.getAll
      attachments    <- fileService.getAttachments(atarCase)
      globalKeywords <- keywordsService.findAll()
      sampleTab = SampleTabViewModel.fromCase(atarCase, allEvents.filter(event => isSampleEvents(event.details.`type`)))
      activityTab = ActivityViewModel
        .fromCase(atarCase, allEvents.filterNot(event => isSampleEvents(event.details.`type`)), queues)
      attachmentsTab = AttachmentsTabViewModel.fromCase(atarCase, attachments)
      keywordsTab    = KeywordsTabViewModel.fromCase(atarCase, globalKeywords.map(_.name))
    } yield {
      atarView(
        caseViewModel    = atarViewModel,
        applicantTab     = applicantTab,
        goodsTab         = goodsTab,
        sampleTab        = sampleTab,
        attachmentsTab   = attachmentsTab,
        uploadForm       = uploadForm,
        initiateResponse = initiateResponse,
        activityTab      = activityTab,
        activityForm     = activityForm,
        keywordsTab      = keywordsTab,
        keywordForm      = keywordForm,
        rulingTab        = rulingTab,
        rulingForm       = rulingForm,
        attachments      = attachments,
        appealTab        = appealTab,
        primaryNavTab    = activeNavTab
      )
    }
  }
}
