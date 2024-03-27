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

package controllers

import cats.data.NonEmptySeq
import models._
import models.reporting._
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.managementtools._
import views.html.report_not_found

import java.time.Instant
import scala.concurrent.Future

class ReportingControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val reportingService         = mock[ReportingService]
  private val queueService             = injector.instanceOf[QueuesService]
  private val usersService             = mock[UserService]
  private val operator                 = mock[Operator]
  private lazy val manage_reports_view = injector.instanceOf[manage_reports_view]

  private val summaryReportView = injector.instanceOf[summaryReportView]
  private val queueReportView   = injector.instanceOf[queueReportView]
  private val caseReportView    = injector.instanceOf[caseReportView]
  private val reportChooseDates = injector.instanceOf[reportChooseDates]
  private val reportChooseTeams = injector.instanceOf[reportChooseTeams]
  private val reportNotFound    = injector.instanceOf[report_not_found]

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(
      reportingService,
      usersService,
      operator
    )
  }

  private def controller(permission: Set[Permission], operator: Operator = Operator("0", Some("name"))) =
    new ReportingController(
      new RequestActionsWithPermissions(playBodyParsers, permission, op = operator),
      reportingService,
      queueService,
      usersService,
      mcc,
      manage_reports_view,
      summaryReportView,
      queueReportView,
      caseReportView,
      reportChooseDates,
      reportChooseTeams,
      reportNotFound,
      realAppConfig
    )

  "downloadCaseReport" should {
    val report = CaseReport(
      name   = "ATaR Summary Report",
      fields = NonEmptySeq.of(ReportField.Reference, ReportField.GoodsName, ReportField.TraderName)
    )

    val reportResults: Paged[Map[String, ReportResultField[_]]] = Paged(
      Seq(
        Map(
          ReportField.Reference.fieldName  -> StringResultField(ReportField.Reference.fieldName, Some("123456")),
          ReportField.GoodsName.fieldName  -> StringResultField(ReportField.GoodsName.fieldName, Some("Fireworks")),
          ReportField.TraderName.fieldName -> StringResultField(ReportField.TraderName.fieldName, Some("Gandalf"))
        ),
        Map(
          ReportField.Reference.fieldName -> StringResultField(ReportField.Reference.fieldName, Some("987654")),
          ReportField.GoodsName.fieldName -> StringResultField(ReportField.GoodsName.fieldName, Some("Beer")),
          ReportField.TraderName.fieldName -> StringResultField(
            ReportField.TraderName.fieldName,
            Some("Barliman Butterbur")
          )
        )
      )
    )

    "return 200 OK and text/csv content type" in {
      given(reportingService.caseReport(any[CaseReport], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Future.successful(reportResults))
        .willReturn(Future.successful(Paged.empty[Map[String, ReportResultField[_]]]))

      given(usersService.getAllUsers(any[Seq[Role.Role]], any[String], any[Pagination])(any[HeaderCarrier])) willReturn Future
        .successful(Paged.empty[Operator])

      val result =
        await(controller(Set(Permission.VIEW_REPORTS)).downloadCaseReport(report)(fakeRequest))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/csv")
      contentAsString(result) shouldBe Seq(
        "Reference,Goods name,Trader name",
        "123456,Fireworks,Gandalf",
        "987654,Beer,Barliman Butterbur"
      ).mkString(
        "",
        "\r\n",
        "\r\n"
      )
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).downloadCaseReport(report)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "downloadQueueReport" should {
    val report = QueueReport()

    val reportResults: Paged[QueueResultGroup] = Paged(
      Seq(
        QueueResultGroup(4, None, ApplicationType.ATAR),
        QueueResultGroup(3, None, ApplicationType.LIABILITY),
        QueueResultGroup(7, None, ApplicationType.CORRESPONDENCE),
        QueueResultGroup(1, None, ApplicationType.MISCELLANEOUS),
        QueueResultGroup(8, Some("2"), ApplicationType.ATAR),
        QueueResultGroup(5, Some("2"), ApplicationType.LIABILITY),
        QueueResultGroup(1, Some("3"), ApplicationType.CORRESPONDENCE),
        QueueResultGroup(2, Some("3"), ApplicationType.MISCELLANEOUS)
      )
    )

    "return 200 OK and text/csv content type" in {
      given(reportingService.queueReport(any[QueueReport], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Future.successful(reportResults))
        .willReturn(Future.successful(Paged.empty[QueueResultGroup]))

      given(usersService.getAllUsers(any[Seq[Role.Role]], any[String], any[Pagination])(any[HeaderCarrier])) willReturn Future
        .successful(Paged.empty[Operator])

      val result =
        await(controller(Set(Permission.VIEW_REPORTS)).downloadQueueReport(report)(fakeRequest))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/csv")
      contentAsString(result) shouldBe Seq(
        "Assigned team,Case type,Count",
        "Gateway,ATaR,4",
        "Gateway,Liability,3",
        "Gateway,Correspondence,7",
        "Gateway,Miscellaneous,1",
        "ACT,ATaR,8",
        "ACT,Liability,5",
        "CAP,Correspondence,1",
        "CAP,Miscellaneous,2"
      ).mkString(
        "",
        "\r\n",
        "\r\n"
      )
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).downloadQueueReport(report)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "downloadSummaryReport" should {
    val report = SummaryReport(
      name      = "Case count by status",
      groupBy   = NonEmptySeq.one(ReportField.Status),
      sortBy    = ReportField.Status,
      maxFields = Seq(ReportField.ElapsedDays)
    )

    val reportResults: Paged[ResultGroup] = Paged(
      Seq(
        SimpleResultGroup(
          2,
          NonEmptySeq.one(StatusResultField(ReportField.Status.fieldName, Some(PseudoCaseStatus.COMPLETED))),
          List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(5)))
        ),
        SimpleResultGroup(
          4,
          NonEmptySeq.one(StatusResultField(ReportField.Status.fieldName, Some(PseudoCaseStatus.CANCELLED))),
          List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(2)))
        ),
        SimpleResultGroup(
          6,
          NonEmptySeq.one(StatusResultField(ReportField.Status.fieldName, Some(PseudoCaseStatus.OPEN))),
          List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(8)))
        ),
        SimpleResultGroup(
          7,
          NonEmptySeq.one(StatusResultField(ReportField.Status.fieldName, Some(PseudoCaseStatus.NEW))),
          List(NumberResultField(ReportField.ElapsedDays.fieldName, Some(4)))
        )
      )
    )

    "return 200 OK and text/csv content type" in {
      given(reportingService.summaryReport(any[SummaryReport], any[Pagination])(any[HeaderCarrier]))
        .willReturn(Future.successful(reportResults))
        .willReturn(Future.successful(Paged.empty[ResultGroup]))

      given(usersService.getAllUsers(any[Seq[Role.Role]], any[String], any[Pagination])(any[HeaderCarrier])) willReturn Future
        .successful(Paged.empty[Operator])

      val result =
        await(controller(Set(Permission.VIEW_REPORTS)).downloadSummaryReport(report)(fakeRequest))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/csv")
      contentAsString(result) shouldBe Seq(
        "Case status,Count,Elapsed days",
        "COMPLETED,2,5",
        "CANCELLED,4,2",
        "OPEN,6,8",
        "NEW,7,4"
      ).mkString(
        "",
        "\r\n",
        "\r\n"
      )
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).downloadSummaryReport(report)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "caseReport" should {
    val report = CaseReport(
      name   = "ATaR Summary Report",
      fields = NonEmptySeq.of(ReportField.Reference, ReportField.GoodsName, ReportField.TraderName)
    )

    "return 200 OK and HTML content type" in {
      given(reportingService.caseReport(any[CaseReport], any[Pagination])(any[HeaderCarrier])) willReturn Future
        .successful(Paged.empty[Map[String, ReportResultField[_]]])
      given(usersService.getAllUsers(any[Seq[Role.Role]], any[String], any[Pagination])(any[HeaderCarrier])) willReturn Future
        .successful(Paged.empty[Operator])

      val result = await(controller(Set(Permission.VIEW_REPORTS)).caseReport(report, SearchPagination())(fakeRequest))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).caseReport(report, SearchPagination())(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "summaryReport" should {
    val report = SummaryReport(
      name    = "Case count by status",
      groupBy = NonEmptySeq.one(ReportField.Status),
      sortBy  = ReportField.Status
    )

    "return 200 OK and HTML content type" in {
      given(reportingService.summaryReport(any[SummaryReport], any[Pagination])(any[HeaderCarrier])) willReturn Future
        .successful(Paged.empty[ResultGroup])
      given(usersService.getAllUsers(any[Seq[Role.Role]], any[String], any[Pagination])(any[HeaderCarrier])) willReturn Future
        .successful(Paged.empty[Operator])

      val result =
        await(controller(Set(Permission.VIEW_REPORTS)).summaryReport(report, SearchPagination())(fakeRequest))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).summaryReport(report, SearchPagination())(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "queueReport" should {
    val report = QueueReport()

    "return 200 OK and HTML content type" in {
      given(reportingService.queueReport(any[QueueReport], any[Pagination])(any[HeaderCarrier])) willReturn Future
        .successful(Paged.empty[QueueResultGroup])

      val result =
        await(controller(Set(Permission.VIEW_REPORTS)).queueReport(report, SearchPagination())(fakeRequest))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).queueReport(report, SearchPagination())(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "getReportByName" should {
    "return 404 for unknown reports" in {
      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName("foo")(fakeRequest))

      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).getReportByName("foo")(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "showChangeTeamsFilter" should {
    val report = SummaryReport(
      name    = "Case count by status",
      groupBy = NonEmptySeq.one(ReportField.Status),
      sortBy  = ReportField.Status
    )

    "return 200 OK and HTML content type for a valid request" in {
      val result =
        await(
          controller(Set(Permission.VIEW_REPORTS))
            .showChangeTeamsFilter(report, SearchPagination())(newFakeGETRequestWithCSRF())
        )

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return unauthorised with no permissions" in {
      val result =
        await(controller(Set()).showChangeTeamsFilter(report, SearchPagination())(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "postChangeTeamsFilter" should {
    val summaryReport = SummaryReport(
      name    = "Case count by status",
      groupBy = NonEmptySeq.one(ReportField.Status),
      sortBy  = ReportField.Status,
      teams   = Set("2", "3")
    )

    val caseReport = CaseReport(
      name   = "ATaR Summary Report",
      fields = NonEmptySeq.of(ReportField.Reference, ReportField.GoodsName, ReportField.TraderName)
    )

    val queueReport = QueueReport()

    "return 303 and redirect to appropriate page when all teams is selected" in {
      val operator = Operator("0", Some("name"), memberOfTeams = Seq("4", "5"))

      val request = newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("allTeams" -> "true")

      val summaryResult = await(
        controller(Set(Permission.VIEW_REPORTS), operator)
          .postChangeTeamsFilter(summaryReport, SearchPagination())(request)
      )

      status(summaryResult) shouldBe Status.SEE_OTHER
      redirectLocation(summaryResult) shouldBe Some(
        routes.ReportingController.summaryReport(summaryReport.copy(teams = Set.empty)).path()
      )

      val caseResult =
        await(
          controller(Set(Permission.VIEW_REPORTS), operator)
            .postChangeTeamsFilter(caseReport, SearchPagination())(request)
        )

      status(caseResult) shouldBe Status.SEE_OTHER
      redirectLocation(caseResult) shouldBe Some(
        routes.ReportingController.caseReport(caseReport.copy(teams = Set.empty)).path()
      )

      val queueResult =
        await(
          controller(Set(Permission.VIEW_REPORTS), operator)
            .postChangeTeamsFilter(queueReport, SearchPagination())(request)
        )

      status(queueResult) shouldBe Status.SEE_OTHER
      redirectLocation(queueResult) shouldBe Some(
        routes.ReportingController.queueReport(queueReport.copy(teams = Set.empty)).path()
      )
    }

    "return 303 and redirect to appropriate page when managed teams is selected" in {
      val operator = Operator("0", Some("name"), memberOfTeams = Seq("4", "5"))

      val request = newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("allTeams" -> "false")

      val summaryResult = await(
        controller(Set(Permission.VIEW_REPORTS), operator)
          .postChangeTeamsFilter(summaryReport, SearchPagination())(request)
      )

      status(summaryResult) shouldBe Status.SEE_OTHER
      redirectLocation(summaryResult) shouldBe Some(
        routes.ReportingController.summaryReport(summaryReport.copy(teams = Set("4", "5"))).path()
      )

      val caseResult =
        await(
          controller(Set(Permission.VIEW_REPORTS), operator)
            .postChangeTeamsFilter(caseReport, SearchPagination())(request)
        )

      status(caseResult) shouldBe Status.SEE_OTHER
      redirectLocation(caseResult) shouldBe Some(
        routes.ReportingController.caseReport(caseReport.copy(teams = Set("4", "5"))).path()
      )

      val queueResult =
        await(
          controller(Set(Permission.VIEW_REPORTS), operator)
            .postChangeTeamsFilter(queueReport, SearchPagination())(request)
        )

      status(queueResult) shouldBe Status.SEE_OTHER
      redirectLocation(queueResult) shouldBe Some(
        routes.ReportingController.queueReport(queueReport.copy(teams = Set("4", "5"))).path()
      )
    }

    "return 400 and HTML content type for an invalid request" in {
      val request = newFakePOSTRequestWithCSRF().withFormUrlEncodedBody()
      val result =
        await(
          controller(Set(Permission.VIEW_REPORTS)).postChangeTeamsFilter(summaryReport, SearchPagination())(request)
        )

      status(result)          shouldBe Status.BAD_REQUEST
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("reporting.choose_teams.required"))
    }

    "return unauthorised with no permissions" in {
      val result =
        await(
          controller(Set())
            .postChangeTeamsFilter(summaryReport, SearchPagination())(newFakePOSTRequestWithCSRF())
        )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "showChangeDateFilter" should {
    val report = SummaryReport(
      name    = "Case count by status",
      groupBy = NonEmptySeq.one(ReportField.Status),
      sortBy  = ReportField.Status
    )

    "return 200 OK and HTML content type for a valid request" in {
      val result =
        await(
          controller(Set(Permission.VIEW_REPORTS))
            .showChangeDateFilter(report, SearchPagination())(newFakeGETRequestWithCSRF())
        )

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return unauthorised with no permissions" in {
      val result =
        await(controller(Set()).showChangeDateFilter(report, SearchPagination())(newFakeGETRequestWithCSRF()))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "postChangeDateFilter" should {
    val summaryReport = SummaryReport(
      name    = "Case count by status",
      groupBy = NonEmptySeq.one(ReportField.Status),
      sortBy  = ReportField.Status,
      dateRange = InstantRange(
        Instant.parse("2020-01-01T09:00:00.00Z"),
        Instant.parse("2021-01-01T09:00:00.00Z")
      )
    )

    val caseReport = CaseReport(
      name   = "ATaR Summary Report",
      fields = NonEmptySeq.of(ReportField.Reference, ReportField.GoodsName, ReportField.TraderName),
      dateRange = InstantRange(
        Instant.parse("2020-01-01T09:00:00.00Z"),
        Instant.parse("2021-01-01T09:00:00.00Z")
      )
    )

    val queueReport = QueueReport(
      dateRange = InstantRange(
        Instant.parse("2020-01-01T09:00:00.00Z"),
        Instant.parse("2021-01-01T09:00:00.00Z")
      )
    )

    "return 303 and redirect to appropriate page when no specific date range is selected" in {
      val request = newFakePOSTRequestWithCSRF().withFormUrlEncodedBody("specificDates" -> "false")

      val summaryResult = await(
        controller(Set(Permission.VIEW_REPORTS)).postChangeDateFilter(summaryReport, SearchPagination())(request)
      )

      status(summaryResult) shouldBe Status.SEE_OTHER
      redirectLocation(summaryResult) shouldBe Some(
        routes.ReportingController.summaryReport(summaryReport.copy(dateRange = InstantRange.allTime)).path()
      )

      val caseResult =
        await(controller(Set(Permission.VIEW_REPORTS)).postChangeDateFilter(caseReport, SearchPagination())(request))

      status(caseResult) shouldBe Status.SEE_OTHER
      redirectLocation(caseResult) shouldBe Some(
        routes.ReportingController.caseReport(caseReport.copy(dateRange = InstantRange.allTime)).path()
      )

      val queueResult =
        await(
          controller(Set(Permission.VIEW_REPORTS))
            .postChangeDateFilter(queueReport, SearchPagination())(request)
        )

      status(queueResult) shouldBe Status.SEE_OTHER
      redirectLocation(queueResult) shouldBe Some(
        routes.ReportingController.queueReport(queueReport.copy(dateRange = InstantRange.allTime)).path()
      )
    }

    "return 303 and redirect to appropriate page when a date range is selected" in {
      val request = newFakePOSTRequestWithCSRF()
        .withFormUrlEncodedBody(
          "specificDates"       -> "true",
          "dateRange.min.year"  -> "2021",
          "dateRange.min.month" -> "1",
          "dateRange.min.day"   -> "1",
          "dateRange.max.year"  -> "2022",
          "dateRange.max.month" -> "1",
          "dateRange.max.day"   -> "1"
        )

      val summaryResult = await(
        controller(Set(Permission.VIEW_REPORTS)).postChangeDateFilter(summaryReport, SearchPagination())(request)
      )

      status(summaryResult) shouldBe Status.SEE_OTHER

      redirectLocation(summaryResult) shouldBe Some(
        routes.ReportingController
          .summaryReport(
            summaryReport.copy(dateRange =
              InstantRange(Instant.parse("2021-01-01T00:00:00.00Z"), Instant.parse("2022-01-02T00:00:00.00Z"))
            )
          )
          .path()
      )

      val caseResult =
        await(controller(Set(Permission.VIEW_REPORTS)).postChangeDateFilter(caseReport, SearchPagination())(request))

      status(caseResult) shouldBe Status.SEE_OTHER

      redirectLocation(caseResult) shouldBe Some(
        routes.ReportingController
          .caseReport(
            caseReport.copy(dateRange =
              InstantRange(Instant.parse("2021-01-01T00:00:00.00Z"), Instant.parse("2022-01-02T00:00:00.00Z"))
            )
          )
          .path()
      )

      val queueResult =
        await(
          controller(Set(Permission.VIEW_REPORTS))
            .postChangeDateFilter(queueReport, SearchPagination())(request)
        )

      status(queueResult) shouldBe Status.SEE_OTHER
      redirectLocation(queueResult) shouldBe Some(
        routes.ReportingController
          .queueReport(
            queueReport.copy(dateRange =
              InstantRange(Instant.parse("2021-01-01T00:00:00.00Z"), Instant.parse("2022-01-02T00:00:00.00Z"))
            )
          )
          .path()
      )
    }

    "return 400 and HTML content type when nothing is selected" in {
      val request = newFakePOSTRequestWithCSRF().withFormUrlEncodedBody()
      val result =
        await(controller(Set(Permission.VIEW_REPORTS)).postChangeDateFilter(summaryReport, SearchPagination())(request))

      status(result)          shouldBe Status.BAD_REQUEST
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("reporting.choose_date.required"))
    }

    "return 400 and HTML content type when an invalid date is selected" in {
      val request = newFakePOSTRequestWithCSRF().withFormUrlEncodedBody(
        "specificDates"       -> "true",
        "dateRange.min.year"  -> "2021",
        "dateRange.min.month" -> "",
        "dateRange.min.day"   -> "1",
        "dateRange.max.year"  -> "2022",
        "dateRange.max.month" -> "1",
        "dateRange.max.day"   -> "1"
      )
      val result =
        await(controller(Set(Permission.VIEW_REPORTS)).postChangeDateFilter(summaryReport, SearchPagination())(request))

      status(result)          shouldBe Status.BAD_REQUEST
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("reporting.startDate.error.required.one", "month"))
    }

    "return 400 and HTML content type when end date is before start date" in {
      val request = newFakePOSTRequestWithCSRF().withFormUrlEncodedBody(
        "specificDates"       -> "true",
        "dateRange.min.year"  -> "2022",
        "dateRange.min.month" -> "1",
        "dateRange.min.day"   -> "1",
        "dateRange.max.year"  -> "2021",
        "dateRange.max.month" -> "1",
        "dateRange.max.day"   -> "1"
      )
      val result =
        await(controller(Set(Permission.VIEW_REPORTS)).postChangeDateFilter(summaryReport, SearchPagination())(request))

      status(result)          shouldBe Status.BAD_REQUEST
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("reporting.choose_date.invalid_end_date"))
    }

    "return unauthorised with no permissions" in {
      val result =
        await(
          controller(Set())
            .postChangeDateFilter(summaryReport, SearchPagination())(newFakePOSTRequestWithCSRF())
        )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "displayManageReporting" should {

    "return 200 OK and HTML content type" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).displayManageReporting()(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).displayManageReporting()(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "correspondenceCaseCount" should {

    val reportName = "correspondence-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.correspondenceCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "miscellaneousCaseCount" should {

    val reportName = "miscellaneous-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.miscellaneousCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "numberOfOpenCases" should {

    val reportName = "number-of-open-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.numberOfOpenCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "completedCasesByTeam" should {

    val reportName = "completed-cases-by-team"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.completedCasesByTeam).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "completedCasesByAssignedUser" should {

    val reportName = "completed-cases-by-assigned-user"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.completedCasesByAssignedUser).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "numberOfCasesPerUser" should {

    val reportName = "number-of-cases-per-user"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.numberOfCasesPerUser).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "cancelledCasesPerUser" should {

    val reportName = "cancelled-cases-by-assigned-user"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.cancelledCasesPerUser).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "casesDueToExpire" should {

    val reportName = "cases-due-to-expire"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.casesDueToExpire).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "cancelledCasesByChapter" should {

    val reportName = "cancelled-cases-by-chapter"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.cancelledCasesByChapter).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "liabilitiesSummary" should {

    val reportName = "liabilities-summary"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.liabilitiesSummary).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "atarSummary" should {

    val reportName = "atar-summary"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.atarSummary).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "numberOfNewAtarCases" should {

    val reportName = "new-atar-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.numberOfNewAtarCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "liabilitiesCases" should {

    val reportName = "liabilities-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.liabilitiesCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "numberOfNewCases" should {

    val reportName = "number-of-new-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.numberOfNewCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "newAndOpenCases" should {

    val reportName = "new-and-open-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.numberOfNewAndOpenCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "numberOfNewLiabilityCases" should {

    val reportName = "new-liabilities"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.numberOfNewLiabilityCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "numberOfNewLiveLiabilityCases" should {

    val reportName = "new-liabilities-cases-live"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.numberOfNewLiveLiabilityCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "numberOfNewNonLiveLiabilityCases" should {

    val reportName = "new-liabilities-cases-non-live"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.numberOfNewNonLiveLiabilityCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "calendarDaysNonLiveLiabilitiesCases" should {

    val reportName = "working-days-non-live-liabilities"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.calendarDaysNonLiveLiabilitiesCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "openCasesInTeams" should {

    val reportName = "number-of-cases-in-teams"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.queueReport(Report.openCasesInTeams).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "caseCountByStatus" should {

    val reportName = "case-count-by-status"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.caseCountByStatus).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "suppressedCaseStatus" should {

    val reportName = "suppressed-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.suppressedCaseCount).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "openCases" should {

    val reportName = "open-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.openCasesCount).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "rejectedBreakdownByUser" should {

    val reportName = "rejection-breakdown"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.rejectedCaseCountByUser).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "120CalendarDaysForAtar" should {

    val reportName = "calendar-days-atar-cases"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.caseReport(Report.calendarAtarCases).path()
      )
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }

  }

  "casesUnderReviewByChapter" should {

    val reportName = "under-review-cases-by-chapter"

    "return 303 SEE_OTHER and redirect to correct report url" in {
      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.casesUnderReviewByChapter).path()
      )
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "casesUnderReviewByUser" should {

    val reportName = "under-review-cases-by-assigned-user"

    "return 303 SEE_OTHER and redirect to correct report url" in {
      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.casesUnderReviewByUser).path()
      )
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "casesUnderAppealByChapter" should {

    val reportName = "under-appeal-cases-by-chapter"

    "return 303 SEE_OTHER and redirect to correct report url" in {
      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.casesUnderAppealByChapter).path()
      )
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

  "casesUnderAppealByUser" should {

    val reportName = "under-appeal-cases-by-assigned-user"

    "return 303 SEE_OTHER and redirect to correct report url" in {
      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ReportingController.summaryReport(Report.casesUnderAppealByUser).path()
      )
    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized().url)
    }
  }

}
