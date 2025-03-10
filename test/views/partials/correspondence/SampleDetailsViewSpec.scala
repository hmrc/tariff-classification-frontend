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

package views.partials.correspondence

import models.viewmodels.SampleStatusTabViewModel
import models.{Event, Paged, Permission, SampleSend}
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.sample.sample_details_correspondence

class SampleDetailsViewSpec extends ViewSpec {

  def sampleDetailsView: sample_details_correspondence = injector.instanceOf[sample_details_correspondence]

  "sample_details_correspondence view" should {

    "show sample return details when sample is being sent" in {

      val doc = view(
        sampleDetailsView(
          SampleStatusTabViewModel(
            "caseReference",
            isSampleBeingSent = true,
            Some(SampleSend.AGENT),
            None,
            "location",
            sampleActivity = Paged.empty[Event]
          )
        )
      )
      doc.getElementById("correspondence-sending-samples_answer") should containText("Yes")
      doc should containElementWithID("correspondence-returning-samples")

    }

    "not show sample return details when sample has no status" in {

      val doc = view(
        sampleDetailsView(
          SampleStatusTabViewModel(
            "caseReference",
            isSampleBeingSent = false,
            Some(SampleSend.AGENT),
            None,
            "location",
            sampleActivity = Paged.empty[Event]
          )
        )
      )
      doc.getElementById("correspondence-sending-samples_answer") should containText("No")
      doc shouldNot containElementWithID("correspondence-returning-samples")

    }

    "show sample location" in {

      val doc = view(
        sampleDetailsView(
          SampleStatusTabViewModel(
            "caseReference",
            isSampleBeingSent = true,
            Some(SampleSend.AGENT),
            None,
            "location",
            sampleActivity = Paged.empty[Event]
          )
        )
      )
      doc.getElementById("sample-status-value") should containText("location")

    }

    "show location edit link when operator has correct permission" in {

      val doc = view(
        sampleDetailsView(
          SampleStatusTabViewModel(
            "caseReference",
            isSampleBeingSent = true,
            Some(SampleSend.AGENT),
            None,
            "location",
            sampleActivity = Paged.empty[Event]
          )
        )(requestWithPermissions(Permission.EDIT_SAMPLE), messages)
      )
      doc.getElementById("change-sample-status") should containElementWithTag("a")

    }

    "not show location edit link when operator does not have correct permission" in {

      val doc = view(
        sampleDetailsView(
          SampleStatusTabViewModel(
            "caseReference",
            isSampleBeingSent = true,
            Some(SampleSend.AGENT),
            None,
            "location",
            sampleActivity = Paged.empty[Event]
          )
        )
      )
      doc shouldNot containElementWithID("change-sample-status")

    }

  }

}
