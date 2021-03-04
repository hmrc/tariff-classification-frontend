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

package service

import connector.BindingTariffClassificationConnector
import models._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ManageKeywordsService @Inject()(connector: BindingTariffClassificationConnector) {

  def createKeyword(keyword: Keyword)(implicit hc: HeaderCarrier): Future[Keyword] =
      connector.createKeyword(keyword)

  def findAll(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Keyword]] =
      connector.findAllKeywords(pagination)

  def fetchCaseKeywords()(implicit hc: HeaderCarrier): Future[Paged[CaseKeyword]] =
    connector.getCaseKeywords()

  def updateKeywordStatus(keyword: Keyword, keywordStatusChange: KeywordStatusChange)(implicit hc: HeaderCarrier): Future[Keyword] = {
    keywordStatusChange.action.toUpperCase match {
      case "APPROVED" => connector.updateKeyword(keyword.copy(approved = true, rejected = false))
      case "REJECTED" => connector.updateKeyword(keyword.copy(approved = false, rejected = true))
      case "REPLACED" => connector.updateKeyword(keyword.copy(name = keywordStatusChange.newKeywordName.get))
    }
  }
}
