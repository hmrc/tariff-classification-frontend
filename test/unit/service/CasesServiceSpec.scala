/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.Clock

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import audit.AuditService
import config.AppConfig
import connector.{BindingTariffClassificationConnector, RulingConnector}
import models.ApplicationType.ApplicationType
import models._

import scala.concurrent.Future.successful

class CasesServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val c = mock[Case]
  private val manyCases = Seq(c)
  private val oneCase = Some(c)
  private val emailService = mock[EmailService]
  private val fileStoreService = mock[FileStoreService]
  private val reportingService = mock[ReportingService]
  private val queue = mock[Queue]
  private val pagination = mock[Pagination]
  private val connector = mock[BindingTariffClassificationConnector]
  private val rulingConnector = mock[RulingConnector]
  private val audit = mock[AuditService]
  private val config = mock[AppConfig]

  private val service = new CasesService(config, audit, emailService, fileStoreService, reportingService, connector, rulingConnector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit, queue, c, config)
  }

  "Get Cases 'By Queue'" should {
    "retrieve connector cases" in {
      given(connector.findCasesByQueue(any[Queue], any[Pagination], any[Seq[ApplicationType]])(any[HeaderCarrier])) willReturn successful(Paged(manyCases))

      await(service.getCasesByQueue(queue, pagination)) shouldBe Paged(manyCases)
    }

    "retrieve connector cases with type restriction" in {
      given(connector.findCasesByQueue(any[Queue], any[Pagination], refEq(Seq(ApplicationType.LIABILITY_ORDER)))(any[HeaderCarrier])) willReturn successful(Paged(manyCases))

      await(service.getCasesByQueue(queue, pagination,Seq(ApplicationType.LIABILITY_ORDER))) shouldBe Paged(manyCases)
    }

  }

  "Get Cases 'By Assignee'" should {
    "retrieve connector cases" in {
      given(connector.findCasesByAssignee(refEq(Operator("assignee")), refEq(pagination))(any[HeaderCarrier])) willReturn successful(Paged(manyCases))

      await(service.getCasesByAssignee(Operator("assignee"), pagination)) shouldBe Paged(manyCases)
    }
  }

  "Get One Case 'By Reference'" should {
    "retrieve connector case" in {
      given(connector.findCase("reference")) willReturn successful(oneCase)

      await(service.getOne("reference")) shouldBe oneCase
    }
  }

  "Search Cases" should {
    "retrieve connector cases" in {
      given(connector.search(any[Search], any[Sort], any[Pagination])(any[HeaderCarrier], any[Clock], any[QueryStringBindable[String]])) willReturn successful(Paged(manyCases))

      await(service.search(Search(), Sort(), pagination)) shouldBe Paged(manyCases)
    }
  }

  "Update Case" should {
    val oldCase = mock[Case]
    val updatedCase = mock[Case]

    "delegate to connector" in {
      given(connector.updateCase(refEq(oldCase))(any[HeaderCarrier])) willReturn successful(updatedCase)

      await(service.updateCase(oldCase)) shouldBe updatedCase
    }
  }

  "Create Case" should {
    val application = mock[Application]
    val createdCase = mock[Case]

    "delegate to connector" in {
      given(connector.createCase(refEq(application))(any[HeaderCarrier])) willReturn successful(createdCase)

      await(service.createCase(application)) shouldBe createdCase
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
      given(fileStoreService.upload(refEq(fileUpload))(any[HeaderCarrier])) willReturn successful(fileStored)
      given(connector.updateCase(any[Case])(any[HeaderCarrier])) willReturn  successful(updatedCase)

      val result = await(service.addAttachment(c, fileUpload, Operator("assignee")))

      result shouldBe updatedCase
    }
  }

  "Remove attachment from case" should {
    val oldCase = mock[Case]
    val updatedCase = mock[Case]
    val attachment = mock[Attachment]

    "remove the given attachment from the case provided" in {
      given(oldCase.attachments) willReturn Seq(attachment)
      given(fileStoreService.removeAttachment(refEq("file-id"))(any[HeaderCarrier])) willReturn successful(())
      given(connector.updateCase(any[Case])(any[HeaderCarrier])) willReturn successful(updatedCase)

      val result = await(service.removeAttachment(oldCase, "file-id"))

      result shouldBe updatedCase
    }
  }

}
