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
import controllers.{RequestActions, v2}
import javax.inject.{Inject, Singleton}
import models.forms._
import models.forms.v2.LiabilityDetailsForm
import models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import models.viewmodels._
import models.{Case, Permission, _}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext

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
  val liability_view: views.html.v2.liability_view,
  val liability_details_edit: views.html.v2.liability_details_edit,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def displayLiability(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async(implicit request => renderView())

  def renderView(
    activityForm: Form[ActivityFormData] = ActivityForm.form,
    uploadAttachmentForm: Form[String]   = UploadAttachmentForm.form,
    keywordForm: Form[String]            = KeywordForm.form
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    val liabilityCase: Case = request.`case`
    val liabilityViewModel  = CaseViewModel.fromCase(liabilityCase, request.operator)
    val rulingViewModel     = Some(RulingViewModel.fromCase(liabilityCase, request.operator.permissions))
    val appealTabViewModel  = Some(AppealTabViewModel.fromCase(liabilityCase, request.operator))
    val ownCase             = liabilityCase.assignee.exists(_.id == request.operator.id)
    val activeNavTab        = PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(liabilityCase.status, ownCase)
    for {
      (activityEvents, queues) <- liabilityViewActivityDetails(liabilityCase.reference)
      attachmentsTab           <- getAttachmentTab(liabilityCase)
      sampleTab                <- getSampleTab(liabilityCase)
      c592        = Some(C592ViewModel.fromCase(liabilityCase))
      activityTab = Some(ActivityViewModel.fromCase(liabilityCase, activityEvents, queues))
      keywordsTab <- keywordsService.findAll.map(kws =>
                      KeywordsTabViewModel(liabilityCase.reference, liabilityCase.keywords, kws.map(_.name))
                    )
    } yield {
      Ok(
        liability_view(
          liabilityViewModel,
          c592,
          rulingViewModel,
          sampleTab,
          activityTab,
          activityForm,
          attachmentsTab,
          uploadAttachmentForm,
          keywordsTab,
          keywordForm,
          appealTabViewModel,
          activeNavTab
        )
      )
    }
  }

  def liabilityViewActivityDetails(reference: String)(implicit request: AuthenticatedRequest[_]) =
    for {
      events <- eventsService
                 .getFilteredEvents(reference, NoPagination(), Some(EventType.values.diff(EventType.sampleEvents)))
      queues <- queuesService.getAll
    } yield (events, queues)

  private def getAttachmentTab(
    liabilityCase: Case
  )(implicit hc: HeaderCarrier): Future[Option[AttachmentsTabViewModel]] =
    for {
      attachments <- fileService.getAttachments(liabilityCase)
      letter      <- fileService.getLetterOfAuthority(liabilityCase)
    } yield Some(AttachmentsTabViewModel(liabilityCase.reference, attachments, letter))

  private def getSampleTab(c: Case)(implicit request: AuthenticatedRequest[_]) =
    eventsService.getFilteredEvents(c.reference, NoPagination(), Some(EventType.sampleEvents)).map { sampleEvents =>
      SampleStatusTabViewModel(
        c.reference,
        c.sample,
        sampleEvents
      )
    }

  def editLiabilityDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.EDIT_LIABILITY)).async { implicit request =>
      val activeNavTab = PrimaryNavigationViewModel.getSelectedTabBasedOnAssigneeAndStatus(
        request.`case`.status,
        request.`case`.assignee.exists(_.id == request.operator.id)
      )
      successful(
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
        .bindFromRequest
        .fold(
          errorForm => successful(Ok(liability_details_edit(request.`case`, errorForm))),
          updatedCase =>
            casesService
              .updateCase(updatedCase, request.operator)
              .map(_ => Redirect(v2.routes.LiabilityController.displayLiability(reference)))
        )
    }

}
