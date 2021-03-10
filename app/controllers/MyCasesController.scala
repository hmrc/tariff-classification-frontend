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
import controllers.SessionKeys._
import controllers.routes.MyCasesController
import javax.inject.{Inject, Singleton}
import models.{NoPagination, Permission, Queue}
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MyCasesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  queuesService: QueuesService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  def myCases(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.VIEW_MY_CASES)).async {
    implicit request =>
      for {
        cases                         <- casesService.getCasesByAssignee(request.operator, NoPagination())
        queues: Seq[Queue]            <- queuesService.getAll
      } yield Ok(views.html.my_cases(queues, cases.results, request.operator, Map.empty))
        .addingToSession(
          (backToQueuesLinkLabel, request2Messages(implicitly)("cases.menu.my-cases")),
          (backToQueuesLinkUrl, MyCasesController.myCases().url)
        )
        .removingFromSession(backToSearchResultsLinkLabel, backToSearchResultsLinkUrl)
  }

}
