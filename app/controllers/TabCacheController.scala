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

import cats.syntax.all._
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.ApplicationType
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.TabCacheService
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TabCacheController @Inject() (
  tabCacheService: TabCacheService,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  verify: RequestActions,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with Logging
    with WithUnsafeDefaultFormBinding {

  def post(reference: String, itemType: ApplicationType): Action[AnyContent] =
    identify.async { implicit request =>
      val maybeTab = for {
        bodyText  <- request.body.asText
        activeTab <- Tab.fromValue(bodyText.trim)
      } yield activeTab

      maybeTab match {
        case Some(activeTab) =>
          tabCacheService
            .setActiveTab(request.internalId, reference, itemType, activeTab)
            .map(_ => Accepted)
        case None =>
          Future.successful(BadRequest)
      }
    }

  def get(reference: String, itemType: ApplicationType): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen identify andThen getData).async {
      implicit request =>
        tabCacheService
          .getActiveTab(request.internalId, reference, itemType)
          .map(tab => Ok(tab.map(_.name).orEmpty))
    }

}
