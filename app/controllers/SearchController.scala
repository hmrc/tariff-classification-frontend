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
import controllers.SessionKeys.{backToSearchResultsLinkLabel, backToSearchResultsLinkUrl}
import controllers.routes.SearchController
import models.forms.SearchForm
import javax.inject.{Inject, Singleton}
import models._
import play.api.i18n.I18nSupport
import play.api.mvc._
import service.{CasesService, FileStoreService, KeywordsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.SearchTab.SearchTab
import views.partials.SearchResult
import views.{SearchTab, html}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SearchController @Inject()(
  verify: RequestActions,
  casesService: CasesService,
  keywordsService: KeywordsService,
  fileStoreService: FileStoreService,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc) with I18nSupport {

  def search(selectedTab: SearchTab,addToSearch: Option[Boolean] = None, reference: Option[String] = None, search: Search = Search(), sort: Sort = Sort(), page: Int):
    Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.ADVANCED_SEARCH)).async { implicit request =>

    val focus: SearchTab = if(addToSearch.contains(true)) SearchTab.SEARCH_BOX else selectedTab
    if (reference.isDefined) {
      reference match {
        case Some(ref) if ref.trim.nonEmpty => successful(Redirect(routes.CaseController.get(ref.trim)))
        case _ => successful(Redirect(routes.IndexController.get()))
      }
    } else if (search.isEmpty) {
      keywordsService.autoCompleteKeywords.map { keywords: Seq[String] =>
        Results.Ok(html.advanced_search(SearchForm.form, None, keywords, focus))
      }
    } else {
      keywordsService.autoCompleteKeywords.flatMap(keywords => {
        SearchForm.form.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(Results.Ok(html.advanced_search(formWithErrors, None, keywords, focus)))
          },
          data => for {
            cases: Paged[Case] <- casesService.search(search, sort, SearchPagination(page))
            attachments: Map[Case, Seq[StoredAttachment]] <- fileStoreService.getAttachments(cases.results)
            results: Paged[SearchResult] = cases.map(c => SearchResult(c, attachments.getOrElse(c, Seq.empty)))
          } yield Results.Ok(html.advanced_search(SearchForm.form.fill(data), Some(results), keywords, focus))
                            .addingToSession((backToSearchResultsLinkLabel, "search results"), (backToSearchResultsLinkUrl,
                              s"${SearchController.search(selectedTab,Some(false), None, search, sort, page).url}#advanced_search_keywords"))
        )
      })
    }
  }

}
