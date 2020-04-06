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

package views.partials.liabilities.status

import models._
import views.ViewMatchers.containText
import views.ViewSpec
import views.html.partials.liabilities.sample.sample_status_events_table

class SampleStatusEventsTableViewSpec extends ViewSpec {

  val sampleStatusChangeEvents: Seq[Event] = Seq(Event("1", SampleStatusChange(Some(SampleStatus.AWAITING), Some(SampleStatus.DESTROYED), None), Operator("1"), "1"))
  val sampleStatusReturnChangeEvents: Seq[Event] = Seq(Event("1", SampleReturnChange(Some(SampleReturn.YES), Some(SampleReturn.NO), None), Operator("1"), "1"))

  def sampleStatusEventsTableView: sample_status_events_table = app.injector.instanceOf[sample_status_events_table]


  "sample_details_liability view" should {

    "render correctly" in {
      val doc = view(sampleStatusEventsTableView(Paged(sampleStatusChangeEvents), 0))
      doc.getElementById("sample-status-events-heading") should containText("Sample activity")
    }

    "render status change events correctly" in {
      val doc = view(sampleStatusEventsTableView(Paged(sampleStatusChangeEvents), 0))
      doc.getElementById("sample-status-events-row-0") should containText("Sample status changed from awaiting sample to destroyed")
    }

    "render status return events correctly" in {
      val doc = view(sampleStatusEventsTableView(Paged(sampleStatusReturnChangeEvents), 0))
      doc.getElementById("sample-status-events-row-0") should containText("Returning sample changed from yes to no")
    }

  }


}
