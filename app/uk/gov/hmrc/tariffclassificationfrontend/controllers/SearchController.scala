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
import uk.gov.hmrc.tariffclassificationfrontend.forms.{SearchForm, SearchFormData}
import uk.gov.hmrc.tariffclassificationfrontend.models.Search
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views.html

import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SearchController @Inject()(authenticatedAction: AuthenticatedAction,
                                 casesService: CasesService,
                                 val messagesApi: MessagesApi,
                                 implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def search(reference: Option[String] = None, search: Search = Search()): Action[AnyContent] = authenticatedAction.async { implicit request =>
    if (reference.isDefined) {
      successful(Redirect(routes.CaseController.summary(reference.get)))
    } else if (search.isEmpty) {
      Future.successful(Results.Ok(html.advanced_search(fillForm(search), None)))
    } else {
      casesService.search(search) map { results =>
        Results.Ok(html.advanced_search(fillForm(search), Some(results)))
      }
    }
  }

  private def fillForm(search: Search) = SearchForm.form.fill(
    SearchFormData(
      search.traderName.getOrElse("")
    )
  )


}
