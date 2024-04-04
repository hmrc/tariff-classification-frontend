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

package utils

import models.{Event, NoPagination, Paged}
import play.api.libs.json.{Json, Writes}
import utils.JsonFormatters.{eventFormat, newEventRequestFormat}

object EventPayloads {

  val event: String             = jsonOf(Events.event)
  val eventRequest: String      = jsonOf(Events.eventRequest)
  val events: String            = jsonOf(Events.events)
  val sampleEvents: String      = jsonOf(Events.sampleEvents)
  val pagedEvents: String       = jsonOf(Paged(Events.events, NoPagination(), 1))
  val pagedSampleEvents: String = jsonOf(Paged(Events.sampleEvents, NoPagination(), 1))
  val pagedEmpty: String        = jsonOf(Paged.empty[Event])
  val completionEvents: String  = jsonOf(Events.pagedCompletedEvents)
  val referralEvents: String    = jsonOf(Events.pagedReferredEvents)

  def emptyMap[A: Writes]: String = jsonOf(Map.empty[String, A])

  def jsonOf[A: Writes](obj: A): String =
    Json.toJson(obj).toString()
}
