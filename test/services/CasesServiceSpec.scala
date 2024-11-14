/*
 * Copyright 2024 HM Revenue & Customs
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

import models.CaseStatus.CaseStatus
import models._
import models.request.NewEventRequest
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito.{never, reset, verify, verifyNoMoreInteractions}
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import java.time.Instant
import scala.concurrent.Future.{failed, successful}

class CasesServiceSpec extends CasesServiceSpecBase with BeforeAndAfterEach {

  private val c          = mock[Case]
  private val manyCases  = Seq(c)
  private val oneCase    = Some(c)
  private val pagination = mock[Pagination]
  private val operator   = Operator("operator-id")

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector)
    reset(audit)
    reset(queue)
    reset(c)
  }

  "Get Cases 'By Queue'" should {
    "retrieve connector cases" in {
      given(
        connector.findCasesByQueue(any[Queue], any[Pagination], any[Set[ApplicationType]])(any[HeaderCarrier])
      ) willReturn successful(
        Paged(manyCases)
      )

      await(service.getCasesByQueue(queue, pagination)) shouldBe Paged(manyCases)
    }

    "retrieve connector cases with type restriction" in {
      given(
        connector.findCasesByQueue(any[Queue], any[Pagination], refEq(Set(ApplicationType.LIABILITY)))(
          any[HeaderCarrier]
        )
      ) willReturn successful(Paged(manyCases))

      await(service.getCasesByQueue(queue, pagination, Set(ApplicationType.LIABILITY))) shouldBe Paged(manyCases)
    }

  }

  "Get Cases 'By All Queues'" should {
    "retrieve connector cases" in {
      given(
        connector.findCasesByAllQueues(
          any[Seq[Queue]],
          any[Pagination],
          any[Set[ApplicationType]],
          any[Set[CaseStatus]],
          any[String]
        )(any[HeaderCarrier])
      ) willReturn successful(
        Paged(manyCases)
      )

      await(service.getCasesByAllQueues(Seq(queue), pagination, assignee = "none")) shouldBe Paged(manyCases)
    }
  }

  "Get Cases 'By Assignee'" should {
    "retrieve connector cases" in {
      given(
        connector.findCasesByAssignee(refEq(Operator("assignee")), refEq(pagination))(any[HeaderCarrier])
      ) willReturn successful(
        Paged(manyCases)
      )

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
      given(
        connector.search(any[Search], any[Sort], any[Pagination])(any[HeaderCarrier], any[QueryStringBindable[String]])
      ) willReturn successful(Paged(manyCases))

      await(service.search(Search(), Sort(), pagination)) shouldBe Paged(manyCases)
    }
  }

  "Update Case" should {
    val oldCase     = mock[Case]
    val updatedCase = mock[Case]

    "delegate to connector" in {
      given(connector.updateCase(refEq(updatedCase))(any[HeaderCarrier])) willReturn successful(updatedCase)

      await(service.updateCase(oldCase, updatedCase, operator)) shouldBe updatedCase
    }

    "Update Case with Auditing" should {
      val oldCase     = mock[Case]
      val updatedCase = mock[Case]

      "delegate to connector" in {
        given(connector.updateCase(refEq(updatedCase))(any[HeaderCarrier])) willReturn successful(updatedCase)

        await(service.updateCase(oldCase, updatedCase, operator)) shouldBe updatedCase
      }
    }
  }

  "Create Case" should {
    val aLiabilityCase = Cases.newLiabilityLiveCaseExample
    val operator       = Operator("id")

    "delegate to connector - add a case created event" in {
      given(connector.createCase(refEq(aLiabilityCase.application))(any[HeaderCarrier])) willReturn successful(
        aLiabilityCase
      )
      given(connector.createEvent(refEq(aLiabilityCase), any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(successful(mock[Event]))

      await(service.createCase(aLiabilityCase.application, operator)) shouldBe aLiabilityCase

      verify(audit).auditCaseCreated(refEq(aLiabilityCase), refEq(operator))(any[HeaderCarrier])

      val eventCreated = theEventCreatedFor(connector, aLiabilityCase)

      eventCreated.operator shouldBe Operator("id")
      eventCreated.details  shouldBe CaseCreated(comment = "Liability case created")

    }

    "not succeed and not create event on create case failure" in {
      given(connector.createCase(any[Application])(any[HeaderCarrier])).willReturn(failed(new RuntimeException()))

      intercept[RuntimeException] {
        await(service.createCase(any[Application], operator))
      }

      verify(connector, never()).createEvent(refEq(aLiabilityCase), any[NewEventRequest])(any[HeaderCarrier])

      verifyNoMoreInteractions(audit)
    }

    "succeed but not create event on create event failure" in {

      given(connector.createCase(any[Application])(any[HeaderCarrier])) willReturn successful(aLiabilityCase)

      given(connector.createEvent(any[Case], any[NewEventRequest])(any[HeaderCarrier]))
        .willReturn(failed(new RuntimeException()))

      await(service.createCase(aLiabilityCase.application, operator)) shouldBe aLiabilityCase

      verify(audit).auditCaseCreated(refEq(aLiabilityCase), refEq(operator))(any[HeaderCarrier])
    }
  }

  "Add attachment into case" should {
    val c           = mock[Case]
    val updatedCase = mock[Case]

    "add the given attachment into the case provided" in {

      given(c.attachments) willReturn Seq.empty
      given(connector.updateCase(any[Case])(any[HeaderCarrier])) willReturn successful(updatedCase)

      val result = await(service.addAttachment(c, "file-id", Operator("assignee")))

      result shouldBe updatedCase
    }
  }

  "Remove attachment from case" should {
    val oldCase     = mock[Case]
    val updatedCase = mock[Case]
    val attachment  = mock[Attachment]

    "remove the given attachment from the case provided" in {
      given(oldCase.attachments) willReturn Seq(attachment)
      given(fileStoreService.removeAttachment(refEq("file-id"))(any[HeaderCarrier])) willReturn successful(())
      given(connector.updateCase(any[Case])(any[HeaderCarrier])) willReturn successful(updatedCase)

      val result = await(service.removeAttachment(oldCase, "file-id"))

      result shouldBe updatedCase
    }
  }

  "Add message into case when case is Correspondence" should {
    val c              = Cases.aCorrespondenceCase()
    val updatedCase    = Cases.aCorrespondenceCase()
    val exampleMessage = Message("name", Instant.now(), "message")

    "add the given message into the case provided" in {

      given(connector.updateCase(any[Case])(any[HeaderCarrier])) willReturn successful(updatedCase)

      val result = await(service.addMessage(c, exampleMessage, Operator("assignee")))

      result shouldBe updatedCase
    }
  }

  "Add message into case when case is Miscellaneous" should {
    val c              = Cases.aMiscellaneousCase()
    val updatedCase    = Cases.aMiscellaneousCase()
    val exampleMessage = Message("name", Instant.now(), "message")

    "add the given message into the case provided" in {

      given(connector.updateCase(any[Case])(any[HeaderCarrier])) willReturn successful(updatedCase)

      val result = await(service.addMessage(c, exampleMessage, Operator("assignee")))

      result shouldBe updatedCase
    }
  }

}
