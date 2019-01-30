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

import java.time._

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future
import scala.concurrent.Future.{failed, successful}

class EventsServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val connector = mock[BindingTariffClassificationConnector]
  private val manyEvents = mock[Seq[Event]]

  private val service = new EventsService(connector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, manyEvents)
  }

  "Get Events by reference" should {
    "retrieve a list of events" in {
      given(connector.findEvents("reference")) willReturn Future.successful(manyEvents)

      await(service.getEvents("reference")) shouldBe manyEvents
    }
  }

  "Add Note" should {
    val operator = mock[Operator]

    val aNote = "This is a note"

    "post a new note to the backend via the connector" in {
      val clock = Clock.fixed(LocalDateTime.of(2018,1,1, 14,0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"))
      val operator = Operator("userId", Some("Billy Bobbins"))
      val newEventRequest = NewEventRequest(Note(Some(aNote)), operator, ZonedDateTime.now(clock))
      val event = mock[Event]
      val aCase = Cases.btiCaseExample
      given(connector.createEvent(refEq(aCase), refEq(newEventRequest))(any[HeaderCarrier]))
        .willReturn(successful(event))

      await(service.addNote(aCase, aNote, operator, clock)) shouldBe event
    }

  }

}
