package services

import audit.AuditService
import connectors.BindingTariffClassificationConnector
import models.*
import models.ApplicationType.ATAR
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach

import scala.Unit.unbox
import scala.concurrent.Future
import scala.concurrent.Future.successful

class ManageKeywordsServiceSpec extends ServiceSpecBase with BeforeAndAfterEach {
  private val connector = mock[BindingTariffClassificationConnector]
  private val auditService = mock[AuditService]
  private val manageKeywordsService = new ManageKeywordsService(auditService, connector)
  private val keyWord = Keyword(name = "keyword")
  private val caseKeyWord = CaseKeyword(keyword = keyWord, cases = List(CaseHeader(reference = "sdcd", assignee = None, team = None, goodsName = None, caseType = ATAR, status = CaseStatus.NEW, daysElapsed = 3, liabilityStatus = None)))
  private val user = Operator("operator")
  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
  }

  "createKeyword" should {
    "return a new password" in {
      when(connector.createKeyword(keyWord)).thenReturn(Future.successful(keyWord))
      await(manageKeywordsService.createKeyword(keyWord, user, ChangeKeywordStatusAction.CREATED)) shouldBe keyWord
    }
  }
  "fetchAll" should {
    "return all passwords" in {
      val pagination = NoPagination()
      when(connector.findAllKeywords(pagination)).thenReturn(Future.successful(Seq(keyWord)))
      await(manageKeywordsService.findAll(pagination)) shouldBe Seq(keyWord)
    }
  }
  "fetchCaseKeywords" should {
    "return all passwords" in {
      when(connector.getCaseKeywords()).thenReturn(Future.successful(Seq(caseKeyWord)))
      await(manageKeywordsService.fetchCaseKeywords()) shouldBe Seq(caseKeyWord)
    }
  }
  "deleteKeyword" should {
    "return all passwords" in {
      when(connector.deleteKeyword(keyWord)).thenReturn(Future.successful(Seq(caseKeyWord)))
      when(auditService.auditManagerKeywordDeleted(Operator("sd"), keyWord)).thenReturn(Unit)
     // doNothing().doThrow(new RuntimeException()).when(auditService.auditManagerKeywordDeleted(Operator("sd"), keyWord))
      await(manageKeywordsService.deleteKeyword(keyWord, Operator("sd"))).isCompleted
    }
  }
  "renameKeyword" should {
    "return a new password" in {
      val newKeyWord = Keyword("new keyword")
      when(connector.deleteKeyword(keyWord)).thenReturn(Future.successful(Seq(caseKeyWord)))
      when(connector.createKeyword(keyWord)).thenReturn(Future.successful(keyWord))
      when(connector.createKeyword(newKeyWord)).thenReturn(Future.successful(newKeyWord))
      await(manageKeywordsService.renameKeyword(keyWord,newKeyWord, user)) shouldBe newKeyWord
    }
  }

}
