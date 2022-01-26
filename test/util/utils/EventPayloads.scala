/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.Json
import models.{Event, NoPagination, Paged}
import utils.JsonFormatters.{eventFormat, newEventRequestFormat}
import play.api.libs.json.Writes

object EventPayloads {

  val event: String        = jsonOf(Events.event)
  val eventRequest: String = jsonOf(Events.eventRequest)
  val events: String       = jsonOf(Events.events)
  val sampleEvents         = jsonOf(Events.sampleEvents)
  val pagedEvents: String  = jsonOf(Paged(Events.events, NoPagination(), 1))
  val pagedSampleEvents    = jsonOf(Paged(Events.sampleEvents, NoPagination(), 1))
  val pagedEmpty: String   = jsonOf(Paged.empty[Event])
  val completionEvents     = jsonOf(Events.pagedCompletedEvents)
  val referralEvents       = jsonOf(Events.pagedReferredEvents)

  def emptyMap[A: Writes] = jsonOf(Map.empty[String, A])

  def jsonOf[A: Writes](obj: A): String =
    Json.toJson(obj).toString()
}
