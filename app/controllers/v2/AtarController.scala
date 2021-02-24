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

import config.AppConfig
import controllers.RequestActions
import javax.inject.{Inject, Singleton}
import models.{Case, CaseStatus, EventType, NoPagination}
import models.forms.{ActivityForm, ActivityFormData, DecisionForm, KeywordForm, UploadAttachmentForm}
import models.viewmodels.{ActivityViewModel, CaseViewModel, GatewayCasesTab, KeywordsTabViewModel, MyCasesTab, OpenCasesTab, PrimaryNavigationViewModel}
import models.viewmodels.atar._
import models.request._
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import service.{CountriesService, EventsService, FileStoreService, KeywordsService, QueuesService}

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
  val atarView: views.html.v2.atar_view,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def displayAtar(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async(implicit request => renderView())

  def renderView(
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    keywordForm: Form[String]            = KeywordForm.form,
    uploadForm: Form[String]             = UploadAttachmentForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    val atarCase: Case = request.`case`
    val atarViewModel  = CaseViewModel.fromCase(atarCase, request.operator)
    val countryNames   = countriesService.getAllCountriesById.mapValues(_.countryName)
    val applicantTab   = ApplicantTabViewModel.fromCase(atarCase, countryNames)
    val goodsTab       = GoodsTabViewModel.fromCase(atarCase)
    val rulingTab      = RulingTabViewModel.fromCase(atarCase)
    val rulingForm     = decisionForm.bindFrom(rulingTab.decision)
    val appealTab      = AppealTabViewModel.fromCase(atarCase)

    val sampleTabViewModel      = getSampleTab(atarCase)
    val attachmentsTabViewModel = getAttachmentTab(atarCase)
    val activityTabViewModel    = getActivityTab(atarCase)
    val keywordsTabViewModel    = getKeywordsTab(atarCase)
    val storedAttachments       = fileService.getAttachments(atarCase)
    val activeNavTab = PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(
      atarCase.status,
      atarCase.assignee.exists(_.id == request.operator.id)
    )
    for {
      sampleTab      <- sampleTabViewModel
      attachmentsTab <- attachmentsTabViewModel
      activityTab    <- activityTabViewModel
      keywordsTab    <- keywordsTabViewModel
      attachments    <- storedAttachments
    } yield Ok(
      atarView(
        atarViewModel,
        applicantTab,
        goodsTab,
        sampleTab,
        attachmentsTab,
        uploadForm,
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
    )
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
    keywordsService.autoCompleteKeywords.map { globalKeywords =>
      KeywordsTabViewModel.fromCase(atarCase, globalKeywords)
    }
}
