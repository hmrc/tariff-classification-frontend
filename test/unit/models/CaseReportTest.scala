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

package models

import java.net.URLDecoder
import java.time.Instant

import play.api.mvc.QueryStringBindable.bindableString

class CaseReportTest extends ModelsBaseSpec {

  private val report = CaseReport(
    filter = CaseReportFilter(
      decisionStartDate = Some(InstantRange(
        min = Instant.EPOCH,
        max  = Instant.EPOCH.plusSeconds(1)
      )),
      referralDate = Some(InstantRange(
        min = Instant.EPOCH,
        max  = Instant.EPOCH.plusSeconds(1)
      )),
      status = Some(Set("OPEN")),
      applicationType = Some(Set("BTI"))
    ),
    field = CaseReportField.ACTIVE_DAYS_ELAPSED,
    group = Set(CaseReportGroup.QUEUE)
  )

  private val params: Map[String, Seq[String]] = Map(
    "min_decision_start" -> Seq("1970-01-01T00:00:00Z"),
    "max_decision_start" -> Seq("1970-01-01T00:00:01Z"),
    "min_referral_date" -> Seq("1970-01-01T00:00:00Z"),
    "max_referral_date" -> Seq("1970-01-01T00:00:01Z"),
    "status"  -> Seq("OPEN"),
    "application_type" -> Seq("BTI"),
    "report_field" -> Seq("active-days-elapsed"),
    "report_group" -> Seq("queue-id")
  )

  /**
    * When we add fields to Report these tests shouldn't need changing, only the fields above.
    **/
  "Report Binder" should {

    "Unbind Populated Search to Query String" in {
      val populatedQueryParam: String =
        "min_decision_start=1970-01-01T00:00:00Z&" +
          "max_decision_start=1970-01-01T00:00:01Z&" +
          "min_referral_date=1970-01-01T00:00:00Z&" +
          "max_referral_date=1970-01-01T00:00:01Z&" +
          "status=OPEN&" +
          "application_type=BTI&" +
          "report_group=queue-id&" +
          "report_field=active-days-elapsed"
      URLDecoder.decode(CaseReport.bindable.unbind("", report), "UTF-8") shouldBe populatedQueryParam
    }

    "Bind empty query string" in {
      CaseReport.bindable.bind("", Map()) shouldBe Some(Left("Invalid Field/Group"))
    }

    "Bind query string with empty field" in {
      CaseReport.bindable.bind("", params.filterNot(_._1 == "report_field")) shouldBe Some(Left("Invalid Field"))
    }

    "Bind query string with empty group" in {
      CaseReport.bindable.bind("", params.filterNot(_._1 == "report_group")) shouldBe Some(Left("Invalid Group"))
    }

    "Bind populated query string" in {
      CaseReport.bindable.bind("", params) shouldBe Some(Right(report))
    }
  }

}
