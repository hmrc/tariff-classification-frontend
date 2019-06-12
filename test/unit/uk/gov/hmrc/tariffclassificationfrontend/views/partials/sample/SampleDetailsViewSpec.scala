/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials.sample

import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.sample.sample_details
import uk.gov.tariffclassificationfrontend.utils.Cases._

class SampleDetailsViewSpec extends ViewSpec {

  "Sample Details" should {

    "render sample to be returned when sample provided" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = true, sampleToBeReturned = true),
        withoutAttachments()
      )

      // When
      val doc = view(sample_details(caseWithSample,Paged.empty[Event]))

      // Then
      doc.getElementById("app-details-sending-samples") should containText(messages("answer.yes"))
      doc.getElementById("app-details-returning-samples") should containText(messages("answer.yes"))
    }

    "not render sample to be returned when sample not provided" in {
      // Given
      val `case` = aCase(
        withBTIDetails(sampleToBeReturned = true),
        withoutAttachments()
      )

      // When
      val doc = view(sample_details(`case`,Paged.empty[Event]))

      // Then
      doc.getElementById("app-details-sending-samples") should containText(messages("answer.no"))
      doc shouldNot containElementWithID("app-details-returning-samples")
    }

    "render sample status details when present on case" in {
      // Given
      val caseWithSample = aCase(
        withSampleStatus(Some(SampleStatus.AWAITING)),
        withBTIDetails(sampleToBeProvided = true, sampleToBeReturned = true),
        withoutAttachments()
      )

      // When
      val doc = view(sample_details(caseWithSample,Paged.empty[Event]))

      doc.getElementById("sample-status-value") should containText(SampleStatus.format(Some(SampleStatus.AWAITING)))
    }

    "not render sample status details of when sample not being provided" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = false, sampleToBeReturned = false),
        withoutAttachments()
      )

      // When
      val doc = view(sample_details(caseWithSample,Paged.empty[Event]))

      doc shouldNot containElementWithID("sample-status-value")
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

    "not render sample status activity when sample not being provided" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = false, sampleToBeReturned = false),
        withoutAttachments()
      )
      // When
      val doc = view(sample_details(caseWithSample,Paged.empty[Event]))

      doc shouldNot containElementWithID("sample-status-events-heading")
    }

    "render sample requested when present on case" in {
      // Given
      val caseWithSample = aCase(
        withSampleRequested(Some(Operator("id", name = Some("Tester Op"))), Some(SampleReturn.TO_BE_CONFIRMED))
      )

      // When
      val doc = view(sample_details(caseWithSample,Paged.empty[Event]))

      doc.getElementById("sample-requested-by") should containText("Tester Op")
      doc.getElementById("sample-requested-return") should containText(SampleReturn.format(Some(SampleReturn.TO_BE_CONFIRMED)))
    }

    "not render sample requested when not present on case" in {
      // Given
      val caseWithSample = aCase(
        withSampleRequested(None, None)
      )

      // When
      val doc = view(sample_details(caseWithSample,Paged.empty[Event]))

      doc shouldNot containElementWithID("sample-requested-by")
      doc shouldNot containElementWithID("sample-requested-return")
    }
  }

}
