/*
 * Copyright 2023 HM Revenue & Customs
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
import views.html.partials.sample.sample_status
import models.viewmodels.atar.SampleTabViewModel

class SampleStatusViewSpec extends ViewSpec {

  "Sample Details" should {

    "render change status link when user has permission" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails()
      )

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged.empty)

      // When
      val doc = view(sample_status(sampleTab)(requestWithPermissions(Permission.EDIT_SAMPLE), messages, appConfig))

      // Then
      doc should containElementWithID("change-sample-status")
    }

    "not render change status link when user does not have permission" in {
      // Given
      val caseWithSample = aCase(
        withBTIDetails()
      )

      val sampleTab = SampleTabViewModel.fromCase(caseWithSample, Paged.empty)

      // When
      val doc = view(sample_status(sampleTab)(requestWithPermissions(Permission.VIEW_CASES), messages, appConfig))

      // Then
      doc shouldNot containElementWithID("change-sample-status")
    }
  }

}
