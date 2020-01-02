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

package uk.gov.tariffclassificationfrontend.utils

import play.api.libs.json.Json
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{Event, NoPagination, Paged}
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.{eventFormat, newEventRequestFormat}

object EventPayloads {

  val event: String = jsonOf(Events.event)
  val eventRequest: String = jsonOf(Events.eventRequest)
  val events: String = jsonOf(Events.events)
  val pagedEvents: String = jsonOf(Paged(Events.events, NoPagination(), 1))
  val pagedEmpty: String = jsonOf(Paged.empty[Event])

  def jsonOf(obj: Event): String = {
    Json.toJson(obj).toString()
  }

  def jsonOf(obj: NewEventRequest): String = {
    Json.toJson(obj).toString()
  }

  def jsonOf(obj: Seq[Event]): String = {
    Json.toJson(obj).toString()
  }

  def jsonOf(obj: Paged[Event]): String = {
    Json.toJson(obj).toString()
  }

}
