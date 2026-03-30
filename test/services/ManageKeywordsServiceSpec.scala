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
import models.*
import models.ApplicationType.ATAR
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import java.time.Instant
import scala.concurrent.Future

class ManageKeywordsServiceSpec extends ServiceSpecBase with BeforeAndAfterEach {
  private val connector             = mock[BindingTariffClassificationConnector]
  private val auditService          = mock[AuditService]
  private val manageKeywordsService = new ManageKeywordsService(auditService, connector)
  private val keyWord               = Keyword(name = "keyword")

  // CHANGED: Use CaseKeywordRow instead of CaseKeyword
  private val caseKeywordRow = CaseKeywordRow(
    keyword = "keyword",
    reference = "sdcd",
    user = None,
    goods = None,
    caseType = "BTI",
    status = "NEW",
    liabilityStatus = None,
    daysElapsed = 3L
  )

  private val user = Operator("operator")

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
  }

  "createKeyword" should {
    "return a new keyword" in {
      when(connector.createKeyword(keyWord)).thenReturn(Future.successful(keyWord))
      await(manageKeywordsService.createKeyword(keyWord, user, ChangeKeywordStatusAction.CREATED)) shouldBe keyWord
    }
  }

  "fetchAll" should {
    "return all keywords" in {
      val pagination = NoPagination()
      when(connector.findAllKeywords(pagination)).thenReturn(Future.successful(Paged(Seq(keyWord))))
      await(manageKeywordsService.findAll(pagination)) shouldBe Paged(Seq(keyWord))
    }
  }

  "fetchCaseKeywords" should {
    "return all case keyword rows" in {
      // CHANGED: Updated mock to match new signature
      when(connector.getCaseKeywords(any[Pagination])(any))
        .thenReturn(Future.successful(Paged(Seq(caseKeywordRow))))

      await(manageKeywordsService.fetchCaseKeywords()) shouldBe Paged(Seq(caseKeywordRow))
    }
  }

  /*  "deleteKeyword" should {
    "return all keywords" in {
      when(connector.deleteKeyword(keyWord)).thenReturn(Future.successful(Seq(caseKeywordRow)))
      when(auditService.auditManagerKeywordDeleted(Operator("sd"), keyWord)).thenReturn(Unit)
     // doNothing().doThrow(new RuntimeException()).when(auditService.auditManagerKeywordDeleted(Operator("sd"), keyWord))
      await(manageKeywordsService.deleteKeyword(keyWord, Operator("sd"))).isCompleted
    }
  }
  "renameKeyword" should {
    "return a new keyword" in {
      val newKeyWord = Keyword("new keyword")
      when(connector.deleteKeyword(keyWord)).thenReturn(Future.successful(Seq(caseKeywordRow)))
      when(connector.createKeyword(keyWord)).thenReturn(Future.successful(keyWord))
      when(connector.createKeyword(newKeyWord)).thenReturn(Future.successful(newKeyWord))
      await(manageKeywordsService.renameKeyword(keyWord,newKeyWord, user)) shouldBe newKeyWord
    }
  }
   */
}
