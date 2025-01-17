/*
 * Copyright 2025 HM Revenue & Customs
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

package services

import org.apache.pekko.stream.Materializer
import audit.AuditService
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import config.AppConfig
import connectors.BindingTariffClassificationConnector
import models._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KeywordsService @Inject() (
  config: AppConfig,
  connector: BindingTariffClassificationConnector,
  auditService: AuditService
)(implicit mat: Materializer) {

  implicit val ec: ExecutionContext = mat.executionContext

  private val KeywordsCacheKey = "allKeywords"

  private val keywordsCache: Cache[String, Seq[Keyword]] =
    Scaffeine()
      .executor(mat.executionContext)
      .expireAfterWrite(config.keywordsCacheExpiration)
      .maximumSize(1)
      .build[String, Seq[Keyword]]()

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

  def findAll()(implicit hc: HeaderCarrier): Future[Seq[Keyword]] =
    keywordsCache.getIfPresent(KeywordsCacheKey) match {
      case Some(value) => Future(value)
      case None =>
        connector
          .findAllKeywords(NoPagination())(hc)
          .map(_.results.filter(_.approved))
          .map { value =>
            keywordsCache.put(KeywordsCacheKey, value)
            value
          }
    }
}
