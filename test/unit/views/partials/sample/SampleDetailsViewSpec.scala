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

package views.partials.sample

import models._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.sample.{sample_details, sample_details_bti}
import utils.Cases._

class SampleDetailsViewSpec extends ViewSpec {

  "Sample Details" should {

    "render bti details" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails()
      )

      // When
      val doc = view(sample_details(caseWithSample,Paged.empty[Event]))

      // Then
      doc should containElementWithID("app-details-sending-samples")
      doc shouldNot containElementWithID("liability-sending-samples")
    }

    "render liability details" in {
      // Given
      val `case` = aCase(
        withLiabilityApplication()
      )

      // When
      val doc = view(sample_details(`case`,Paged.empty[Event]))

      // Then
      doc should containElementWithID("liability-sending-samples")
      doc shouldNot containElementWithID("app-details-sending-samples")
    }

    "render sample status activity when present on case" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = true, sampleToBeReturned = false),
        withoutAttachments()
      )

      val event1 : Event = Event("1",SampleStatusChange(None, Some(SampleStatus.AWAITING), None),Operator("1"),"1")
      val event2 : Event = Event("2",SampleStatusChange(Some(SampleStatus.AWAITING), Some(SampleStatus.MOVED_TO_ELM), None),Operator("1"),"1")
      val event3 : Event = Event("3",SampleStatusChange(Some(SampleStatus.MOVED_TO_ELM), Some(SampleStatus.DESTROYED), None),Operator("1"),"1")
      val sampleStatusActivity : Seq[Event] = Seq(event1,event2,event3)

      // When
      val doc = view(sample_details(caseWithSample,Paged.apply(sampleStatusActivity,NoPagination(),3)))

      doc.getElementById("sample-status-events-row-0-title") should containText("Sample status changed from none to awaiting sample")
      doc.getElementById("sample-status-events-row-1-title") should containText("Sample status changed from awaiting sample to moved to ELM")
      doc.getElementById("sample-status-events-row-2-title") should containText("Sample status changed from moved to ELM to destroyed")
    }

    "render sample status event as sending sample for liability" in {
      // Given
      val caseWithSample = aCase(
        withLiabilityApplication()
      )

      val event1 : Event = Event("1",SampleStatusChange(None, Some(SampleStatus.AWAITING), None),Operator("1"),"1")
      val sampleStatusActivity : Seq[Event] = Seq(event1)

      // When
      val doc = view(sample_details(caseWithSample,Paged.apply(sampleStatusActivity,NoPagination(),3)))

      doc.getElementById("sample-status-events-row-0-title") should containText("Sending sample changed from no to yes")
    }

  }

}
