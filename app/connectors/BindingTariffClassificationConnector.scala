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

package connectors

import com.codahale.metrics.MetricRegistry
import com.google.inject.Inject
import config.AppConfig
import metrics.HasMetrics
import models.CaseStatus._
import models.EventType.EventType
import models.Role.Role
import models._
import models.reporting._
import models.request.NewEventRequest
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import play.api.libs.json.Json
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.JsonFormatters._

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BindingTariffClassificationConnector @Inject() (
  appConfig: AppConfig,
  client: HttpClientV2,
  val metrics: MetricRegistry
)(implicit mat: Materializer)
    extends HasMetrics
    with InjectAuthHeader {

  implicit val ec: ExecutionContext = mat.executionContext

  private lazy val statuses: Set[CaseStatus] = Set(NEW, OPEN, REFERRED, SUSPENDED, COMPLETED)

  def createCase(application: Application)(implicit hc: HeaderCarrier): Future[Case] =
    withMetricsTimerAsync("create-case") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/cases"
      client
        .post(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .withBody(Json.toJson(NewCaseRequest(application)))
        .execute[Case]
    }

  def findCase(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] =
    withMetricsTimerAsync("get-case-by-reference") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/cases/$reference"
      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Option[Case]]
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
      val fullURL = buildQueryUrl(
        types = types,
        statuses = statuses,
        queueIds = Seq(queueId),
        assigneeId = "none",
        pagination = pagination
      )

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[Case]]
    }

  def findCasesByAllQueues(
    queue: Seq[Queue],
    pagination: Pagination,
    types: Set[ApplicationType] = ApplicationType.values,
    statuses: Set[CaseStatus] = CaseStatus.openStatuses,
    assignee: String
  )(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-cases-by-all-queues") { _ =>
      val fullURL = buildQueryUrl(
        types = types,
        statuses = statuses,
        queueIds = queue.map(_.id),
        assigneeId = assignee,
        pagination = pagination
      )

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[Case]]
    }

  def findCasesByAssignee(assignee: Operator, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-cases-by-assignee") { _ =>
      val fullURL =
        buildQueryUrl(statuses = statuses, queueIds = Seq(), assigneeId = assignee.id, pagination = pagination)

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[Case]]
    }

  def findAssignedCases(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    withMetricsTimerAsync("get-assigned-cases") { _ =>
      val fullURL = buildQueryUrl(
        statuses = CaseStatus.openStatuses,
        queueIds = Seq(),
        assigneeId = "some",
        pagination = pagination
      )

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[Case]]
    }

  def updateCase(c: Case)(implicit hc: HeaderCarrier): Future[Case] =
    withMetricsTimerAsync("update-case") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/cases/${c.reference}"

      client
        .put(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .withBody(Json.toJson(c))
        .execute[Case]
    }

  def createEvent(c: Case, e: NewEventRequest)(implicit hc: HeaderCarrier): Future[Event] =
    withMetricsTimerAsync("create-event") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/cases/${c.reference}/events"
      client
        .post(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .withBody(Json.toJson(e))
        .execute[Event]
    }

  def findEvents(reference: String, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Event]] =
    findFilteredEvents(reference, pagination, Set.empty)

  def findFilteredEvents(reference: String, pagination: Pagination, onlyEventTypes: Set[EventType])(implicit
    hc: HeaderCarrier
  ): Future[Paged[Event]] =
    withMetricsTimerAsync("get-events-for-case") { _ =>
      val searchParam = s"case_reference=$reference" + onlyEventTypes.map(o => s"&type=$o").mkString("")
      val fullURL =
        s"${appConfig.bindingTariffClassificationUrl}/events?$searchParam&page=${pagination.page}&page_size=${pagination.pageSize}"

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[Event]]
    }

  private def latestEventByCase(events: Seq[Event]): Map[String, Event] =
    events
      .groupBy(_.caseReference)
      .flatMap { case (_, eventsForCase) =>
        eventsForCase
          .sortBy(_.timestamp)(Event.latestFirst)
          .headOption
          .map(event => event.caseReference -> event)
          .toMap
      }

  def findReferralEvents(references: Set[String])(implicit
    hc: HeaderCarrier
  ): Future[Map[String, Event]] =
    withMetricsTimerAsync("get-referral-events") { _ =>
      val pagination = NoPagination()
      // Conservative approximation as to how many case references we can fit into a single URL
      val batchSize = (appConfig.maxUriLength.intValue - appConfig.bindingTariffClassificationUrl.length - 250) / 10
      Source(references)
        .grouped(batchSize)
        .mapAsync(Runtime.getRuntime.availableProcessors()) { ids =>
          val searchParam = s"case_reference=${ids.mkString(",")}&type=${EventType.CASE_REFERRAL}"
          val fullURL =
            s"${appConfig.bindingTariffClassificationUrl}/events?$searchParam&page=${pagination.page}&page_size=${pagination.pageSize}"

          client
            .get(url"$fullURL")
            .setHeader(authHeaders(appConfig): _*)
            .execute[Paged[Event]]
        }
        .runFold(Map.empty[String, Event]) { case (eventsById, nextBatch) =>
          eventsById ++ latestEventByCase(nextBatch.results)
        }
    }

  def findCompletionEvents(references: Set[String])(implicit
    hc: HeaderCarrier
  ): Future[Map[String, Event]] =
    withMetricsTimerAsync("get-completion-events") { _ =>
      val pagination = NoPagination()
      // Conservative approximation as to how many case references we can fit into a single URL
      val batchSize = (appConfig.maxUriLength.intValue - appConfig.bindingTariffClassificationUrl.length - 250) / 10
      Source(references)
        .grouped(batchSize)
        .mapAsync(Runtime.getRuntime.availableProcessors()) { ids =>
          val searchParam = s"case_reference=${ids.mkString(",")}&type=${EventType.CASE_COMPLETED}"
          val fullURL =
            s"${appConfig.bindingTariffClassificationUrl}/events?$searchParam&page=${pagination.page}&page_size=${pagination.pageSize}"

          client
            .get(url"$fullURL")
            .setHeader(authHeaders(appConfig): _*)
            .execute[Paged[Event]]
        }
        .runFold(Map.empty[String, Event]) { case (eventsById, nextBatch) =>
          eventsById ++ latestEventByCase(nextBatch.results)
        }
    }

  def search(search: Search, sort: Sort, pagination: Pagination)(implicit
    hc: HeaderCarrier,
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

      val fullURL =
        s"${appConfig.bindingTariffClassificationUrl}/cases?${(reqParams ++ optParams).mkString("&")}&page=${pagination.page}&page_size=${pagination.pageSize}"

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[Case]]
    }

  def getAllUsers(roles: Seq[Role], team: String, pagination: Pagination)(implicit
    hc: HeaderCarrier
  ): Future[Paged[Operator]] =
    withMetricsTimerAsync("get-all-users") { _ =>
      val searchParam = s"role=${roles.mkString(",")}&member_of_teams=$team"
      val fullURL =
        s"${appConfig.bindingTariffClassificationUrl}/users?$searchParam&page=${pagination.page}&page_size=${pagination.pageSize}"

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[Operator]]
    }

  def updateUser(operator: Operator)(implicit hc: HeaderCarrier): Future[Operator] =
    withMetricsTimerAsync("update-user") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/users/${operator.id}"

      client
        .put(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .withBody(Json.toJson(operator))
        .execute[Operator]
    }

  def markDeleted(operator: Operator)(implicit hc: HeaderCarrier): Future[Operator] =
    withMetricsTimerAsync("delete-user") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/mark-deleted/users/${operator.id}"

      client
        .put(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .withBody(Json.toJson(operator))
        .execute[Operator]
    }

  def getUserDetails(id: String)(implicit hc: HeaderCarrier): Future[Option[Operator]] =
    withMetricsTimerAsync("get-user-details") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/users/$id"

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Option[Operator]]
    }

  def createUser(operator: Operator)(implicit hc: HeaderCarrier): Future[Operator] =
    withMetricsTimerAsync("create-user") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/users"

      client
        .post(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .withBody(Json.toJson(NewUserRequest(operator)))
        .execute[Operator]
    }

  def caseReport(report: CaseReport, pagination: Pagination)(implicit
    hc: HeaderCarrier,
    reportBind: QueryStringBindable[CaseReport],
    pageBind: QueryStringBindable[Pagination]
  ): Future[Paged[Map[String, ReportResultField[_]]]] =
    withMetricsTimerAsync("case-report") { _ =>
      val reportQuery = reportBind.unbind("", report)
      val pageQuery   = pageBind.unbind("", pagination)
      val fullURL     = s"${appConfig.bindingTariffClassificationUrl}/report/cases?$reportQuery&$pageQuery"

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[Map[String, ReportResultField[_]]]]
    }

  def summaryReport(report: SummaryReport, pagination: Pagination)(implicit
    hc: HeaderCarrier,
    reportBind: QueryStringBindable[SummaryReport],
    pageBind: QueryStringBindable[Pagination]
  ): Future[Paged[ResultGroup]] =
    withMetricsTimerAsync("summary-report") { _ =>
      val reportQuery = reportBind.unbind("", report)
      val pageQuery   = pageBind.unbind("", pagination)
      val fullURL     = s"${appConfig.bindingTariffClassificationUrl}/report/summary?$reportQuery&$pageQuery"

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[ResultGroup]]
    }

  def queueReport(report: QueueReport, pagination: Pagination)(implicit
    hc: HeaderCarrier,
    reportBind: QueryStringBindable[QueueReport],
    pageBind: QueryStringBindable[Pagination]
  ): Future[Paged[QueueResultGroup]] =
    withMetricsTimerAsync("queue-report") { _ =>
      val reportQuery = reportBind.unbind("", report)
      val pageQuery   = pageBind.unbind("", pagination)
      val fullURL     = s"${appConfig.bindingTariffClassificationUrl}/report/queues?$reportQuery&$pageQuery"

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[QueueResultGroup]]
    }

  def createKeyword(keyword: Keyword)(implicit hc: HeaderCarrier): Future[Keyword] =
    withMetricsTimerAsync("create-keyword") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/keyword"

      client
        .post(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .withBody(Json.toJson(NewKeywordRequest(Keyword(keyword.name.toUpperCase, keyword.approved))))
        .execute[Keyword]
    }

  def findAllKeywords(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Keyword]] =
    withMetricsTimerAsync("find-all-keywords") { _ =>
      val fullURL =
        s"${appConfig.bindingTariffClassificationUrl}/keywords?page=${pagination.page}&page_size=${pagination.pageSize}"

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[Keyword]]
    }

  def getCaseKeywords()(implicit hc: HeaderCarrier): Future[Paged[CaseKeyword]] =
    withMetricsTimerAsync("get-case-keywords") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/case-keywords"

      client
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Paged[CaseKeyword]]
    }

  def deleteKeyword(keyword: Keyword)(implicit hc: HeaderCarrier): Future[Unit] =
    withMetricsTimerAsync("delete-keyword") { _ =>
      val fullURL = s"${appConfig.bindingTariffClassificationUrl}/keyword/${keyword.name}"

      client
        .delete(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[HttpResponse](throwOnFailure(readEitherOf(readRaw)), ec)
        .map(_ => ())
    }
}
