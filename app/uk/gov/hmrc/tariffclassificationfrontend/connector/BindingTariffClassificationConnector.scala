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

import java.time.Clock

import com.google.inject.Inject
import javax.inject.Singleton
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.ApplicationType.ApplicationType
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.hmrc.tariffclassificationfrontend.models.EventType.EventType
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class BindingTariffClassificationConnector @Inject()(appConfig: AppConfig, client: AuthenticatedHttpClient) {

  private lazy val statuses: String = Set(NEW, OPEN, REFERRED, SUSPENDED)
    .map(_.toString).mkString(",")

  private lazy val liveStatuses: String = Set(OPEN, REFERRED, SUSPENDED)
    .map(_.toString).mkString(",")

  def createCase(application: Application)(implicit hc: HeaderCarrier): Future[Case] = {
    val url = s"${appConfig.bindingTariffClassificationUrl}/cases"
    client.POST[NewCaseRequest, Case](url, NewCaseRequest(application))
  }

  def findCase(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] = {
    val url = s"${appConfig.bindingTariffClassificationUrl}/cases/$reference"
    client.GET[Option[Case]](url)
  }

  private def buildQueryUrl(types : Seq[ApplicationType] = Seq(ApplicationType.BTI,ApplicationType.LIABILITY_ORDER), withStatuses: String,
                            queueId: String = "", assigneeId: String, pagination: Pagination): String = {
    val sortBy = "application.type,application.status,days-elapsed"
    val queryString = s"application_type=${types.mkString(",")}&queue_id=$queueId&assignee_id=$assigneeId&status=$withStatuses&sort_by=$sortBy&sort_direction=desc&page=${pagination.page}&page_size=${pagination.pageSize}"
    s"${appConfig.bindingTariffClassificationUrl}/cases?$queryString"
  }

  def findCasesByQueue(queue: Queue, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] = {
    val queueId = if (queue == Queues.gateway) "none" else queue.id
    val url = buildQueryUrl(withStatuses = statuses, queueId = queueId,  assigneeId = "none", pagination = pagination)
    client.GET[Paged[Case]](url)
  }

  def findCasesByAssignee(assignee: Operator, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] = {
    val url = buildQueryUrl(withStatuses = statuses, queueId = "",  assigneeId = assignee.id,  pagination = pagination)
    client.GET[Paged[Case]](url)
  }

  def findAssignedCases(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] = {
    val url = buildQueryUrl(withStatuses = liveStatuses, queueId = "",  assigneeId = "some",  pagination = pagination)
    client.GET[Paged[Case]](url)
  }

  def updateCase(c: Case)(implicit hc: HeaderCarrier): Future[Case] = {
    val url = s"${appConfig.bindingTariffClassificationUrl}/cases/${c.reference}"
    client.PUT[Case, Case](url = url, body = c)
  }

  def createEvent(c: Case, e: NewEventRequest)(implicit hc: HeaderCarrier): Future[Event] = {
    val url = s"${appConfig.bindingTariffClassificationUrl}/cases/${c.reference}/events"
    client.POST[NewEventRequest, Event](url = url, body = e)
  }

  def findEvents(reference: String, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Event]] = {
    findFilteredEvents(reference,pagination, Set.empty)
  }

  def findFilteredEvents(reference: String, pagination: Pagination, onlyEventTypes: Set[EventType])(implicit hc: HeaderCarrier): Future[Paged[Event]] = {

    val searchParam = s"case_reference=$reference" + onlyEventTypes.map(o => s"&type=$o").mkString("")

    val url = s"${appConfig.bindingTariffClassificationUrl}/events?${searchParam}&page=${pagination.page}&page_size=${pagination.pageSize}"
    client.GET[Paged[Event]](url)
  }

  def search(search: Search, sort: Sort, pagination: Pagination)
            (implicit hc: HeaderCarrier, clock: Clock = Clock.systemUTC(), qb: QueryStringBindable[String]): Future[Paged[Case]] = {

    val reqParams = Seq(
      qb.unbind("sort_direction", sort.direction.toString),
      qb.unbind("sort_by", sort.field.toString)
    )

    val optParams = Seq(
      search.traderName.map(qb.unbind("trader_name", _)),
      search.commodityCode.map(qb.unbind("commodity_code", _)),
      search.decisionDetails.map(qb.unbind("decision_details", _)),
      search.status.map(_.map(s => qb.unbind("status", s.toString)).mkString("&")),
      search.keywords.map(_.map(k => qb.unbind("keyword", k)).mkString("&"))
    ).filter(_.isDefined).map(_.get)

    val url = s"${appConfig.bindingTariffClassificationUrl}/cases?application_type=BTI&${(reqParams ++ optParams).mkString("&")}&page=${pagination.page}&page_size=${pagination.pageSize}"
    client.GET[Paged[Case]](url)
  }

  def generateReport(report: CaseReport)(implicit hc: HeaderCarrier): Future[Seq[ReportResult]] = {
    val url = s"${appConfig.bindingTariffClassificationUrl}/report?${CaseReport.bindable.unbind("", report)}"
    client.GET[Seq[ReportResult]](url)
  }

}
