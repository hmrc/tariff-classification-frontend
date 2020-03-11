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
import views.html.partials.sample.sample_details_liability
import utils.Cases._

class SampleDetailsLiabilityViewSpec extends ViewSpec {

  "Sample Details" should {

    "render sample to be returned when sample provided" in {
      // Given
      val caseWithSample = aCase(
        withLiabilityApplication(),
        withSample(Sample(Some(SampleStatus.AWAITING), None, Some(SampleReturn.YES)))
      )

      // When
      val doc = view(sample_details_liability(caseWithSample))

      // Then
      doc.getElementById("liability-sending-samples") should containText(messages("answer.yes"))
      doc.getElementById("liability-returning-samples") should containText(messages("answer.yes"))
    }

    "not render sample to be returned when sample not provided" in {
      // Given
      val `case` = aCase(
        withLiabilityApplication()
      )

      // When
      val doc = view(sample_details_liability(`case`))

      // Then
      doc.getElementById("liability-sending-samples") should containText(messages("answer.no"))
      doc shouldNot containElementWithID("liability-returning-samples")
    }

    "render sample status details when present on case" in {
      // Given
      val caseWithSample = aCase(
        withSampleStatus(Some(SampleStatus.AWAITING)),
        withBTIDetails(sampleToBeProvided = true, sampleToBeReturned = true),
        withoutAttachments()
      )

      // When
      val doc = view(sample_details_liability(caseWithSample))

      doc.getElementById("sample-status-value") should containText(SampleStatus.format(Some(SampleStatus.AWAITING)))
    }

    "not render sample status details of when sample not being provided" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = false, sampleToBeReturned = false),
        withoutAttachments()
      )

      // When
      val doc = view(sample_details_liability(caseWithSample))

      doc shouldNot containElementWithID("sample-status-value")
    }

    "not render sample status activity when sample not being provided" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails(sampleToBeProvided = false, sampleToBeReturned = false),
        withoutAttachments()
      )
      // When
      val doc = view(sample_details_liability(caseWithSample))

      doc shouldNot containElementWithID("sample-status-events-heading")
    }

    "not render sample requested when present on case" in {
      // Given
      val caseWithSample = aCase(
        withLiabilityApplication(),
        withSampleRequested(Some(Operator("id", name = Some("Tester Op"))), Some(SampleReturn.TO_BE_CONFIRMED))
      )

      // When
      val doc = view(sample_details_liability(caseWithSample))

      doc shouldNot containElementWithID("sample-requested-by")
      doc shouldNot containElementWithID("sample-requested-return")
    }
  }

}
