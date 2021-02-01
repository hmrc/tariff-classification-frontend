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

package controllers.v2

import cats.syntax.traverse._
import com.google.inject.Inject
import config.AppConfig
import controllers.RequestActions
import models.request.AuthenticatedRequest
import models.viewmodels._
import models._
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.{CasesService, EventsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class MyCasesController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  eventsService: EventsService,
  mcc: MessagesControllerComponents,
  val myCasesView: views.html.v2.my_cases_view
)(
  implicit val appConfig: AppConfig,
  ec: ExecutionContext
) extends FrontendController(mcc)
    with I18nSupport with Logging {

  private def getReferralEvents(
    cases: Paged[Case]
  )(implicit hc: HeaderCarrier): Future[Map[String, Event]] =
    cases.results.toList
      .traverse { aCase =>
        eventsService.getFilteredEvents(aCase.reference, NoPagination(), Some(Set(EventType.CASE_REFERRAL))).map {
          events =>
            val eventsLatestFirst = events.results.sortBy(_.timestamp)(Event.latestFirst)
            val latestReferralEvent = eventsLatestFirst.collectFirst {
              case event @ Event(_, _, _, caseReference, _) => Map(caseReference -> event)
              case _                                                                => Map.empty
            }
            latestReferralEvent.getOrElse(Map.empty)
        }
      }
      .map(_.foldLeft(Map.empty[String, Event])(_ ++ _))

  private def getCompletedEvents(
    cases: Paged[Case]
  )(implicit hc: HeaderCarrier): Future[Map[String, Event]] =
    cases.results.toList
      .traverse { aCase =>
        eventsService.getFilteredEvents(aCase.reference, NoPagination(), Some(Set(EventType.CASE_COMPLETED))).map {
          events =>
            val eventsLatestFirst = events.results.sortBy(_.timestamp)(Event.latestFirst)
            val latestCompletedEvent = eventsLatestFirst.collectFirst {
              case event @ Event(_, _, _, caseReference, _) => Map(caseReference -> event)
              case _                                                                 => Map.empty
            }
            latestCompletedEvent.getOrElse(Map.empty)
        }
      }
      .map(_.foldLeft(Map.empty[String, Event])(_ ++ _))

  def displayMyCases(activeSubNav: SubNavigationTab = AssignedToMeTab): Action[AnyContent] =
    (verify.authenticated andThen verify.mustHave(Permission.VIEW_MY_CASES)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        for {
          cases <- casesService.getCasesByAssignee(request.operator, NoPagination())
          referralEventsByCase <- getReferralEvents(cases)
          completeEventsByCase <- getCompletedEvents(cases)
          _ = logger.info(completeEventsByCase.toString())
          myCaseStatuses = activeSubNav match {
            case AssignedToMeTab  => ApplicationsTab.assignedToMeCases(cases.results)
            case ReferredByMeTab  => ApplicationsTab.referredByMe(cases.results, referralEventsByCase)
            case CompletedByMeTab => ApplicationsTab.completedByMe(cases.results, completeEventsByCase)
          }
        } yield Ok(myCasesView(myCaseStatuses, activeSubNav))
    }

}
