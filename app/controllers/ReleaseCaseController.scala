/*
 * Copyright 2024 HM Revenue & Customs
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
import models._
import models.forms.ReleaseCaseForm
import models.request.AuthenticatedCaseRequest
import play.api.data.Form
import play.api.mvc._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.v2.confirmation_case_creation
import views.html.{release_case, resource_not_found}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future.successful
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReleaseCaseController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  queueService: QueuesService,
  mcc: MessagesControllerComponents,
  val releaseCaseView: release_case,
  val confirmation_case_creation: confirmation_case_creation,
  val resource_not_found: resource_not_found,
  implicit val appConfig: AppConfig
)(using val executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with RenderCaseAction
    with WithUnsafeDefaultFormBinding {

  private lazy val releaseCaseForm: Form[String]   = ReleaseCaseForm.form
  override protected val config: AppConfig         = appConfig
  override protected val caseService: CasesService = casesService

  def releaseCase(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.RELEASE_CASE)).async(using request => releaseCase(releaseCaseForm, reference))

  def releaseCaseToQueue(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.RELEASE_CASE)).async { implicit request =>
      def onInvalidForm(formWithErrors: Form[String]): Future[Result] =
        releaseCase(formWithErrors, reference)

      def onValidForm(queueSlug: String): Future[Result] =
        queueService.getOneBySlug(queueSlug) flatMap {
          case None => successful(Ok(resource_not_found(s"Queue $queueSlug")))
          case Some(q: Queue) =>
            validateAndRedirect(casesService.releaseCase(_, q, request.operator).map { _ =>
              routes.ReleaseCaseController.confirmReleaseCase(reference)
            })
        }

      releaseCaseForm.bindFromRequest().fold(onInvalidForm, onValidForm)
    }

  private def releaseCase(f: Form[String], caseRef: String)(using
    request: AuthenticatedCaseRequest[_]
  ): Future[Result] =
    getCaseAndRenderView(
      caseRef,
      c => queueService.getAllForCaseType(c.application.`type`).map(releaseCaseView(c, f, _))
    )

  def confirmReleaseCase(reference: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.VIEW_CASES)).async { implicit request =>
      def queueNotFound(using request: AuthenticatedCaseRequest[_]) =
        successful(resource_not_found(s"Case Queue"))

      renderView(
        c => c.status == CaseStatus.OPEN,
        c =>
          c.queueId
            .map(id =>
              queueService.getOneById(id) flatMap {
                case Some(queue) => successful(confirmation_case_creation(c, queue.name))
                case None        => queueNotFound
              }
            )
            .getOrElse(queueNotFound)
      )
    }

}
