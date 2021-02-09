/*
 * Copyright 2021 HM Revenue & Customs
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

package connector

import com.google.inject.Inject
import com.kenshoo.play.metrics.Metrics
import javax.inject.Singleton
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.http.HeaderCarrier
import config.AppConfig
import metrics.HasMetrics
import models.ApplicationType
import models.CaseStatus._
import models.EventType.EventType
import models._
import models.request.NewEventRequest
import play.api.libs.json.Reads

import scala.concurrent.{ExecutionContext, Future}
import utils.JsonFormatters._
import uk.gov.hmrc.http.HttpReads.Implicits._
import utils.JsonFormatters

@Singleton
class BindingTariffClassificationConnector @Inject() (
  appConfig: AppConfig,
  client: AuthenticatedHttpClient,
  val metrics: Metrics
)(implicit ec: ExecutionContext)
    extends HasMetrics {

  private lazy val statuses: String = Set(NEW, OPEN, REFERRED, SUSPENDED, COMPLETED)
    .map(_.toString)
    .mkString(",")

  private lazy val liveStatuses: String = Set(OPEN, REFERRED, SUSPENDED)
    .map(_.toString)
    .mkString(",")

  def createCase(application: Application)(implicit hc: HeaderCarrier): Future[Case] =
    withMetricsTimerAsync("create-case") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases"
      client.POST[NewCaseRequest, Case](url, NewCaseRequest(application))
    }

  def findCase(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] =
    withMetricsTimerAsync("get-case-by-reference") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases/$reference"
      client.GET[Option[Case]](url)
    }

  private def buildQueryUrl(
    types: Seq[ApplicationType] = ApplicationType.values.toSeq,
    statuses: String,
    queueIds: Seq[String],
    assigneeId: String,
    pagination: Pagination
  ): String = {
    val sortBy = "application.type,application.status,days-elapsed"
    val queryString =
      s"application_type=${types.map(_.name).mkString(",")}&queue_id=${queueIds.mkString(",")}&assignee_id=$assigneeId&status=$statuses&sort_by=$sortBy&sort_direction=desc&page=${pagination.page}&page_size=${pagination.pageSize}"
    s"${appConfig.bindingTariffClassificationUrl}/cases?$queryString"
  }

  def findCasesByQueue(
    queue: Queue,
    pagination: Pagination,
    types: Seq[ApplicationType] = ApplicationType.values.toSeq
  )(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-cases-by-queue") { _ =>
      val queueId = if (queue == Queues.gateway) "none" else queue.id
      val url = buildQueryUrl(
        types      = types,
        statuses   = statuses,
        queueIds   = Seq(queueId),
        assigneeId = "none",
        pagination = pagination
      )
      client.GET[Paged[Case]](url)
    }

  def findCasesByAllQueues(
    queue: Seq[Queue],
    pagination: Pagination,
    types: Seq[ApplicationType] = ApplicationType.values.toSeq
  )(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-cases-by-queue") { _ =>
      val url = buildQueryUrl(
        types      = types,
        statuses   = statuses,
        queueIds   = queue.map(_.id),
        assigneeId = "none",
        pagination = pagination
      )
      client.GET[Paged[Case]](url)
    }

  def findCasesByAssignee(assignee: Operator, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-cases-by-assignee") { _ =>
      val url = buildQueryUrl(statuses = statuses, queueIds = Seq(), assigneeId = assignee.id, pagination = pagination)
      client.GET[Paged[Case]](url)
    }

  def findAssignedCases(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-assigned-cases") { _ =>
      val url = buildQueryUrl(statuses = liveStatuses, queueIds = Seq(), assigneeId = "some", pagination = pagination)
      client.GET[Paged[Case]](url)
    }

  def updateCase(c: Case)(implicit hc: HeaderCarrier): Future[Case] =
    withMetricsTimerAsync("update-case") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases/${c.reference}"
      client.PUT[Case, Case](url = url, body = c)
    }

  def createEvent(c: Case, e: NewEventRequest)(implicit hc: HeaderCarrier): Future[Event] =
    withMetricsTimerAsync("create-event") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases/${c.reference}/events"
      client.POST[NewEventRequest, Event](url = url, body = e)
    }

  def findEvents(reference: String, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Event]] =
    findFilteredEvents(reference, pagination, Set.empty)

  def findFilteredEvents(reference: String, pagination: Pagination, onlyEventTypes: Set[EventType])(
    implicit hc: HeaderCarrier
  ): Future[Paged[Event]] =
    withMetricsTimerAsync("get-events-for-case") { _ =>
      val searchParam = s"case_reference=$reference" + onlyEventTypes.map(o => s"&type=$o").mkString("")
      val url =
        s"${appConfig.bindingTariffClassificationUrl}/events?$searchParam&page=${pagination.page}&page_size=${pagination.pageSize}"
      client.GET[Paged[Event]](url)
    }

  def findCompletionEvents(references: Set[String], pagination: Pagination)(
    implicit hc: HeaderCarrier
  ): Future[Paged[Event]] =
    withMetricsTimerAsync("get-completion-events") { _ =>
      val searchParam = s"case_reference=${references.mkString(",")}&type=${EventType.CASE_COMPLETED}"
      val url =
        s"${appConfig.bindingTariffClassificationUrl}/events?$searchParam&page=${pagination.page}&page_size=${pagination.pageSize}"
      client.GET[Paged[Event]](url)
    }

  def search(search: Search, sort: Sort, pagination: Pagination)(
    implicit hc: HeaderCarrier,
    qb: QueryStringBindable[String]
  ): Future[Paged[Case]] =
    withMetricsTimerAsync("search-cases") { _ =>
      val reqParams = Seq(
        qb.unbind("sort_direction", sort.direction.toString),
        qb.unbind("sort_by", sort.field.toString)
      )

      val optParams = Seq(
        search.traderName.map(qb.unbind("trader_name", _)),
        search.commodityCode.map(qb.unbind("commodity_code", _)),
        search.decisionDetails.map(qb.unbind("decision_details", _)),
        search.status.map(_.map(s => qb.unbind("status", s.toString)).mkString("&")),
        search.applicationType.map(_.map(s => qb.unbind("application_type", s.name)).mkString("&")),
        search.keywords.map(_.map(k => qb.unbind("keyword", k)).mkString("&"))
      ).filter(_.isDefined).map(_.get)

      val url =
        s"${appConfig.bindingTariffClassificationUrl}/cases?${(reqParams ++ optParams).mkString("&")}&page=${pagination.page}&page_size=${pagination.pageSize}"
      client.GET[Paged[Case]](url)
    }

  def generateReport(report: CaseReport)(implicit hc: HeaderCarrier): Future[Seq[ReportResult]] =
    withMetricsTimerAsync("generate-report") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/report?${CaseReport.bindable.unbind("", report)}"
      client.GET[Seq[ReportResult]](url)
    }

  def updateUser(o: Operator)(implicit hc: HeaderCarrier): Future[Operator] =
    withMetricsTimerAsync("update-user") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/users/${o.id}"
      client.PUT[Operator, Operator](url = url, body = o)
    }

  def getUserDetails(id: String)(implicit hc: HeaderCarrier): Future[Operator] =
    withMetricsTimerAsync("update-user") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/users/$id"
      client.GET[Operator](url = url)
    }

  def createUser(operator: Operator)(implicit hc: HeaderCarrier): Future[Operator] =
    withMetricsTimerAsync("create-user") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/users"
      implicit val rd
        : Reads[Operator] = JsonFormatters.operator //this was required to get the code to compile, but don't think this should be here
      client.POST[NewUserRequest, Operator](url, NewUserRequest(operator))
    }

}
