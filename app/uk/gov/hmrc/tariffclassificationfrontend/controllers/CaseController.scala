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
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms._
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, NoPagination}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, EventsService, FileStoreService, KeywordsService}
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class CaseController @Inject()(authenticatedAction: AuthenticatedAction,
                               casesService: CasesService,
                               keywordsService: KeywordsService,
                               fileService: FileStoreService,
                               eventsService: EventsService,
                               mapper: DecisionFormMapper,
                               decisionForm: DecisionForm,
                               val messagesApi: MessagesApi,
                               implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private lazy val activityForm: Form[ActivityFormData] = ActivityForm.form
  private lazy val keywordForm: Form[String] = KeywordForm.form

  def trader(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      reference,
      CaseDetailPage.TRADER,
      c => {
        for {
          letter <- fileService.getLetterOfAuthority(c)
        } yield views.html.partials.case_trader(c, letter)
      })
  }

  def applicationDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      reference,
      CaseDetailPage.APPLICATION_DETAILS,
      c => {
        for {
          attachments <- fileService.getAttachments(c)
          letter <- fileService.getLetterOfAuthority(c)
        } yield views.html.partials.application_details(c, attachments, letter)
      }
    )
  }

  def rulingDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>

    getCaseAndRenderView(reference, CaseDetailPage.RULING, c => {

      val form = decisionForm.bindFrom(c.decision)

      fileService
        .getAttachments(c)
        .map(views.html.partials.ruling_details(c, form, _))
    })
  }

  def activityDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, CaseDetailPage.ACTIVITY, c => {
      eventsService.getEvents(c.reference, NoPagination()).map(views.html.partials.activity_details(c, _, activityForm))
    })
  }

  def keywordsDetails(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, CaseDetailPage.KEYWORDS,
      c => {
        keywordsService.autoCompleteKeywords.flatMap(autoCompleteKeywords =>
          successful(views.html.partials.keywords_details(c, autoCompleteKeywords, keywordForm)))
      }
    )
  }

  def addNote(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    activityForm.bindFromRequest.fold(
      errorForm =>
        getCaseAndRenderView(
          reference, CaseDetailPage.ACTIVITY, c => eventsService.getEvents(c.reference, NoPagination()).map(views.html.partials.activity_details(c, _, errorForm))),

      validForm =>
        getCaseAndRedirect(reference, CaseDetailPage.ACTIVITY, c => {
          eventsService.addNote(c, validForm.note, request.operator).map(_ =>
            routes.CaseController.activityDetails(reference))
        })
    )
  }

  def addKeyword(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    keywordForm.bindFromRequest.fold(
      errorForm =>
        getCaseAndRenderView(
          reference, CaseDetailPage.KEYWORDS, c => {
            keywordsService.autoCompleteKeywords.flatMap(autoCompleteKeywords =>
              successful(views.html.partials.keywords_details(c, autoCompleteKeywords, errorForm)))
          }),

      keyword =>
        getCaseAndRenderView(reference, CaseDetailPage.KEYWORDS,
          c =>
            for {
              updatedCase <- keywordsService.addKeyword(c, keyword, request.operator)
              autoCompleteKeywords <- keywordsService.autoCompleteKeywords
            } yield views.html.partials.keywords_details(updatedCase, autoCompleteKeywords, keywordForm)
        )
    )
  }

  def removeKeyword(reference: String, keyword: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference, CaseDetailPage.KEYWORDS,
      c =>
        for {
          updatedCase <- keywordsService.removeKeyword(c, keyword, request.operator)
          autoCompleteKeywords <- keywordsService.autoCompleteKeywords
        } yield views.html.partials.keywords_details(updatedCase, autoCompleteKeywords, keywordForm)
    )
  }

  private def getCaseAndRenderView(reference: String, page: CaseDetailPage, toHtml: Case => Future[Html])
                                  (implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).flatMap {
      case Some(c: Case) => toHtml(c).map(html => Ok(views.html.case_details(c, page, html)))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }

  private def getCaseAndRedirect(reference: String, page: CaseDetailPage, toHtml: Case => Future[Call])
                                (implicit request: Request[_]): Future[Result] = {
    casesService.getOne(reference).flatMap {
      case Some(c: Case) => toHtml(c).map(_ => Redirect(routes.CaseController.activityDetails(reference)))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }

}
