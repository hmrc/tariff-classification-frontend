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

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import audit.AuditService
import com.github.blemale.scaffeine.Scaffeine
import connector.BindingTariffClassificationConnector
import models._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import config.AppConfig

@Singleton
class KeywordsService @Inject() (
  config: AppConfig,
  connector: BindingTariffClassificationConnector,
  auditService: AuditService
)(
  implicit mat: Materializer
) {

  implicit val ec: ExecutionContext = mat.executionContext

  val cacheHeaderCarrier = HeaderCarrier().withExtraHeaders("X-Api-Token" -> config.apiToken)

  val KeywordsCacheKey = "allKeywords"

  val keywordsCache = Scaffeine()
    .executor(mat.executionContext)
    .expireAfterWrite(config.keywordsCacheExpiration)
    .maximumSize(1)
    .buildAsyncFuture[String, Paged[Keyword]](_ => connector.findAllKeywords(NoPagination())(cacheHeaderCarrier))

  def addKeyword(c: Case, keyword: String, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] =
    if (c.keywords.contains(keyword.toUpperCase)) {
      Future.successful(c)
    } else {
      val caseToUpdate = c.copy(keywords = c.keywords + keyword.toUpperCase)
      connector.updateCase(caseToUpdate) map { updated: Case =>
        auditService.auditCaseKeywordAdded(updated, keyword, operator)
        updated
      }
    }

  def removeKeyword(c: Case, keyword: String, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] =
    if (c.keywords.contains(keyword.toUpperCase)) {
      val caseToUpdate = c.copy(keywords = c.keywords - keyword.toUpperCase)
      connector.updateCase(caseToUpdate) map { updated: Case =>
        auditService.auditCaseKeywordRemoved(updated, keyword, operator)
        updated
      }
    } else {
      Future.successful(c)
    }

  def findAll: Future[Paged[Keyword]] =
    keywordsCache.get(KeywordsCacheKey)
}
