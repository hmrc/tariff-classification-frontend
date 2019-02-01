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

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditService
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future
import scala.concurrent.Future.successful

class KeywordsServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val connector = mock[BindingTariffClassificationConnector]
  private val config = mock[AppConfig]

  private val service = new KeywordsService(connector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, config)
  }

  "Add keyword" should {

    "New keywords are converted to uppercase and added to the case" in {
      val aKeyword = "Apples"
      val aCase = Cases.btiCaseExample
      val aCaseWithNewKeyword = aCase.copy(keywords = aCase.keywords + "APPLES")
      given(connector.updateCase(refEq(aCaseWithNewKeyword))(any[HeaderCarrier])).willReturn(successful(aCaseWithNewKeyword))

      await(service.addKeyword(aCase, aKeyword)) shouldBe aCaseWithNewKeyword
    }

    "New keywords that are duplicates are not added" in {
      val aKeyword = "Apples"
      val aCase = Cases.btiCaseExample
      val aCaseWithExistingKeyword = aCase.copy(keywords = aCase.keywords + "APPLES")

      await(service.addKeyword(aCaseWithExistingKeyword, aKeyword)) shouldBe aCaseWithExistingKeyword
    }
  }

  "Remove keyword" should {

    "remove keyword from the set" in {
      val aKeyword = "Apples"
      val aCase = Cases.btiCaseExample
      val aCaseWithKeyword = aCase.copy(keywords = aCase.keywords + "APPLES")
      given(connector.updateCase(refEq(aCase))(any[HeaderCarrier])).willReturn(successful(aCase))

      await(service.removeKeyword(aCaseWithKeyword, aKeyword)) shouldBe aCase
    }

    "trying to remove keyword that is not in the set leaves the set unchanged" in {
      val aKeyword = "Oranges"
      val aCase = Cases.btiCaseExample
      val aCaseWithKeyword = aCase.copy(keywords = aCase.keywords + "APPLES")
      given(connector.updateCase(refEq(aCaseWithKeyword))(any[HeaderCarrier])).willReturn(successful(aCaseWithKeyword))

      await(service.removeKeyword(aCaseWithKeyword, aKeyword)) shouldBe aCaseWithKeyword
    }
  }

  "Retrieve auto complete keywords" should {

    "return a list of keywords" in {
      await(service.autoCompleteKeywords) should contain ("ABS")
    }
  }

}
