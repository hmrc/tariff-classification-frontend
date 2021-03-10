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

package views.managementtools

import models._
import models.reporting._
import views.ViewMatchers._
import views.ViewSpec
import views.html.managementtools.queueReportTable

class QueueReportTableViewSpec extends ViewSpec {

  "queueReportTable view" should {
    val report = QueueReport()

    val reportResults: Paged[QueueResultGroup] = Paged(Seq(
      QueueResultGroup(4, None, ApplicationType.ATAR),
      QueueResultGroup(3, None, ApplicationType.LIABILITY),
      QueueResultGroup(7, None, ApplicationType.CORRESPONDENCE),
      QueueResultGroup(1, None, ApplicationType.MISCELLANEOUS),
      QueueResultGroup(8, Some("2"), ApplicationType.ATAR),
      QueueResultGroup(5, Some("2"), ApplicationType.LIABILITY),
      QueueResultGroup(1, Some("3"), ApplicationType.CORRESPONDENCE),
      QueueResultGroup(2, Some("3"), ApplicationType.MISCELLANEOUS),
    ))

    "render a header for each field" in {
      val doc = view(queueReportTable(report, SearchPagination(), reportResults, Queues.allQueuesById, "queue-report"))
      doc should containElementWithID(s"queue-report-assigned_team")
      doc should containElementWithID(s"queue-report-case_type")
      doc should containElementWithID(s"queue-report-count")
      doc.getElementById("queue-report-assigned_team") should containText(messages("reporting.field.assigned_team"))
      doc.getElementById("queue-report-case_type") should containText(messages("reporting.field.case_type"))
      doc.getElementById("queue-report-count") should containText(messages("reporting.field.count"))
    }

    "render data for each row" in {
      val doc = view(queueReportTable(report, SearchPagination(), reportResults, Queues.allQueuesById, "queue-report"))
      for ((row, idx) <- reportResults.results.zipWithIndex) {
        doc should containElementWithID(s"queue-report-assigned_team-${idx}")
        doc should containElementWithID(s"queue-report-case_type-${idx}")
        doc should containElementWithID(s"queue-report-count-${idx}")
      }

      doc.getElementById("queue-report-assigned_team-0") should containText("Gateway")
      doc.getElementById("queue-report-case_type-0") should containText("ATaR")
      doc.getElementById("queue-report-count-0") should containText("4")

      doc.getElementById("queue-report-assigned_team-1") should containText("Gateway")
      doc.getElementById("queue-report-case_type-1") should containText("Liability")
      doc.getElementById("queue-report-count-1") should containText("3")

      doc.getElementById("queue-report-assigned_team-2") should containText("Gateway")
      doc.getElementById("queue-report-case_type-2") should containText("Correspondence")
      doc.getElementById("queue-report-count-2") should containText("7")

      doc.getElementById("queue-report-assigned_team-3") should containText("Gateway")
      doc.getElementById("queue-report-case_type-3") should containText("Miscellaneous")
      doc.getElementById("queue-report-count-3") should containText("1")

      doc.getElementById("queue-report-assigned_team-4") should containText("ACT")
      doc.getElementById("queue-report-case_type-4") should containText("ATaR")
      doc.getElementById("queue-report-count-4") should containText("8")

      doc.getElementById("queue-report-assigned_team-5") should containText("ACT")
      doc.getElementById("queue-report-case_type-5") should containText("Liability")
      doc.getElementById("queue-report-count-5") should containText("5")

      doc.getElementById("queue-report-assigned_team-6") should containText("CAP")
      doc.getElementById("queue-report-case_type-6") should containText("Correspondence")
      doc.getElementById("queue-report-count-6") should containText("1")

      doc.getElementById("queue-report-assigned_team-7") should containText("CAP")
      doc.getElementById("queue-report-case_type-7") should containText("Miscellaneous")
      doc.getElementById("queue-report-count-7") should containText("2")
    }
  }
}
