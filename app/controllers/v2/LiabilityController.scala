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
import controllers.{RequestActions, v2}
import javax.inject.{Inject, Singleton}
import models.forms.{ActivityForm, ActivityFormData, KeywordForm, UploadAttachmentForm}
import models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import models.viewmodels._
import models.{Case, Permission, _}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, EventsService, FileStoreService, KeywordsService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class LiabilityController @Inject()(
                                     verify: RequestActions,
                                     casesService: CasesService,
                                     eventsService: EventsService,
                                     queuesService: QueuesService,
                                     fileService: FileStoreService,
                                     keywordsService: KeywordsService,
                                     mcc: MessagesControllerComponents,
                                     val liability_view: views.html.v2.liability_view,
                                     implicit val appConfig: AppConfig
                                   ) extends FrontendController(mcc) with I18nSupport {

  def displayLiability(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async {
    implicit request => {
      buildLiabilityView()
    }
  }

  def buildLiabilityView(
                          activityForm: Form[ActivityFormData] = ActivityForm.form,
                          uploadAttachmentForm: Form[String] = UploadAttachmentForm.form,
                          keywordForm: Form[String] = KeywordForm.form
                        )(implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    val liabilityCase: Case = request.`case`
    val liabilityViewModel = LiabilityViewModel.fromCase(liabilityCase, request.operator)
    val rulingViewModel = Some(RulingViewModel.fromCase(liabilityCase, request.operator.permissions))

    for {
      (activityEvents, queues) <- liabilityViewActivityDetails(liabilityCase.reference)
      attachmentsTab <- getAttachmentTab(liabilityCase)
      sampleTab <- getSampleTab(liabilityCase)
      c592 = Some(C592ViewModel.fromCase(liabilityCase))
      activityTab = Some(ActivityViewModel.fromCase(liabilityCase, activityEvents, queues))
      keywordsTab <- keywordsService.autoCompleteKeywords.map(kws => KeywordsTabViewModel(liabilityCase.reference, liabilityCase.keywords, kws))
    } yield {
      Ok(liability_view(
        liabilityViewModel,
        c592,
        rulingViewModel,
        sampleTab,
        activityTab,
        activityForm,
        attachmentsTab,
        uploadAttachmentForm,
        keywordsTab,
        keywordForm
      ))
    }
  }

  def liabilityViewActivityDetails(reference: String)(implicit request: AuthenticatedRequest[_]) = {
    for {
      events <- eventsService.getFilteredEvents(reference, NoPagination(), Some(EventType.values.diff(EventType.sampleEvents)))
      queues <- queuesService.getAll
    } yield (events, queues)
  }

  private def getAttachmentTab(liabilityCase: Case)(implicit hc: HeaderCarrier): Future[Option[AttachmentsTabViewModel]] = {
    for {
      attachments <- fileService.getAttachments(liabilityCase)
      letter <- fileService.getLetterOfAuthority(liabilityCase)
    } yield Some(AttachmentsTabViewModel(liabilityCase.reference, attachments, letter))
  }

  private def getSampleTab(c: Case)(implicit request: AuthenticatedRequest[_]) = {
    eventsService.getFilteredEvents(c.reference, NoPagination(),Some(EventType.sampleEvents)).map {
      sampleEvents => SampleStatusTabViewModel(
        c.reference,
        c.sample,
        sampleEvents
      )
    }
  }

  def addNote(reference: String): Action[AnyContent] = (verify.authenticated andThen
    verify.casePermissions(reference) andThen verify.mustHave(Permission.ADD_NOTE)).async { implicit request =>

    def onError: Form[ActivityFormData] => Future[Result] = errorForm => {
      buildLiabilityView(errorForm)
    }

    def onSuccess: ActivityFormData => Future[Result] = validForm => {
      eventsService.addNote(request.`case`, validForm.note, request.operator)
        .map(_ => Redirect(v2.routes.LiabilityController.displayLiability(reference)))
    }

    ActivityForm.form.bindFromRequest.fold(onError, onSuccess)
  }

  def addKeyword(reference: String): Action[AnyContent] = (verify.authenticated andThen
    verify.casePermissions(reference) andThen verify.mustHave(Permission.KEYWORDS)).async {
    implicit request => ???


  }

}
