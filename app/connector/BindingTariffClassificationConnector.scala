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

package connector

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.google.inject.Inject
import com.kenshoo.play.metrics.Metrics
import config.AppConfig

import javax.inject.Singleton
import metrics.HasMetrics
import models.CaseStatus._
import models.EventType.EventType
import models.Role.Role
import models.reporting._
import models.request.NewEventRequest
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import utils.JsonFormatters._
import javax.inject.Singleton
import models._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BindingTariffClassificationConnector @Inject() (
  appConfig: AppConfig,
  client: AuthenticatedHttpClient,
  val metrics: Metrics
)(implicit mat: Materializer)
    extends HasMetrics {

  implicit val ec: ExecutionContext = mat.executionContext

  private lazy val statuses: Set[CaseStatus] = Set(NEW, OPEN, REFERRED, SUSPENDED, COMPLETED)

  def createCase(application: Application)(implicit hc: HeaderCarrier): Future[Case] =
    withMetricsTimerAsync("create-case") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases"
      client.POST[NewCaseRequest, Case](url, NewCaseRequest(application), headers = client.addAuth)
    }

  def findCase(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] =
    withMetricsTimerAsync("get-case-by-reference") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases/$reference"
      client.GET[Option[Case]](url, headers = client.addAuth)
    }

  private def buildQueryUrl(
    types: Set[ApplicationType] = ApplicationType.values,
    statuses: Set[CaseStatus],
    queueIds: Seq[String],
    assigneeId: String,
    pagination: Pagination
  ): String = {
    val sortBy = "application.type,application.status,days-elapsed"

    val queryString =
      s"application_type=${types.map(_.name).mkString(",")}" +
        s"&queue_id=${queueIds.mkString(",")}" +
        s"&assignee_id=$assigneeId" +
        s"&status=${statuses.map(_.toString).mkString(",")}" +
        s"&sort_by=$sortBy" +
        "&sort_direction=desc" +
        s"&page=${pagination.page}" +
        s"&page_size=${pagination.pageSize}"

    s"${appConfig.bindingTariffClassificationUrl}/cases?$queryString"
  }

  def findCasesByQueue(
    queue: Queue,
    pagination: Pagination,
    types: Set[ApplicationType] = ApplicationType.values
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
      client.GET[Paged[Case]](url, headers = client.addAuth)
    }

  def findCasesByAllQueues(
    queue: Seq[Queue],
    pagination: Pagination,
    types: Set[ApplicationType] = ApplicationType.values,
    statuses: Set[CaseStatus]   = CaseStatus.openStatuses,
    assignee: String
  )(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-cases-by-all-queues") { _ =>
      val url = buildQueryUrl(
        types      = types,
        statuses   = statuses,
        queueIds   = queue.map(_.id),
        assigneeId = assignee,
        pagination = pagination
      )
      client.GET[Paged[Case]](url, headers = client.addAuth)
    }

  def findCasesByAssignee(assignee: Operator, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-cases-by-assignee") { _ =>
      val url = buildQueryUrl(statuses = statuses, queueIds = Seq(), assigneeId = assignee.id, pagination = pagination)
      client.GET[Paged[Case]](url, headers = client.addAuth)
    }

  def findAssignedCases(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-assigned-cases") { _ =>
      val url = buildQueryUrl(
        statuses   = CaseStatus.openStatuses,
        queueIds   = Seq(),
        assigneeId = "some",
        pagination = pagination
      )
      client.GET[Paged[Case]](url, headers = client.addAuth)
    }

  def updateCase(c: Case)(implicit hc: HeaderCarrier): Future[Case] =
    withMetricsTimerAsync("update-case") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases/${c.reference}"
      client.PUT[Case, Case](url = url, body = c, headers = client.addAuth)
    }

  def createEvent(c: Case, e: NewEventRequest)(implicit hc: HeaderCarrier): Future[Event] =
    withMetricsTimerAsync("create-event") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases/${c.reference}/events"
      client.POST[NewEventRequest, Event](url = url, body = e, headers = client.addAuth)
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
      client.GET[Paged[Event]](url, headers = client.addAuth)
    }

  private def latestEventByCase(events: Seq[Event]): Map[String, Event] =
    events
      .groupBy(_.caseReference)
      .flatMap {
        case (_, eventsForCase) =>
          eventsForCase
            .sortBy(_.timestamp)(Event.latestFirst)
            .headOption
            .map(event => event.caseReference -> event)
            .toMap
      }

  def findReferralEvents(references: Set[String])(
    implicit hc: HeaderCarrier
  ): Future[Map[String, Event]] =
    withMetricsTimerAsync("get-referral-events") { _ =>
      val pagination = NoPagination()
      // Conservative approximation as to how many case references we can fit into a single URL
      val batchSize = (appConfig.maxUriLength.intValue - appConfig.bindingTariffClassificationUrl.length - 250) / 10
      Source(references)
        .grouped(batchSize)
        .mapAsync(Runtime.getRuntime().availableProcessors()) { ids =>
          val searchParam = s"case_reference=${ids.mkString(",")}&type=${EventType.CASE_REFERRAL}"
          val url =
            s"${appConfig.bindingTariffClassificationUrl}/events?$searchParam&page=${pagination.page}&page_size=${pagination.pageSize}"
          client.GET[Paged[Event]](url, headers = client.addAuth)
        }
        .runFold(Map.empty[String, Event]) {
          case (eventsById, nextBatch) =>
            eventsById ++ latestEventByCase(nextBatch.results)
        }
    }

  def findCompletionEvents(references: Set[String])(
    implicit hc: HeaderCarrier
  ): Future[Map[String, Event]] =
    withMetricsTimerAsync("get-completion-events") { _ =>
      val pagination = NoPagination()
      // Conservative approximation as to how many case references we can fit into a single URL
      val batchSize = (appConfig.maxUriLength.intValue - appConfig.bindingTariffClassificationUrl.length - 250) / 10
      Source(references)
        .grouped(batchSize)
        .mapAsync(Runtime.getRuntime().availableProcessors()) { ids =>
          val searchParam = s"case_reference=${ids.mkString(",")}&type=${EventType.CASE_COMPLETED}"
          val url =
            s"${appConfig.bindingTariffClassificationUrl}/events?$searchParam&page=${pagination.page}&page_size=${pagination.pageSize}"
          client.GET[Paged[Event]](url, headers = client.addAuth)
        }
        .runFold(Map.empty[String, Event]) {
          case (eventsById, nextBatch) =>
            eventsById ++ latestEventByCase(nextBatch.results)
        }
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
        search.caseDetails.map(qb.unbind("case_details", _)),
        search.caseSource.map(qb.unbind("case_source", _)),
        search.commodityCode.map(qb.unbind("commodity_code", _)),
        search.decisionDetails.map(qb.unbind("decision_details", _)),
        search.status.map(_.map(s => qb.unbind("status", s.toString)).mkString("&")),
        search.applicationType.map(_.map(s => qb.unbind("application_type", s.name)).mkString("&")),
        search.keywords.map(_.map(k => qb.unbind("keyword", k)).mkString("&"))
      ).filter(_.isDefined).map(_.get)

      val url =
        s"${appConfig.bindingTariffClassificationUrl}/cases?${(reqParams ++ optParams).mkString("&")}&page=${pagination.page}&page_size=${pagination.pageSize}"
      client.GET[Paged[Case]](url, headers = client.addAuth)
    }

  def getAllUsers(roles: Seq[Role], team: String, pagination: Pagination)(
    implicit hc: HeaderCarrier
  ): Future[Paged[Operator]] =
    withMetricsTimerAsync("get-all-users") { _ =>
      val searchParam = s"role=${roles.mkString(",")}&member_of_teams=$team"
      val url =
        s"${appConfig.bindingTariffClassificationUrl}/users?$searchParam&page=${pagination.page}&page_size=${pagination.pageSize}"

      client.GET[Paged[Operator]](url = url, headers = client.addAuth)
    }

  def updateUser(o: Operator)(implicit hc: HeaderCarrier): Future[Operator] =
    withMetricsTimerAsync("update-user") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/users/${o.id}"
      client.PUT[Operator, Operator](url = url, body = o, headers = client.addAuth)
    }

  def markDeleted(o: Operator)(implicit hc: HeaderCarrier): Future[Operator] =
    withMetricsTimerAsync("delete-user") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/mark-deleted/users/${o.id}"
      client.PUT[Operator, Operator](url = url, body = o, headers = client.addAuth)
    }

  def getUserDetails(id: String)(implicit hc: HeaderCarrier): Future[Option[Operator]] =
    withMetricsTimerAsync("get-user-details") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/users/$id"
      client.GET[Option[Operator]](url = url, headers = client.addAuth)
    }

  def createUser(operator: Operator)(implicit hc: HeaderCarrier): Future[Operator] =
    withMetricsTimerAsync("create-user") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/users"
      client.POST[NewUserRequest, Operator](url, NewUserRequest(operator), headers = client.addAuth)
    }

  def caseReport(report: CaseReport, pagination: Pagination)(
    implicit hc: HeaderCarrier,
    reportBind: QueryStringBindable[CaseReport],
    pageBind: QueryStringBindable[Pagination]
  ): Future[Paged[Map[String, ReportResultField[_]]]] =
    withMetricsTimerAsync("case-report") { _ =>
      val reportQuery = reportBind.unbind("", report)
      val pageQuery   = pageBind.unbind("", pagination)
      val url         = s"${appConfig.bindingTariffClassificationUrl}/report/cases?$reportQuery&$pageQuery"
      client.GET[Paged[Map[String, ReportResultField[_]]]](url, headers = client.addAuth)
    }

  def summaryReport(report: SummaryReport, pagination: Pagination)(
    implicit hc: HeaderCarrier,
    reportBind: QueryStringBindable[SummaryReport],
    pageBind: QueryStringBindable[Pagination]
  ): Future[Paged[ResultGroup]] =
    withMetricsTimerAsync("summary-report") { _ =>
      val reportQuery = reportBind.unbind("", report)
      val pageQuery   = pageBind.unbind("", pagination)
      val url         = s"${appConfig.bindingTariffClassificationUrl}/report/summary?$reportQuery&$pageQuery"
      client.GET[Paged[ResultGroup]](url, headers = client.addAuth)
    }

  def queueReport(report: QueueReport, pagination: Pagination)(
    implicit hc: HeaderCarrier,
    reportBind: QueryStringBindable[QueueReport],
    pageBind: QueryStringBindable[Pagination]
  ): Future[Paged[QueueResultGroup]] =
    withMetricsTimerAsync("queue-report") { _ =>
      val reportQuery = reportBind.unbind("", report)
      val pageQuery   = pageBind.unbind("", pagination)
      val url         = s"${appConfig.bindingTariffClassificationUrl}/report/queues?$reportQuery&$pageQuery"
      client.GET[Paged[QueueResultGroup]](url, headers = client.addAuth)
    }

  def createKeyword(keyword: Keyword)(implicit hc: HeaderCarrier): Future[Keyword] =
    withMetricsTimerAsync("create-keyword") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/keyword"
      client
        .POST[NewKeywordRequest, Keyword](
          url,
          NewKeywordRequest(Keyword(keyword.name.toUpperCase, keyword.approved)),
          headers = client.addAuth
        )
    }

  def findAllKeywords(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Keyword]] =
    withMetricsTimerAsync("find-all-keywords") { _ =>
      val url =
        s"${appConfig.bindingTariffClassificationUrl}/keywords?page=${pagination.page}&page_size=${pagination.pageSize}"
      client.GET[Paged[Keyword]](url, headers = client.addAuth)
    }

  def getCaseKeywords()(implicit hc: HeaderCarrier): Future[Paged[CaseKeyword]] =
    withMetricsTimerAsync("get-case-keywords") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/case-keywords"
      client.GET[Paged[CaseKeyword]](url, headers = client.addAuth)
    }

  def deleteKeyword(keyword: Keyword)(implicit hc: HeaderCarrier): Future[Unit] =
    withMetricsTimerAsync("delete-keyword") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/keyword/${keyword.name}"
      client.DELETE[Unit](url, headers = client.addAuth).map(_ => ())
    }
}
