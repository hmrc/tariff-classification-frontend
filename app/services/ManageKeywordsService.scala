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

import audit.AuditService
import connectors.BindingTariffClassificationConnector
import models.ChangeKeywordStatusAction.ChangeKeywordStatusAction
import models._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ManageKeywordsService @Inject() (auditService: AuditService, connector: BindingTariffClassificationConnector)(
  implicit ec: ExecutionContext
) {

  def createKeyword(keyword: Keyword, user: Operator, keywordStatusAction: ChangeKeywordStatusAction)(implicit
    hc: HeaderCarrier
  ): Future[Keyword] =
    for {
      keywordCreated <- connector.createKeyword(keyword)
      _ = auditService.auditManagerKeywordCreated(user, keywordCreated, keywordStatusAction)
    } yield keywordCreated

  def findAll(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Keyword]] =
    connector.findAllKeywords(pagination)

  def fetchCaseKeywords()(implicit hc: HeaderCarrier): Future[ManageKeywordsData] =
    connector.getCaseKeywords()

  def deleteKeyword(keyword: Keyword, user: Operator)(implicit hc: HeaderCarrier): Future[Unit] =
    connector.deleteKeyword(keyword).map(_ => auditService.auditManagerKeywordDeleted(user, keyword))

  def renameKeyword(keywordToDelete: Keyword, keywordToAdd: Keyword, user: Operator)(implicit
    hc: HeaderCarrier
  ): Future[Keyword] =
    for {
      keywordRenamed <- connector
                          .deleteKeyword(keywordToDelete)
                          .flatMap(_ => connector.createKeyword(keywordToDelete.copy(approved = false)))
                          .flatMap(_ => connector.createKeyword(keywordToAdd))
      _ = auditService.auditManagerKeywordRenamed(user, keywordToDelete, keywordRenamed)
    } yield keywordRenamed
}
