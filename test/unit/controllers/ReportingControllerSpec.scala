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

package controllers

import java.time.Instant
import models.forms.InstantRangeForm
import models.reporting.Report
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
import views.html.managementtools.manage_reports_view

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ReportingControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val reportingService                     = mock[ReportingService]
  private val queueService                         = mock[QueuesService]
  private val usersService                         = mock[UserService]
  private val casesService                         = mock[CasesService]
  private val operator                             = mock[Operator]
  private val requiredPermissions: Set[Permission] = Set(Permission.VIEW_REPORTS)
  private val noPermissions: Set[Permission]       = Set.empty
  private lazy val manage_reports_view             = injector.instanceOf[manage_reports_view]


  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(
      reportingService,
      queueService,
      usersService,
      casesService,
      operator
    )
  }

  private def controller(permission: Set[Permission]) = new ReportingController(
    new RequestActionsWithPermissions(playBodyParsers, permission),
    reportingService,
    queueService,
    usersService,
    casesService,
    mcc,
    manage_reports_view,
    realAppConfig
  )

  private def request[A](operator: Operator, request: Request[A]) = new AuthenticatedRequest(operator, request)

  // TODO: Add test coverage for new reports

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
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)
    }
  }

  "caseCountByStatus" should {

    val reportName = "case-count-by-status"

    "return 303 SEE_OTHER and redirect to correct report url" in {

      val result = await(controller(Set(Permission.VIEW_REPORTS)).getReportByName(reportName)(fakeRequest))
      status(result)      shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ReportingController.summaryReport(Report.caseCountByStatus).path())
    }

    "return unauthorised with no permissions" in {

      val result = await(controller(Set()).getReportByName(reportName)(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)
    }

  }

}
