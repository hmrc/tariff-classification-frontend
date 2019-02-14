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

package uk.gov.hmrc.tariffclassificationfrontend.service

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditService
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

@Singleton
class KeywordsService @Inject()(connector: BindingTariffClassificationConnector, auditService: AuditService) {

  def addKeyword(c: Case, keyword: String, operator: Operator)
                (implicit hc: HeaderCarrier): Future[Case] = {
    if (keywords.contains(keyword.toUpperCase)) {
      Future.successful(c)
    } else {
      val caseToUpdate = c.copy(keywords = c.keywords + keyword.toUpperCase)
      connector.updateCase(caseToUpdate) map { updated: Case =>
        auditService.auditCaseKeywordAdded(updated, keyword, operator)
        updated
      }
    }
  }

  def removeKeyword(c: Case, keyword: String, operator: Operator)
                   (implicit hc: HeaderCarrier): Future[Case] = {
    if (c.keywords.contains(keyword.toUpperCase)) {
      val caseToUpdate = c.copy(keywords = c.keywords - keyword.toUpperCase)
      connector.updateCase(caseToUpdate) map { updated: Case =>
        auditService.auditCaseKeywordRemoved(updated, keyword, operator)
        updated
      }
    } else {
      Future.successful(c)
    }
  }

  def autoCompleteKeywords: Future[Seq[String]] = {
    Future {
      val url = getClass.getClassLoader.getResource("keywords.txt")
      (for (line <- Source.fromURL(url, "UTF-8").getLines()) yield line).toSeq
    }
  }

}
