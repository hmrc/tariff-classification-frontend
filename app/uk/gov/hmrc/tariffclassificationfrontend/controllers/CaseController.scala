/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms._
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.service._
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CaseController @Inject()(verify: RequestActions,
                               casesService: CasesService,
                               keywordsService: KeywordsService,
                               fileService: FileStoreService,
                               eventsService: EventsService,
                               queuesService: QueuesService,
                               commodityCodeService: CommodityCodeService,
                               decisionForm: DecisionForm,
                               val messagesApi: MessagesApi,
                               implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private type Keyword = String
  private lazy val activityForm: Form[ActivityFormData] = ActivityForm.form
  private lazy val keywordForm: Form[String] = KeywordForm.form
  private val pagesWithStartTabIndexes: Map[CaseDetailPage, Int] = Map(TRADER -> 1000, APPLICATION_DETAILS -> 2000, SAMPLE_DETAILS -> 3000, ATTACHMENTS -> 4000,
    ACTIVITY -> 5000, KEYWORDS -> 6000, RULING -> 7000, APPEAL -> 8000)

  def get(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)) { implicit request =>
    request.`case`.application.`type` match {
      case ApplicationType.BTI => Redirect(routes.CaseController.trader(reference))
      case ApplicationType.LIABILITY_ORDER => Redirect(routes.CaseController.activityDetails(reference))
    }
  }

  def trader(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    validateAndRenderView(
      TRADER,
      c => {
        for {
          letter <- fileService.getLetterOfAuthority(c)
        } yield views.html.partials.case_trader(c, letter, pagesWithStartTabIndexes(TRADER))
      },
      "tab-item-Applicant"
    )
  }

  def applicationDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    validateAndRenderView(
      APPLICATION_DETAILS,
      c => {
        for {
          attachments <- fileService.getAttachments(c)
          letter <- fileService.getLetterOfAuthority(c)
        } yield views.html.partials.application_details(c, attachments, letter, pagesWithStartTabIndexes(APPLICATION_DETAILS))
      },
      "tab-item-Item"
    )
  }

  def sampleDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    validateAndRenderView(
      SAMPLE_DETAILS,
      c => {
        for {
          events <- eventsService.getFilteredEvents(c.reference, NoPagination(), Some(Set(EventType.SAMPLE_STATUS_CHANGE)))
        } yield views.html.partials.sample.sample_details(c, events, pagesWithStartTabIndexes(SAMPLE_DETAILS))
      },
      "tab-item-Sample"
    )
  }

  def rulingDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    validateAndRenderView(
      RULING,
      c => for {
        attachments <- fileService.getAttachments(c)
        commodityCode = c.decision.map(_.bindingCommodityCode).flatMap(commodityCodeService.find)
      } yield views.html.partials.ruling.ruling_details(c, decisionForm.bindFrom(c.decision), attachments, commodityCode, pagesWithStartTabIndexes(RULING)),
      "tab-item-Ruling"
    )
  }

  def activityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>

    validateAndRenderView(
      ACTIVITY,
      showActivity(_, activityForm),
      "tab-item-Activity"
    )
  }

  def addNote(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.ADD_NOTE)).async { implicit request =>

    def onError: Form[ActivityFormData] => Future[Result] = errorForm => {
      validateAndRenderView(
        ACTIVITY,
        showActivity(_, errorForm),
        "tab-item-Activity"
      )
    }

    def onSuccess: ActivityFormData => Future[Result] = validForm => {
      validateAndRedirect(
        reference,
        ACTIVITY,
        c => {
          eventsService.addNote(c, validForm.note, request.operator).map(_ =>
            routes.CaseController.activityDetails(reference)
          )
        }
      )
    }

    activityForm.bindFromRequest.fold(onError, onSuccess)
  }

  def keywordsDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    validateAndRenderView(
      reference,
      KEYWORDS,
      showKeywords(_, keywordForm),
      request.`case`,
      "tab-item-Keywords"
    )
  }

  def addKeyword(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.KEYWORDS)).async { implicit request =>
    keywordForm.bindFromRequest.fold(
      errorForm => {
        validateAndRenderView(
          KEYWORDS,
          showKeywords(_, errorForm),
          "tab-item-Keywords"
        )
      },
      keyword =>
        validateAndRenderView(
          KEYWORDS,
          updateKeywords(_, keyword)(keywordsService.addKeyword),
          "tab-item-Keywords"
        )
    )
  }

  def removeKeyword(reference: String, keyword: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.KEYWORDS)).async { implicit request: AuthenticatedCaseRequest[AnyContent] =>
    validateAndRenderView(
      KEYWORDS,
      updateKeywords(_, keyword)(keywordsService.removeKeyword),
      "tab-item-Keywords"
    )
  }

  private def updateKeywords(c: Case, keyword: Keyword)
                            (updateKeywords: (Case, Keyword, Operator) => Future[Case])
                            (implicit request: AuthenticatedRequest[AnyContent]): Future[HtmlFormat.Appendable] = {
    for {
      updatedCase <- updateKeywords(c, keyword, request.operator)
      autoCompleteKeywords <- keywordsService.autoCompleteKeywords
    } yield views.html.partials.keywords_details(updatedCase, autoCompleteKeywords, keywordForm, pagesWithStartTabIndexes(KEYWORDS))
  }

  private def showKeywords(c: Case, f: Form[String])
                          (implicit request: AuthenticatedRequest[AnyContent]): Future[HtmlFormat.Appendable] = {
    keywordsService.autoCompleteKeywords.map { keywords: Seq[String] =>
      views.html.partials.keywords_details(c, keywords, f, pagesWithStartTabIndexes(KEYWORDS))
    }
  }

  private def validateAndRenderView(reference: String, page: CaseDetailPage, toHtml: Case => Future[Html], c: Case, activeTabId: String)
                                   (implicit request: Request[_]): Future[Result] = {

    toHtml(c).map(html => Ok(views.html.case_details(c, page, html, Some(activeTabId))))
  }

  private def validateAndRenderView(page: CaseDetailPage, toHtml: Case => Future[Html], activeTabId: String)
                                   (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {

    toHtml(request.`case`).map(html => Ok(views.html.case_details(request.`case`, page, html, Some(activeTabId))))
  }

  private def validateAndRedirect(reference: String, page: CaseDetailPage, toHtml: Case => Future[Call])
                                 (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {

    toHtml(request.`case`).map(_ => Redirect(routes.CaseController.activityDetails(reference)))
  }

  private def showActivity(c: Case, f: Form[ActivityFormData])
                          (implicit request: AuthenticatedRequest[AnyContent]): Future[HtmlFormat.Appendable] = {
    for {
      events <- eventsService.getFilteredEvents(c.reference, NoPagination(), Some(EventType.values.diff(Set(EventType.SAMPLE_STATUS_CHANGE))))
      queues <- queuesService.getAll
    } yield views.html.partials.activity_details(c, events, f, queues, pagesWithStartTabIndexes(ACTIVITY))
  }

}
