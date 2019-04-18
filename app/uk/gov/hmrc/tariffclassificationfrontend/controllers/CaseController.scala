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
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, NoPagination, Operator}
import uk.gov.hmrc.tariffclassificationfrontend.service._
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class CaseController @Inject()(verify: RequestActions,
                               casesService: CasesService,
                               keywordsService: KeywordsService,
                               fileService: FileStoreService,
                               eventsService: EventsService,
                               queuesService: QueuesService,
                               decisionForm: DecisionForm,
                               val messagesApi: MessagesApi,
                               implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private type Keyword = String
  private lazy val activityForm: Form[ActivityFormData] = ActivityForm.form
  private lazy val keywordForm: Form[String] = KeywordForm.form

  def trader(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.setPermissions).async { implicit request =>
    getCaseAndRenderView(
      TRADER,
      c => {
        for {
          letter <- fileService.getLetterOfAuthority(c)
        } yield views.html.partials.case_trader(c, letter)
      }
    )
  }

  private def getCaseAndRenderView(page: CaseDetailPage, toHtml: Case => Future[Html])
                                  (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {

    toHtml(request.`case`).map(html => Ok(views.html.case_details(request.`case`, page, html)))
  }

  def applicationDetails(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.setPermissions).async { implicit request =>
    getCaseAndRenderView(
      APPLICATION_DETAILS,
      c => {
        for {
          attachments <- fileService.getAttachments(c)
          letter <- fileService.getLetterOfAuthority(c)
        } yield views.html.partials.application_details(c, attachments, letter)
      }
    )
  }

  def rulingDetails(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.setPermissions).async { implicit request =>
    getCaseAndRenderView(
      RULING,
      c => {
        val form = decisionForm.bindFrom(c.decision)
        fileService
          .getAttachments(c)
          .map(views.html.partials.ruling_details(c, form, _))
      }
    )
  }

  def activityDetails(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.setPermissions).async { implicit request =>

    getCaseAndRenderView(
      ACTIVITY,
      showActivity(_, activityForm)
    )
  }

  private def showActivity(c: Case, f: Form[ActivityFormData])
                          (implicit request: AuthenticatedRequest[AnyContent]): Future[HtmlFormat.Appendable] = {
    for {
      events <- eventsService.getEvents(c.reference, NoPagination())
      queues <- queuesService.getAll
    } yield views.html.partials.activity_details(c, events, f, queues)
  }

  def addNote(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.setPermissions).async { implicit request =>

    def onError: Form[ActivityFormData] => Future[Result] = errorForm => {
      getCaseAndRenderView(
        ACTIVITY,
        showActivity(_, errorForm)
      )
    }

    def onSuccess: ActivityFormData => Future[Result] = validForm => {
      getCaseAndRedirect(
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

  private def getCaseAndRedirect(reference: String, page: CaseDetailPage, toHtml: Case => Future[Call])
                                (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {

    toHtml(request.`case`).map(_ => Redirect(routes.CaseController.activityDetails(reference)))
  }

  def keywordsDetails(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.setPermissions).async { implicit request =>
    getCaseAndRenderView(
      reference,
      KEYWORDS,
      showKeywords(_, keywordForm),
      request.`case`
    )
  }

  private def getCaseAndRenderView(reference: String, page: CaseDetailPage, toHtml: Case => Future[Html], c: Case)
                                  (implicit request: Request[_]): Future[Result] = {

    toHtml(c).map(html => Ok(views.html.case_details(c, page, html)))
  }

  def addKeyword(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.mustHaveWritePermission).async { implicit request =>
    keywordForm.bindFromRequest.fold(
      errorForm =>
        getCaseAndRenderView(
          KEYWORDS,
          showKeywords(_, errorForm)
        ),
      keyword =>
        getCaseAndRenderView(
          KEYWORDS,
          updateKeywords(_, keyword)(keywordsService.addKeyword)
        )
    )
  }

  private def showKeywords(c: Case, f: Form[String])
                          (implicit request: AuthenticatedRequest[AnyContent]): Future[HtmlFormat.Appendable] = {
    keywordsService.autoCompleteKeywords.map { keywords: Seq[String] =>
      views.html.partials.keywords_details(c, keywords, f)
    }
  }

  def removeKeyword(reference: String, keyword: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.mustHaveWritePermission).async { implicit request =>
    getCaseAndRenderView(
      KEYWORDS,
      updateKeywords(_, keyword)(keywordsService.removeKeyword)
    )
  }

  private def updateKeywords(c: Case, keyword: Keyword)
                            (updateKeywords: (Case, Keyword, Operator) => Future[Case])
                            (implicit request: AuthenticatedRequest[AnyContent]): Future[HtmlFormat.Appendable] = {
    for {
      updatedCase <- updateKeywords(c, keyword, request.operator)
      autoCompleteKeywords <- keywordsService.autoCompleteKeywords
    } yield views.html.partials.keywords_details(updatedCase, autoCompleteKeywords, keywordForm)
  }

  def caseNotFound(reference: String): Action[AnyContent] = Action.async { implicit request =>
    successful(Ok(views.html.case_not_found(reference)))
  }
}
