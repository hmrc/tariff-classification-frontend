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
import uk.gov.hmrc.tariffclassificationfrontend.controllers.SessionKeys.{backToSearchResultsLinkLabel, backToSearchResultsLinkUrl}
import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes.SearchController
import uk.gov.hmrc.tariffclassificationfrontend.forms.SearchForm
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService, KeywordsService}
import uk.gov.hmrc.tariffclassificationfrontend.views.SearchTab.SearchTab
import uk.gov.hmrc.tariffclassificationfrontend.views.html
import uk.gov.hmrc.tariffclassificationfrontend.views.partials.SearchResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SearchController @Inject()(verify: RequestActions,
                                 casesService: CasesService,
                                 keywordsService: KeywordsService,
                                 fileStoreService: FileStoreService,
                                 val messagesApi: MessagesApi,
                                 implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def search(selectedTab: SearchTab, reference: Option[String] = None, search: Search = Search(), sort: Sort = Sort(), page: Int):
    Action[AnyContent] = (verify.authenticate andThen verify.mustHave(Permission.ADVANCED_SEARCH)).async { implicit request =>

    if (reference.isDefined) {
      successful(Redirect(routes.CaseController.trader(reference.get)))
    } else if (search.isEmpty) {
      keywordsService.autoCompleteKeywords.map { keywords: Seq[String] =>
        Results.Ok(html.advanced_search(SearchForm.form, None, keywords, selectedTab))
      }
    } else {
      keywordsService.autoCompleteKeywords.flatMap(keywords => {
        SearchForm.form.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(Results.Ok(html.advanced_search(formWithErrors, None, keywords, selectedTab)))
          },
          data => for {
            cases: Paged[Case] <- casesService.search(search, sort, SearchPagination(page))
            attachments: Map[Case, Seq[StoredAttachment]] <- fileStoreService.getAttachments(cases.results)
            results: Paged[SearchResult] = cases.map(c => SearchResult(c, attachments.getOrElse(c, Seq.empty)))
          } yield Results.Ok(html.advanced_search(SearchForm.form.fill(data), Some(results), keywords, selectedTab))
                            .addingToSession((backToSearchResultsLinkLabel, "search results"), (backToSearchResultsLinkUrl,
                              s"${SearchController.search(selectedTab, None, search, sort, page).url}#advanced_search_keywords"))
        )
      })
    }
  }

}
