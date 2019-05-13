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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.controllers.SessionKeys._
import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes.MyCasesController
import uk.gov.hmrc.tariffclassificationfrontend.models.{NoPagination, Permission, Queue}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, QueuesService}
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MyCasesController @Inject()(verify: RequestActions,
                                  casesService: CasesService,
                                  queuesService: QueuesService,
                                  val messagesApi: MessagesApi,
                                  implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def myCases(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.VIEW_MY_CASES)).async { implicit request =>
    for {
      cases <- casesService.getCasesByAssignee(request.operator, NoPagination())
      queues: Seq[Queue] <- queuesService.getAll
      countQueues: Map[String, Int] <- casesService.countCasesByQueue(request.operator)
    } yield Ok(views.html.my_cases(queues, cases, request.operator, countQueues))
         .addingToSession((backToQueuesLinkLabel, messagesApi("cases.menu.my-cases")), (backToQueuesLinkUrl, MyCasesController.myCases().url))
         .removingFromSession(backToSearchResultsLinkLabel, backToSearchResultsLinkUrl)
  }
}
