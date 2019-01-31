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

import scala.concurrent.Future

class CasesServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val manyCases = mock[Seq[Case]]
  private val oneCase = mock[Option[Case]]
  private val emailService = mock[EmailService]
  private val fileStoreService = mock[FileStoreService]
  private val queue = mock[Queue]
  private val connector = mock[BindingTariffClassificationConnector]
  private val audit = mock[AuditService]
  private val config = mock[AppConfig]

  private val service = new CasesService(config, audit, emailService, fileStoreService, connector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit, queue, oneCase, manyCases, config)
  }

  "Get Cases 'By Queue'" should {
    "retrieve connector cases" in {
      given(connector.findCasesByQueue(queue)) willReturn Future.successful(manyCases)

      await(service.getCasesByQueue(queue)) shouldBe manyCases
    }
  }

  "Get Cases 'By Assignee'" should {
    "retrieve connector cases" in {
      given(connector.findCasesByAssignee(Operator("assignee"))) willReturn Future.successful(manyCases)

      await(service.getCasesByAssignee(Operator("assignee"))) shouldBe manyCases
    }
  }

  "Get One Case 'By Reference'" should {
    "retrieve connector case" in {
      given(connector.findCase("reference")) willReturn Future.successful(oneCase)

      await(service.getOne("reference")) shouldBe oneCase
    }
  }

  "Search Cases" should {
    "retrieve connector cases" in {
      given(connector.search(any[Search], any[Sort])(any[HeaderCarrier], any[QueryStringBindable[Search]], any[QueryStringBindable[Sort]])) willReturn Future.successful(manyCases)

      await(service.search(Search(), Sort())) shouldBe manyCases
    }
  }

  "Update Case" should {
    val oldCase = mock[Case]
    val updatedCase = mock[Case]

    "delegate to connector" in {
      given(connector.updateCase(refEq(oldCase))(any[HeaderCarrier])) willReturn Future.successful(updatedCase)

      await(service.updateCase(oldCase)) shouldBe updatedCase
    }
  }

  "Add attachment into case" should {
    val c = mock[Case]
    val updatedCase = mock[Case]
    val fileUpload = mock[FileUpload]
    val fileStored = mock[FileStoreAttachment]

    "add the given attachment into the case provided" in {

      given(c.attachments) willReturn Seq.empty
      given(fileStored.id) willReturn "file-id"
      given(fileStoreService.upload(refEq(fileUpload))(any[HeaderCarrier])) willReturn Future.successful(fileStored)
      given(connector.updateCase(any[Case])(any[HeaderCarrier])) willReturn  Future.successful(updatedCase)

      val result = await(service.addAttachment(c, fileUpload, Operator("assignee")))

      result shouldBe updatedCase
    }
  }

}
