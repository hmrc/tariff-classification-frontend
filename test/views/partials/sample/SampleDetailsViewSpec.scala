/*
 * Copyright 2025 HM Revenue & Customs
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

package views.partials.sample

import models._
import models.viewmodels.atar.SampleTabViewModel
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.sample.sample_details

class SampleDetailsViewSpec extends ViewSpec {

  "Sample Details" should {

    "render bti details" in {

      val caseWithSample = aCase(
        withBTIDetails()
      )

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged.empty)

      val doc = view(sample_details(sampleTab))

      doc should containElementWithID("app-details-sending-samples-answer")
      doc shouldNot containElementWithID("liability-sending-samples")
    }

    "render sample status activity when present on case" in {

      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = true),
        withoutAttachments()
      )

      val event1: Event = Event("1", SampleStatusChange(None, Some(SampleStatus.AWAITING), None), Operator("1"), "1")
      val event2: Event = Event(
        "2",
        SampleStatusChange(Some(SampleStatus.AWAITING), Some(SampleStatus.MOVED_TO_ELM), None),
        Operator("1"),
        "1"
      )
      val event3: Event = Event(
        "3",
        SampleStatusChange(Some(SampleStatus.MOVED_TO_ELM), Some(SampleStatus.DESTROYED), None),
        Operator("1"),
        "1"
      )
      val sampleStatusActivity: Seq[Event] = Seq(event1, event2, event3)

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged(sampleStatusActivity, NoPagination(), 3))

      val doc = view(sample_details(sampleTab))

      doc.getElementById("sample-status-events-row-0-title") should containText(
        "Sample status changed from none to awaiting sample"
      )
      doc.getElementById("sample-status-events-row-1-title") should containText(
        "Sample status changed from awaiting sample to moved to ELM"
      )
      doc.getElementById("sample-status-events-row-2-title") should containText(
        "Sample status changed from moved to ELM to destroyed"
      )
    }

  }

}
