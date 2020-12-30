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
import models.forms.ReleaseCaseForm
import javax.inject.{Inject, Singleton}
import models.request.AuthenticatedCaseRequest
import models.{CaseStatus, Permission, Queue}
import play.api.data.Form
import play.api.mvc._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class ReleaseCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  queueService: QueuesService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with RenderCaseAction {

  private lazy val releaseCaseForm: Form[String]   = ReleaseCaseForm.form
  override protected val config: AppConfig         = appConfig
  override protected val caseService: CasesService = casesService

  def releaseCase(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.RELEASE_CASE)).async(implicit request => releaseCase(releaseCaseForm, reference))

  def releaseCaseToQueue(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.RELEASE_CASE)).async { implicit request =>
      def onInvalidForm(formWithErrors: Form[String]): Future[Result] =
        releaseCase(formWithErrors, reference)

      def onValidForm(queueSlug: String): Future[Result] =
        queueService.getOneBySlug(queueSlug) flatMap {
          case None => successful(Ok(views.html.resource_not_found(s"Queue $queueSlug")))
          case Some(q: Queue) =>
            validateAndRedirect(casesService.releaseCase(_, q, request.operator).map { _ =>
              routes.ReleaseCaseController.confirmReleaseCase(reference)
            })
        }

      releaseCaseForm.bindFromRequest.fold(onInvalidForm, onValidForm)
    }

  private def releaseCase(f: Form[String], caseRef: String)(
    implicit request: AuthenticatedCaseRequest[_]
  ): Future[Result] =
    getCaseAndRenderView(
      caseRef,
      c => queueService.getAllForCaseType(c.application.`type`).map(views.html.release_case(c, f, _))
    )

  def confirmReleaseCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)).async { implicit request =>
      def queueNotFound(implicit request: AuthenticatedCaseRequest[_]) =
        successful(views.html.resource_not_found(s"Case Queue"))

      renderView(
        c => c.status == CaseStatus.OPEN,
        c =>
          c.queueId
            .map(id =>
              queueService.getOneById(id) flatMap {
                case Some(queue) => successful(views.html.confirm_release_case(c, queue.name))
                case None        => queueNotFound
              }
            )
            .getOrElse(queueNotFound)
      )
    }
}
