/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.SessionKeys.{backToQueuesLinkLabel, backToQueuesLinkUrl, backToSearchResultsLinkLabel, backToSearchResultsLinkUrl}
import models.Role
import models.request.AuthenticatedRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.read_only_home
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding

@Singleton
class IndexController @Inject() (
  authenticate: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  val read_only_home: read_only_home,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport
    with WithUnsafeDefaultFormBinding {

  def get(): Action[AnyContent] = authenticate { implicit request: AuthenticatedRequest[AnyContent] =>
    request.operator.role match {
      case Role.CLASSIFICATION_MANAGER | Role.CLASSIFICATION_OFFICER =>
        Redirect(routes.OperatorDashboardController.onPageLoad)

      case _ =>
        Ok(read_only_home())
          .addingToSession((backToQueuesLinkLabel, ""), (backToQueuesLinkUrl, routes.IndexController.get().url))
          .removingFromSession(backToSearchResultsLinkLabel, backToSearchResultsLinkUrl)
    }
  }

}
