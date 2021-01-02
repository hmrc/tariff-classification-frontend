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

package controllers

import config.AppConfig
import controllers.v2.{AtarController, CorrespondenceController, LiabilityController}
import models.forms._
import javax.inject.{Inject, Singleton}
import models._
import models.request.AuthenticatedCaseRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import service._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaseController @Inject() (
  verify: RequestActions,
  keywordsService: KeywordsService,
  eventsService: EventsService,
  mcc: MessagesControllerComponents,
  liabilityController: LiabilityController,
  atarController: AtarController,
  correspondenceController : CorrespondenceController,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  def get(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)) {
    implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.ATAR           => Redirect(v2.routes.AtarController.displayAtar(reference))
        case ApplicationType.LIABILITY      => Redirect(v2.routes.LiabilityController.displayLiability(reference))
        case ApplicationType.CORRESPONDENCE => Redirect(v2.routes.CorrespondenceController.displayCorrespondence(reference))
      }
  }

  def sampleDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)) { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.ATAR =>
          Redirect(v2.routes.AtarController.displayAtar(reference).withFragment(Tab.SAMPLE_TAB.name))
        case ApplicationType.LIABILITY =>
          Redirect(v2.routes.LiabilityController.displayLiability(reference).withFragment(Tab.SAMPLE_TAB.name))
        case ApplicationType.CORRESPONDENCE =>
          Redirect(v2.routes.CorrespondenceController.displayCorrespondence(reference).withFragment(Tab.SAMPLE_TAB.name))
      }
    }

  def rulingDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)) { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.ATAR =>
          Redirect(v2.routes.AtarController.displayAtar(reference).withFragment(Tab.RULING_TAB.name))
        case ApplicationType.LIABILITY =>
          Redirect(v2.routes.LiabilityController.displayLiability(reference).withFragment(Tab.RULING_TAB.name))
      }
    }

  def activityDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)) { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.ATAR =>
          Redirect(v2.routes.AtarController.displayAtar(reference).withFragment(Tab.ACTIVITY_TAB.name))
        case ApplicationType.LIABILITY =>
          Redirect(v2.routes.LiabilityController.displayLiability(reference).withFragment(Tab.ACTIVITY_TAB.name))
        case ApplicationType.CORRESPONDENCE =>
          Redirect(v2.routes.CorrespondenceController.displayCorrespondence(reference).withFragment(Tab.ACTIVITY_TAB.name))
      }
    }

  def keywordsDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)) { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.ATAR =>
          Redirect(v2.routes.AtarController.displayAtar(reference).withFragment(Tab.KEYWORDS_TAB.name))
        case ApplicationType.LIABILITY =>
          Redirect(v2.routes.LiabilityController.displayLiability(reference).withFragment(Tab.KEYWORDS_TAB.name))
      }
    }

  def attachmentsDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)) { implicit request =>
      request.`case`.application.`type` match {
        case ApplicationType.ATAR =>
          Redirect(v2.routes.AtarController.displayAtar(reference).withFragment(Tab.ATTACHMENTS_TAB.name))
        case ApplicationType.LIABILITY =>
          Redirect(v2.routes.LiabilityController.displayLiability(reference).withFragment(Tab.ATTACHMENTS_TAB.name))
        case ApplicationType.CORRESPONDENCE =>
          Redirect(v2.routes.CorrespondenceController.displayCorrespondence(reference).withFragment(Tab.ATTACHMENTS_TAB.name))
      }
    }

  def addNote(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.ADD_NOTE))
      .async { implicit request =>
        def onError: Form[ActivityFormData] => Future[Result] = errorForm => {
          request.`case`.application.`type` match {
            case ApplicationType.ATAR =>
              atarController.renderView(activityForm = errorForm)
            case ApplicationType.LIABILITY =>
              liabilityController.renderView(activityForm = errorForm)
          }
        }

        def onSuccess: ActivityFormData => Future[Result] = validForm => {
          eventsService
            .addNote(request.`case`, validForm.note, request.operator)
            .map(_ => Redirect(routes.CaseController.activityDetails(reference)))
        }

        ActivityForm.form.bindFromRequest.fold(onError, onSuccess)
      }

  def addKeyword(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.KEYWORDS))
      .async { implicit request =>
        def onError: Form[String] => Future[Result] = (errorForm: Form[String]) => {
          request.`case`.application.`type` match {
            case ApplicationType.ATAR =>
              atarController.renderView(keywordForm = errorForm)
            case ApplicationType.LIABILITY =>
              liabilityController.renderView(keywordForm = errorForm)
          }
        }

        def onSuccess: String => Future[Result] = validForm => {
          keywordsService
            .addKeyword(request.`case`, validForm, request.operator)
            .map(_ => Redirect(routes.CaseController.keywordsDetails(reference)))
        }

        KeywordForm.form.bindFromRequest.fold(onError, onSuccess)
      }

  def removeKeyword(reference: String, keyword: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.KEYWORDS))
      .async { implicit request: AuthenticatedCaseRequest[AnyContent] =>
        keywordsService.removeKeyword(request.`case`, keyword, request.operator) map { _ =>
          Redirect(routes.CaseController.keywordsDetails(reference))
        }
      }
}
