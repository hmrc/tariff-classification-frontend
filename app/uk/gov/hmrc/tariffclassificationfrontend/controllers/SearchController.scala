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
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.SearchForm
import uk.gov.hmrc.tariffclassificationfrontend.models.{Paged, _}
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService, KeywordsService}
import uk.gov.hmrc.tariffclassificationfrontend.views.html
import uk.gov.hmrc.tariffclassificationfrontend.views.partials.SearchResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

@Singleton
class SearchController @Inject()(authenticatedAction: AuthenticatedAction,
                                 casesService: CasesService,
                                 keywordsService: KeywordsService,
                                 fileStoreService: FileStoreService,
                                 val messagesApi: MessagesApi,
                                 implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def search(reference: Option[String] = None,
             search: Search = Search(),
             sort: Sort = Sort(),
             page: Int): Action[AnyContent] = authenticatedAction.async { implicit request =>

    if (reference.isDefined) {
      successful(Redirect(routes.CaseController.trader(reference.get)))
    } else if (search.isEmpty) {
      keywordsService.autoCompleteKeywords.map { keywords: Seq[String] =>
        show(SearchForm.form, None, keywords)
      }
    } else {
      keywordsService.autoCompleteKeywords.flatMap { keywords: Seq[String] =>
        SearchForm.form.bindFromRequest.fold(
          formWithErrors => successful(show(formWithErrors, None, keywords)),
          data => for {
            cases: Paged[Case] <- casesService.search(search, sort, SearchPagination(page))
            attachments: Map[Case, Seq[StoredAttachment]] <- fileStoreService.getAttachments(cases.results)
            results: Paged[SearchResult] = cases.map(c => SearchResult(c, attachments.getOrElse(c, Seq.empty)))
          } yield show(SearchForm.form.fill(data), Some(results), keywords)
        )
      }
    }

  }

  private def show(f: Form[Search], results: Option[Paged[SearchResult]], keywords: Seq[String])
                  (implicit request: AuthenticatedRequest[AnyContent]): Result = {
    Results.Ok(html.advanced_search(SearchForm.form, results, keywords))
  }

}
