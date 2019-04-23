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
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.ReleaseCaseForm
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Queue}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class ReassignCaseController @Inject()(verify: RequestActions,
                                       override val caseService: CasesService,
                                       queueService: QueuesService,
                                       val messagesApi: MessagesApi,
                                       override implicit val config: AppConfig) extends RenderCaseAction {

  private lazy val form: Form[String] = ReleaseCaseForm.form

  def showAvailableQueues(reference: String, origin: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.mustHaveWritePermission).async { implicit request =>
    reassignToQueue(form, reference, origin)
  }

  def reassignCase(reference: String, origin: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.mustHaveWritePermission).async { implicit request =>

    def onInvalidForm(formWithErrors: Form[String]): Future[Result] = {
      reassignToQueue(formWithErrors, reference, origin)
    }

    def onValidForm(queueSlug: String): Future[Result] = {
      queueService.getOneBySlug(queueSlug) flatMap {
        case None => successful(Ok(views.html.resource_not_found(s"Queue $queueSlug")))
        case Some(q: Queue) =>
          validateAndRenderView(
            caseService.reassignCase(_, q, request.operator).map(views.html.confirm_reassign_case(_, q, origin))
          )
      }
    }

    form.bindFromRequest.fold(onInvalidForm, onValidForm)
  }

  private def reassignToQueue(f: Form[String], caseRef: String, origin: String)
                             (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    validateAndRenderView(c =>
      for {
        queues <- queueService.getNonGateway
        assignedQueue <- c.queueId.map(queueService.getOneById).getOrElse(successful(None))
      } yield views.html.reassign_queue_case(c, f, queues, assignedQueue, origin)
    )
  }

  override protected def redirect: String => Call = {
    // in case this is called from the "assigned cases" journey, we should redirect to `/queue/assigned/:assigneeId`
    routes.CaseController.applicationDetails
  }

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    c.assignee.isDefined && reassignCaseStatuses.contains(c.status)
  }

}
