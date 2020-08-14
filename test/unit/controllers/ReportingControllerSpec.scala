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

package controllers

import java.time.Instant

import models.forms.InstantRangeForm
import models.request.AuthenticatedRequest
import models.{Permission, _}
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.mvc.{AnyContent, Request}
import play.api.test.Helpers._
import service._
import uk.gov.hmrc.http.HeaderCarrier
import views.{Report, SelectedReport}

import scala.concurrent.Future

class ReportingControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val reportingService = mock[ReportingService]
  private val queueService = mock[QueuesService]
  private val casesService = mock[CasesService]
  private val operator = mock[Operator]
  private val requiredPermissions: Set[Permission] = Set(Permission.VIEW_REPORTS)
  private val noPermissions: Set[Permission] = Set.empty

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(
      reportingService,
      queueService,
      casesService,
      operator
    )
  }

  private def controller(permission: Set[Permission]) = new ReportingController(
    new RequestActionsWithPermissions(defaultPlayBodyParsers, permission), reportingService, queueService, casesService,  mcc, realAppConfig
  )

  private def request[A](operator: Operator, request: Request[A]) = new AuthenticatedRequest(operator, request)

  "GET Reports" should {
    "Return OK" in {
      given(queueService.getAll) willReturn Future.successful(Seq.empty)
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(operator.hasPermissions(requiredPermissions)) willReturn true

      val req: AuthenticatedRequest[AnyContent] = request(operator, newFakeGETRequestWithCSRF(app))

      val result = await(controller(requiredPermissions).getReports(req.request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) shouldBe views.html.reports(Seq.empty, None, Map.empty)(req, mcc.messagesApi.preferred(req), realAppConfig).toString()
    }

    "Return Forbidden for Non-Manager" in {
      given(queueService.getAll) willReturn Future.successful(Seq.empty)

      val req: AuthenticatedRequest[AnyContent] = request(operator, newFakeGETRequestWithCSRF(app))
      val result = await(controller(noPermissions).getReports(req.request))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }
  }

  "GET Report Criteria" should {
    "Return OK for SLA Report" in {
      given(queueService.getAll) willReturn Future.successful(Seq.empty)
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(operator.hasPermissions(requiredPermissions)) willReturn true

      val req: AuthenticatedRequest[AnyContent] = request(operator, newFakeGETRequestWithCSRF(app))
      val result = await(controller(requiredPermissions).getReportCriteria(Report.SLA.toString,0)(req.request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      contentAsString(result) shouldBe views.html.reports(
        Seq.empty,
        Some(SelectedReport(
          Report.SLA,
          views.html.partials.reports.sla_report_criteria(InstantRangeForm.form)(req, mcc.messagesApi.preferred(req), realAppConfig))
        ),
        Map.empty)(req, mcc.messagesApi.preferred(req), realAppConfig).toString()
    }

    "Return OK for Referral Report" in {
      given(queueService.getAll) willReturn Future.successful(Seq.empty)
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(operator.hasPermissions(requiredPermissions)) willReturn true

      val req: AuthenticatedRequest[AnyContent] = request(operator, newFakeGETRequestWithCSRF(app))
      val result = await(controller(requiredPermissions).getReportCriteria(Report.REFERRAL.toString,0)(req.request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      contentAsString(result) shouldBe views.html.reports(
        Seq.empty,
        Some(SelectedReport(
          Report.REFERRAL,
          views.html.partials.reports.referral_report_criteria(InstantRangeForm.form)(req, mcc.messagesApi.preferred(req), realAppConfig))
        ), Map.empty)(req, mcc.messagesApi.preferred(req), realAppConfig).toString()
    }

    "Redirect to Reports for Not Found" in {
      given(operator.hasPermissions(requiredPermissions)) willReturn true

      val req: AuthenticatedRequest[AnyContent] = request(operator, newFakeGETRequestWithCSRF(app))
      val result = await(controller(requiredPermissions).getReportCriteria("xyz",0)(req.request))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/reports")
    }

    "Return Forbidden for Non-Manager" in {
      given(queueService.getAll) willReturn Future.successful(Seq.empty)

      val req: AuthenticatedRequest[AnyContent] = request(operator, newFakeGETRequestWithCSRF(app))
      val result = await(controller(noPermissions).getReportCriteria(Report.SLA.toString,0)(req.request))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }
  }

  "GET Report" should {
    val startDate = Instant.EPOCH
    val endDate = Instant.EPOCH.plusSeconds(86400)
    val range = InstantRange(startDate, endDate)

    "Return OK for SLA Report" in {

      given(queueService.getNonGateway) willReturn Future.successful(Seq.empty[Queue])
      given(reportingService.getSLAReport(refEq(range))(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[ReportResult])
      given(operator.hasPermissions(requiredPermissions)) willReturn true

      val req: AuthenticatedRequest[AnyContent] = request(
        operator,
        newFakeGETRequestWithCSRF(app)
          .withFormUrlEncodedBody(
            "min.day" -> "1", "min.month" -> "1", "min.year" -> "1970",
            "max.day" -> "2", "max.month" -> "1", "max.year" -> "1970"
          )
      )
      val result = await(controller(requiredPermissions).getReport(Report.SLA.toString)(req.request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      contentAsString(result) shouldBe views.html.report_sla(range, Seq.empty[ReportResult], Seq.empty[Queue])(req, mcc.messagesApi.preferred(req), realAppConfig).toString()
    }

    "Return OK for Referral Report" in {
      given(queueService.getNonGateway) willReturn Future.successful(Seq.empty[Queue])
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(reportingService.getReferralReport(refEq(range))(any[HeaderCarrier])) willReturn Future.successful(Seq.empty[ReportResult])

      val req: AuthenticatedRequest[AnyContent] = request(
        operator,
        newFakeGETRequestWithCSRF(app)
          .withFormUrlEncodedBody(
            "min.day" -> "1", "min.month" -> "1", "min.year" -> "1970",
            "max.day" -> "2", "max.month" -> "1", "max.year" -> "1970"
          )
      )
      val result = await(controller(requiredPermissions).getReport(Report.REFERRAL.toString)(req.request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      contentAsString(result) shouldBe views.html.report_referral(range, Seq.empty[ReportResult], Seq.empty[Queue])(req, mcc.messagesApi.preferred(req), realAppConfig).toString()
    }

    "Return Bad Request for missing params" in {
      given(queueService.getAll) willReturn Future.successful(Seq.empty)
      given(casesService.countCasesByQueue(any[Operator])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, Int]))
      given(operator.hasPermissions(requiredPermissions)) willReturn true

      val req: AuthenticatedRequest[AnyContent] = request(operator, newFakeGETRequestWithCSRF(app))
      val result = await(controller(requiredPermissions).getReport(Report.SLA.toString)(req.request))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      contentAsString(result) shouldBe views.html.reports(
        Seq.empty,
        Some(SelectedReport(
          Report.SLA,
          views.html.partials.reports.sla_report_criteria(InstantRangeForm.form.bind(Map[String, String]()))(req, mcc.messagesApi.preferred(req), realAppConfig))
        ),
        Map.empty)(req, mcc.messagesApi.preferred(req), realAppConfig).toString()
    }

    "Redirect to Reports for Not Found" in {
      given(operator.hasPermissions(requiredPermissions)) willReturn true

      val req: AuthenticatedRequest[AnyContent] = request(operator, newFakeGETRequestWithCSRF(app))
      val result = await(controller(requiredPermissions).getReport("xyz")(req.request))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/reports")
    }

    "Return Forbidden for Non-Manager" in {
      given(queueService.getAll) willReturn Future.successful(Seq.empty)

      val req: AuthenticatedRequest[AnyContent] = request(operator, newFakeGETRequestWithCSRF(app))
      val result = await(controller(noPermissions).getReport(Report.SLA.toString)(req.request))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }
  }
}
