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
import uk.gov.hmrc.http.HeaderCarrier
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
  val atarView: atar_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with UpscanErrorHandling
    with I18nSupport {

  def displayAtar(reference: String, fileId: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      handleUploadErrorAndRender(uploadForm => renderView(fileId = fileId, uploadForm = uploadForm))
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

    val sampleTabViewModel: Future[SampleTabViewModel]           = getSampleTab(atarCase)
    val attachmentsTabViewModel: Future[AttachmentsTabViewModel] = getAttachmentTab(atarCase)
    val activityTabViewModel: Future[ActivityViewModel]          = getActivityTab(atarCase)
    val keywordsTabViewModel: Future[KeywordsTabViewModel]       = getKeywordsTab(atarCase)
    val storedAttachments: Future[Seq[StoredAttachment]]         = fileService.getAttachments(atarCase)
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
      sampleTab      <- sampleTabViewModel
      attachmentsTab <- attachmentsTabViewModel
      activityTab    <- activityTabViewModel
      keywordsTab    <- keywordsTabViewModel
      attachments    <- storedAttachments
      initiateResponse <- fileService.initiate(
                           FileStoreInitiateRequest(
                             id              = Some(uploadFileId),
                             successRedirect = Some(fileUploadSuccessRedirect),
                             errorRedirect   = Some(fileUploadErrorRedirect),
                             maxFileSize     = appConfig.fileUploadMaxSize
                           )
                         )
    } yield {
      atarView(
        atarViewModel,
        applicantTab,
        goodsTab,
        sampleTab,
        attachmentsTab,
        uploadForm,
        initiateResponse,
        activityTab,
        activityForm,
        keywordsTab,
        keywordForm,
        rulingTab,
        rulingForm,
        attachments,
        appealTab,
        activeNavTab
      )
    }
  }

  private def getSampleTab(atarCase: Case)(implicit request: AuthenticatedRequest[_]) =
    eventsService.getFilteredEvents(atarCase.reference, NoPagination(), Some(EventType.sampleEvents)).map { events =>
      SampleTabViewModel.fromCase(atarCase, events)
    }

  private def getAttachmentTab(atarCase: Case)(implicit hc: HeaderCarrier): Future[AttachmentsTabViewModel] =
    fileService.getAttachments(atarCase).map(attachments => AttachmentsTabViewModel.fromCase(atarCase, attachments))

  private def getActivityTab(atarCase: Case)(implicit request: AuthenticatedRequest[_]): Future[ActivityViewModel] =
    for {
      events <- eventsService.getFilteredEvents(atarCase.reference, NoPagination(), Some(EventType.nonSampleEvents))
      queues <- queuesService.getAll
    } yield ActivityViewModel.fromCase(atarCase, events, queues)

  private def getKeywordsTab(atarCase: Case): Future[KeywordsTabViewModel] =
    keywordsService.findAll.map { globalKeywords =>
      KeywordsTabViewModel.fromCase(atarCase, globalKeywords.map(_.name))
    }
}
