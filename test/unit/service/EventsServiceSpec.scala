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

import java.time._

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.{reset, verify, verifyNoMoreInteractions, verifyZeroInteractions}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import audit.AuditService
import connector.BindingTariffClassificationConnector
import models._
import models.request.NewEventRequest
import views.html.partials.sample.sample_details
import utils.Cases

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class EventsServiceSpec extends ServiceSpecBase with BeforeAndAfterEach {

  private val connector = mock[BindingTariffClassificationConnector]
  private val auditService = mock[AuditService]
  private val event = mock[Event]
  private val manyEvents = Seq(event)

  private val service = new EventsService(connector, auditService)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, event)
  }

  "Get Events by reference" should {
    "retrieve a list of events" in {
      given(connector.findFilteredEvents("reference", NoPagination(), Set.empty)) willReturn Future.successful(Paged(manyEvents))

      await(service.getEvents("reference", NoPagination())) shouldBe Paged(manyEvents)
    }
  }

  "Get Filtered Events by reference" should {
    "retrieve a list of events" in {

      val filteredEvents : Seq[Event] = Seq(Event("1",SampleStatusChange(Some(SampleStatus.AWAITING), Some(SampleStatus.DESTROYED), None),Operator("1"),"1"))

      // When
      given(connector.findFilteredEvents("reference", NoPagination(),
        Set(EventType.SAMPLE_STATUS_CHANGE))) willReturn Future.successful(Paged(filteredEvents,NoPagination(),1))

      await(service.getFilteredEvents("reference", NoPagination(),Some(Set(EventType.SAMPLE_STATUS_CHANGE)))) shouldBe Paged(filteredEvents,NoPagination(),1)
    }
  }

  "Add Note" should {
    val aNote = "This is a note"
    val clock = Clock.fixed(LocalDateTime.of(2018,1,1, 14,0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
    val operator = Operator("userId", Some("Billy Bobbins"))
    val newEventRequest = NewEventRequest(Note(aNote), operator, Instant.now(clock))
    val event = mock[Event]
    val aCase = Cases.btiCaseExample

    "post a new note to the backend via the connector" in {
      given(connector.createEvent(refEq(aCase), refEq(newEventRequest))(any[HeaderCarrier]))
        .willReturn(successful(event))

      await(service.addNote(aCase, aNote, operator, clock)) shouldBe event

      verify(auditService).auditNote(refEq(aCase), refEq(aNote), refEq(operator))(any[HeaderCarrier])
      verifyNoMoreInteractions(auditService)
    }

    "propagate the error if the connector fails" in {
      given(connector.createEvent(refEq(aCase), refEq(newEventRequest))(any[HeaderCarrier]))
        .willReturn(failed(new IllegalStateException))

      intercept[IllegalStateException] {
        await(service.addNote(aCase, aNote, operator, clock))
      }

      verifyZeroInteractions(auditService)
    }
  }

}
