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

package controllers

import config.AppConfig
import models.forms._
import javax.inject.{Inject, Singleton}
import models.TabIndexes.tabIndexFor
import models._
import models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.{Html, HtmlFormat}
import service._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.CaseDetailPage._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.viewmodels.atar.RulingTabViewModel
import models.viewmodels.atar.ApplicantTabViewModel
import models.viewmodels.atar.GoodsTabViewModel
import models.viewmodels.atar.SampleTabViewModel
import models.viewmodels.ActivityViewModel
import models.viewmodels.KeywordsTabViewModel

@Singleton
class CaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  keywordsService: KeywordsService,
  fileService: FileStoreService,
  eventsService: EventsService,
  queuesService: QueuesService,
  commodityCodeService: CommodityCodeService,
  decisionForm: DecisionForm,
  countriesService: CountriesService,
  mcc: MessagesControllerComponents,
  val caseDetailsView: views.html.case_details,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  private type Keyword = String
  private lazy val activityForm: Form[ActivityFormData] = ActivityForm.form
  private lazy val keywordForm: Form[String]            = KeywordForm.form

  def get(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)) {
    implicit request =>
      request.`case`.application.`type` match {
        // case ApplicationType.ATAR => Redirect(routes.CaseController.applicantDetails(reference))
        case ApplicationType.ATAR => Redirect(v2.routes.AtarController.displayAtar(reference))
        case ApplicationType.LIABILITY => Redirect(v2.routes.LiabilityController.displayLiability(reference))
      }
  }

  def applicantDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      validateAndRenderView(
        TRADER,
        c => {
          val countryNames = countriesService.getAllCountriesById.mapValues(_.countryName)
          val applicantTab = ApplicantTabViewModel.fromCase(c, countryNames)
          Future.successful( views.html.partials.case_trader(applicantTab, tabIndexFor(TRADER)))
        },
        ActiveTab.Applicant
      )
    }

  def itemDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      validateAndRenderView(
        APPLICATION_DETAILS,
        c => {
          val goodsTab = GoodsTabViewModel.fromCase(c)
          Future.successful(views.html.partials.application_details(goodsTab, tabIndexFor(APPLICATION_DETAILS)))
        },
        ActiveTab.Item
      )
    }

  def sampleDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.LIABILITY =>
          Future.successful(Redirect(v2.routes.LiabilityController.displayLiability(reference)))
        case ApplicationType.ATAR =>
          validateAndRenderView(
            SAMPLE_DETAILS,
            c =>
              for {
                events <- eventsService.getFilteredEvents(c.reference, NoPagination(), Some(EventType.sampleEvents))
                sampleTab = SampleTabViewModel.fromCase(c, events)
              } yield views.html.partials.sample.sample_details(sampleTab, tabIndexFor(SAMPLE_DETAILS)),
            ActiveTab.Sample
          )
      }
    }

  def rulingDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      validateAndRenderView(
        RULING,
        c =>
          for {
            attachments <- fileService.getAttachments(c)
            rulingTab = RulingTabViewModel.fromCase(c)
          } yield views.html.partials.ruling
            .ruling_details(rulingTab, decisionForm.bindFrom(c.decision), attachments, tabIndexFor(RULING)),
        ActiveTab.Ruling
      )
    }

  def activityDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      validateAndRenderView(
        ACTIVITY,
        showActivity(_, activityForm),
        ActiveTab.Activity
      )
    }

  def addNote(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.ADD_NOTE)).async { implicit request =>
      def onError: Form[ActivityFormData] => Future[Result] = errorForm => {
        validateAndRenderView(
          ACTIVITY,
          showActivity(_, errorForm),
          ActiveTab.Activity
        )
      }

      def onSuccess: ActivityFormData => Future[Result] = validForm => {
        validateAndRedirect(
          reference,
          ACTIVITY,
          c =>
            eventsService
              .addNote(c, validForm.note, request.operator)
              .map(_ => routes.CaseController.activityDetails(reference))
        )
      }

      activityForm.bindFromRequest.fold(onError, onSuccess)
    }

  def keywordsDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference)).async { implicit request =>
      validateAndRenderView(
        reference,
        KEYWORDS,
        showKeywords(_, keywordForm),
        request.`case`,
        ActiveTab.Keywords
      )
    }

  def addKeyword(reference: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.KEYWORDS)).async { implicit request =>
      keywordForm.bindFromRequest.fold(
        errorForm =>
          validateAndRenderView(
            KEYWORDS,
            showKeywords(_, errorForm),
            ActiveTab.Keywords
          ),
        keyword =>
          validateAndRenderView(
            KEYWORDS,
            updateKeywords(_, keyword)(keywordsService.addKeyword),
            ActiveTab.Keywords
          )
      )
    }

  def removeKeyword(reference: String, keyword: String): Action[AnyContent] =
    (verify.authenticated andThen
      verify.casePermissions(reference) andThen
      verify.mustHave(Permission.KEYWORDS)).async { implicit request: AuthenticatedCaseRequest[AnyContent] =>
      validateAndRenderView(
        KEYWORDS,
        updateKeywords(_, keyword)(keywordsService.removeKeyword),
        ActiveTab.Keywords
      )
    }

  private def updateKeywords(c: Case, keyword: Keyword)(
    updateKeywords: (Case, Keyword, Operator) => Future[Case]
  )(implicit request: AuthenticatedRequest[AnyContent]): Future[HtmlFormat.Appendable] =
    for {
      updatedCase          <- updateKeywords(c, keyword, request.operator)
      autoCompleteKeywords <- keywordsService.autoCompleteKeywords
      keywordsTab = KeywordsTabViewModel.fromCase(c, autoCompleteKeywords)
    } yield views.html.partials.keywords_details(keywordsTab, keywordForm, tabIndexFor(KEYWORDS))

  private def showKeywords(c: Case, f: Form[String])(
    implicit request: AuthenticatedRequest[AnyContent]
  ): Future[HtmlFormat.Appendable] =
    keywordsService.autoCompleteKeywords.map { keywords: Seq[String] =>
      val keywordsTab = KeywordsTabViewModel.fromCase(c, keywords)
      views.html.partials.keywords_details(keywordsTab, f, tabIndexFor(KEYWORDS))
    }

  private def validateAndRenderView(
    reference: String,
    page: CaseDetailPage,
    toHtml: Case => Future[Html],
    c: Case,
    activeTab: ActiveTab
  )(implicit request: AuthenticatedCaseRequest[_]): Future[Result] =
    toHtml(c).map(html => Ok(caseDetailsView(c, page, html, Some(activeTab))))

  private def validateAndRenderView(page: CaseDetailPage, toHtml: Case => Future[Html], activeTab: ActiveTab)(
    implicit request: AuthenticatedCaseRequest[_]
  ): Future[Result] =
    toHtml(request.`case`).map(html => Ok(caseDetailsView(request.`case`, page, html, Some(activeTab))))

  private def validateAndRedirect(reference: String, page: CaseDetailPage, toHtml: Case => Future[Call])(
    implicit request: AuthenticatedCaseRequest[_]
  ): Future[Result] =
    toHtml(request.`case`).map(_ => Redirect(routes.CaseController.activityDetails(reference)))

  private def showActivity(c: Case, f: Form[ActivityFormData])(
    implicit request: AuthenticatedRequest[AnyContent]
  ): Future[HtmlFormat.Appendable] =
    for {
      events <- eventsService
                 .getFilteredEvents(c.reference, NoPagination(), Some(EventType.values.diff(EventType.sampleEvents)))
      queues <- queuesService.getAll
      activityTab = ActivityViewModel.fromCase(c, events, queues)
    } yield views.html.partials.activity_details(activityTab, f, tabIndexFor(ACTIVITY))

  def getCountryName(code: String) = countriesService.getAllCountries.find(_.code == code).map(_.countryName)

}
