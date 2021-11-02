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
import javax.inject.{Inject, Singleton}
import models._
import models.forms.ReleaseCaseForm
import models.request.AuthenticatedCaseRequest
import play.api.data.Form
import play.api.mvc._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class ReassignCaseController @Inject() (
  verify: RequestActions,
  override val caseService: CasesService,
  queueService: QueuesService,
  mcc: MessagesControllerComponents,
  val reassign_queue_case: views.html.reassign_queue_case,
  val confirm_reassign_case: views.html.confirm_reassign_case,
  val resource_not_found: views.html.resource_not_found,
  override implicit val config: AppConfig
) extends FrontendController(mcc)
    with RenderCaseAction {

  private lazy val form: Form[String] = ReleaseCaseForm.form

  def showAvailableQueues(reference: String, origin: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.MOVE_CASE_BACK_TO_QUEUE)).async { implicit request =>
      reassignToQueue(form, origin)
    }

  private def reassignToQueue(f: Form[String], origin: String)(
    implicit request: AuthenticatedCaseRequest[_]
  ): Future[Result] =
    validateAndRenderView(c =>
      for {
        queues        <- queueService.getAllForCaseType(c.application.`type`)
        assignedQueue <- c.queueId.map(queueService.getOneById).getOrElse(successful(None))
      } yield reassign_queue_case(c, f, queues, assignedQueue, origin)
    )

  def reassignCase(reference: String, origin: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.MOVE_CASE_BACK_TO_QUEUE)).async { implicit request =>
      def onInvalidForm(formWithErrors: Form[String]): Future[Result] =
        reassignToQueue(formWithErrors, origin)

      def onValidForm(queueSlug: String): Future[Result] =
        queueService.getOneBySlug(queueSlug) flatMap {
          case None => successful(Ok(resource_not_found(s"Queue $queueSlug")))
          case Some(q: Queue) =>
            validateAndRedirect(
              caseService
                .reassignCase(_, q, request.operator)
                .map(_ => routes.ReassignCaseController.confirmReassignCase(reference, origin))
            )
        }

      form.bindFromRequest.fold(onInvalidForm, onValidForm)
    }

  def confirmReassignCase(reference: String, origin: String): Action[AnyContent] =
    (verify.authenticated
      andThen verify.casePermissions(reference)
      andThen verify.mustHave(Permission.ASSIGN_CASE)).async { implicit request =>
      def queueNotFound(implicit request: AuthenticatedCaseRequest[_]) =
        successful(resource_not_found(s"Case Queue"))

      renderView(
        c => CaseStatus.openStatuses.contains(c.status),
        c =>
          c.queueId
            .map(id =>
              queueService.getOneById(id) flatMap {
                case Some(queue) => successful(confirm_reassign_case(c, queue, origin))
                case None        => queueNotFound
              }
            )
            .getOrElse(queueNotFound)
      )
    }

}
