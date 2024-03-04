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

package views.managementtools

import cats.data.NonEmptySeq
import models._
import models.reporting._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.caseReportTable

class CaseReportTableViewSpec extends ViewSpec {

  "caseReportTable view" should {
    val report = CaseReport(
      "ATaR Summary Report",
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

    "render a header for each field" in {
      val doc = view(caseReportTable(report, SearchPagination(), reportResults, Map.empty, Map.empty, "case-report"))
      for (field <- report.fields.toSeq) {
        doc should containElementWithID(s"case-report-${field.fieldName}")
      }
      doc.getElementById("case-report-reference")   should containText(messages("reporting.field.reference"))
      doc.getElementById("case-report-goods_name")  should containText(messages("reporting.field.goods_name"))
      doc.getElementById("case-report-trader_name") should containText(messages("reporting.field.trader_name"))
    }

    "render data for each row" in {
      val doc = view(caseReportTable(report, SearchPagination(), reportResults, Map.empty, Map.empty, "case-report"))
      for ((_, idx) <- reportResults.results.zipWithIndex) {
        doc should containElementWithID(s"case-report-details-$idx")
        doc should containElementWithID(s"case-report-reference-$idx")
        doc should containElementWithID(s"case-report-goods_name-$idx")
        doc should containElementWithID(s"case-report-trader_name-$idx")
      }

      doc.getElementById("case-report-reference-0")   should containText("123456")
      doc.getElementById("case-report-goods_name-0")  should containText("Fireworks")
      doc.getElementById("case-report-trader_name-0") should containText("Gandalf")

      doc.getElementById("case-report-reference-1")   should containText("987654")
      doc.getElementById("case-report-goods_name-1")  should containText("Beer")
      doc.getElementById("case-report-trader_name-1") should containText("Barliman Butterbur")
    }
  }
}
