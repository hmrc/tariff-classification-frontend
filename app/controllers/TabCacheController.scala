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

import controllers.actions.IdentifierAction
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}
import service.TabCacheService
import models.ApplicationType

@Singleton
class TabCacheController @Inject() (
  tabCacheService: TabCacheService,
  identify: IdentifierAction,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with Logging {

  def post(itemType: ApplicationType.Value): Action[AnyContent] =
    identify.async { implicit request =>
      val maybeTab = for {
        bodyText <- request.body.asText
        activeTab <- Tab.fromValue(bodyText.trim)
      } yield activeTab

      maybeTab match {
        case Some(activeTab) =>
          tabCacheService
            .setActiveTab(request.internalId, itemType, activeTab)
            .map(_ => Accepted)
        case None =>
          Future.successful(BadRequest)
      }
    }
}
