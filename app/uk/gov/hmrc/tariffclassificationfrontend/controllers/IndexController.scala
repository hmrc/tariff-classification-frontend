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
import uk.gov.hmrc.tariffclassificationfrontend.controllers.SessionKeys.{backToQueuesLinkLabel, backToQueuesLinkUrl, backToSearchResultsLinkLabel, backToSearchResultsLinkUrl}
import uk.gov.hmrc.tariffclassificationfrontend.models.Role
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.views.html.read_only_home

@Singleton
class IndexController @Inject()(authenticate: AuthenticatedAction,
                                val messagesApi: MessagesApi,
                                implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def get(): Action[AnyContent] = authenticate { implicit request: AuthenticatedRequest[AnyContent] =>
    request.operator.role match {
      case Role.CLASSIFICATION_MANAGER | Role.CLASSIFICATION_OFFICER =>
        Redirect(routes.MyCasesController.myCases())
      case _ =>
        Ok(read_only_home())
          .addingToSession((backToQueuesLinkLabel, "Search"), (backToQueuesLinkUrl, routes.IndexController.get().url))
          .removingFromSession(backToSearchResultsLinkLabel, backToSearchResultsLinkUrl)
    }
  }

}
