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
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.sample.sample_details_bti
import models.viewmodels.atar.SampleTabViewModel

class SampleDetailsBTIViewSpec extends ViewSpec {

  "Sample Details" should {

    "render sample to be returned when sample provided" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = true, sampleToBeReturned = true),
        withoutAttachments()
      )

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged.empty)

      // When
      val doc = view(sample_details_bti(sampleTab))

      // Then
      doc.getElementById("app-details-sending-samples")   should containText(messages("answer.yes"))
      doc.getElementById("app-details-returning-samples") should containText(messages("answer.yes"))
    }

    "not render sample to be returned when sample not provided" in {
      // Given
      val `case` = aCase(
        withBTIDetails(sampleToBeReturned = true),
        withoutAttachments()
      )

      val sampleTab = SampleTabViewModel.fromCase(`case`, Paged.empty)

      // When
      val doc = view(sample_details_bti(sampleTab))

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

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged.empty)

      // When
      val doc = view(sample_details_bti(sampleTab))

      doc.getElementById("sample-status-value") should containText(SampleStatus.format(Some(SampleStatus.AWAITING)))
    }

    "not render sample status details of when sample not being provided" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = false, sampleToBeReturned = false),
        withoutAttachments()
      )

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged.empty)

      // When
      val doc = view(sample_details_bti(sampleTab))

      doc shouldNot containElementWithID("sample-status-value")
    }

    "not render sample status activity when sample not being provided" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = false, sampleToBeReturned = false),
        withoutAttachments()
      )

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged.empty)

      // When
      val doc = view(sample_details_bti(sampleTab))

      doc shouldNot containElementWithID("sample-status-events-heading")
    }

    "render sample requested when present on case" in {
      // Given
      val caseWithSample = aCase(
        withSampleRequested(Some(Operator("id", name = Some("Tester Op"))), Some(SampleReturn.TO_BE_CONFIRMED))
      )

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged.empty)

      // When
      val doc = view(sample_details_bti(sampleTab))

      doc.getElementById("sample-requested-by") should containText("Tester Op")
      doc.getElementById("sample-requested-return") should containText(
        SampleReturn.format(Some(SampleReturn.TO_BE_CONFIRMED))
      )
    }

    "not render sample requested when not present on case" in {
      // Given
      val caseWithSample = aCase(
        withSampleRequested(None, None)
      )

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged.empty)

      // When
      val doc = view(sample_details_bti(sampleTab))

      doc shouldNot containElementWithID("sample-requested-by")
      doc shouldNot containElementWithID("sample-requested-return")
    }
  }

}
