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
import controllers.SessionKeys._
import controllers.routes.QueuesController
import javax.inject.{Inject, Singleton}
import models.ApplicationType.ApplicationType
import models.{ApplicationType, _}
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

@Singleton
class QueuesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  queuesService: QueuesService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  def queue(slug: String, caseType: Option[String] = None): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_QUEUE_CASES)).async { implicit request =>
      val types: Seq[ApplicationType] = caseType
        .map(x => Seq[ApplicationType](ApplicationType.withName(x)))
        .getOrElse(Seq(ApplicationType.BTI, ApplicationType.LIABILITY_ORDER))

      queuesService.getOneBySlug(slug) flatMap {
        case None => successful(Ok(views.html.resource_not_found()))
        case Some(q: Queue) =>
          for {
            cases            <- casesService.getCasesByQueue(q, NoPagination(), types)
            queues           <- queuesService.getAll
            caseCountByQueue <- casesService.countCasesByQueue(request.operator)
          } yield Ok(views.html.queue(queues, q, caseCountByQueue, cases, types.mkString(",")))
            .addingToSession(
              (backToQueuesLinkLabel, s"${q.name} cases"),
              (backToQueuesLinkUrl, QueuesController.queue(q.slug, caseType).url)
            )
            .removingFromSession(backToSearchResultsLinkLabel, backToSearchResultsLinkUrl)
      }
    }

}
