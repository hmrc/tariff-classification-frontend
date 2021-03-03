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
import uk.gov.hmrc.http.HeaderCarrier
import audit.AuditService
import connector.BindingTariffClassificationConnector
import models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class KeywordsService @Inject() (connector: BindingTariffClassificationConnector, auditService: AuditService) {

  def addKeyword(c: Case, keyword: String, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] =
    if (c.keywords.contains(keyword.toUpperCase)) {
      successful(c)
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
      successful(c)
    }

  def findAll(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Keyword]] =
    connector.findAllKeywords(pagination)

}
