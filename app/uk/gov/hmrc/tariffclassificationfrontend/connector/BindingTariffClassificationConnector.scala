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

package uk.gov.hmrc.tariffclassificationfrontend.connector

import java.time.{Clock, Instant}

import com.google.inject.Inject
import javax.inject.Singleton
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.{caseFormat, eventFormat, newEventRequestFormat}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class BindingTariffClassificationConnector @Inject()(configuration: AppConfig, client: HttpClient) {

  private lazy val statuses: String = Seq(NEW, OPEN, REFERRED, SUSPENDED)
    .map(_.toString)
    .reduce( (a: String, b: String) => s"$a,$b" )

  def findCase(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] = {
    val url = s"${configuration.bindingTariffClassificationUrl}/cases/$reference"
    client.GET[Option[Case]](url)
  }

  def findCasesByQueue(queue: Queue)(implicit hc: HeaderCarrier): Future[Seq[Case]] = {
    val queueId = if (queue == Queues.gateway) "none" else queue.id
    val queryString = s"queue_id=$queueId&assignee_id=none&status=$statuses&sort_by=days-elapsed&sort_direction=desc"
    val url = s"${configuration.bindingTariffClassificationUrl}/cases?$queryString"
    client.GET[Seq[Case]](url)
  }

  def findCasesByAssignee(assignee: Operator)(implicit hc: HeaderCarrier): Future[Seq[Case]] = {
    val queryString = s"assignee_id=${assignee.id}&status=$statuses&sort_by=days-elapsed&sort_direction=desc"
    val url = s"${configuration.bindingTariffClassificationUrl}/cases?$queryString"
    client.GET[Seq[Case]](url)
  }

  def updateCase(c: Case)(implicit hc: HeaderCarrier): Future[Case] = {
    val url = s"${configuration.bindingTariffClassificationUrl}/cases/${c.reference}"
    client.PUT[Case, Case](url = url, body = c)
  }

  def createEvent(c: Case, e: NewEventRequest)(implicit hc: HeaderCarrier): Future[Event] = {
    val url = s"${configuration.bindingTariffClassificationUrl}/cases/${c.reference}/events"
    client.POST[NewEventRequest, Event](url = url, body = e)
  }

  def findEvents(reference: String)(implicit hc: HeaderCarrier): Future[Seq[Event]] = {
    val url = s"${configuration.bindingTariffClassificationUrl}/cases/$reference/events"
    client.GET[Seq[Event]](url)
  }

  def search(search: Search, sort: Sort)(implicit hc: HeaderCarrier, clock: Clock = Clock.systemUTC(), queryBinder: QueryStringBindable[String]): Future[Seq[Case]] = {
    // Mandatory Params
    val params = Seq(
      queryBinder.unbind("sort_direction", sort.direction.toString),
      queryBinder.unbind("sort_by", sort.field.toString)
    )
      // Optional Params
      .++(search.traderName.map(queryBinder.unbind("trader_name", _)))
      .++(search.commodityCode.map(queryBinder.unbind("commodity_code", _)))
      .++(search.liveRulingsOnly.filter(identity).map(_ => queryBinder.unbind("min_decision_end", Instant.now(clock).toString) + "&" + queryBinder.unbind("status", CaseStatus.COMPLETED.toString)))
      .++(search.keywords.map(_.map(queryBinder.unbind("keyword", _)).mkString("&")))

    val url = s"${configuration.bindingTariffClassificationUrl}/cases?${params.mkString("&")}"
    client.GET[Seq[Case]](url)
  }

}
