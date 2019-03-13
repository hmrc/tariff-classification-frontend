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
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.Future.successful

@Singleton
class AssignQueueController @Inject()(authenticatedAction: AuthenticatedAction,
                                      override val caseService: CasesService,
                                      queueService: QueuesService,
                                      val messagesApi: MessagesApi,
                                      override implicit val config: AppConfig) extends RenderCaseAction {

  override protected def redirect: String => Call = routes.CaseController.applicationDetails

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    c.assignee.isDefined && Set(OPEN, REFERRED, SUSPENDED).contains(c.status)
  }

  private lazy val form: Form[String] = ReleaseCaseForm.form

  def get(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(reference,
      c => successful(views.html.reassign_queue_case(c, form, queueService.getNonGateway, c.queueId.flatMap(queueService.getOneById).map(_.name))))
  }

}
