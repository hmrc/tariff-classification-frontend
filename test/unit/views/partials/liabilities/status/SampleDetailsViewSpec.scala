package views.partials.liabilities.status

import models.viewmodels.SampleStatusTabViewModel
import models.{Event, Paged, Permission}
import views.ViewMatchers.containText
import views.ViewSpec
import views.ViewMatchers._
import views.html.partials.liabilities.sample.sample_details_liability

class SampleDetailsViewSpec extends ViewSpec {

  def sampleDetailsView: sample_details_liability = app.injector.instanceOf[sample_details_liability]

  "sample_details_liability view" should {

    "show sample return details when sample is being sent" in {

      val doc = view(sampleDetailsView(SampleStatusTabViewModel("caseReference",
        isSampleBeingSent = true,
        Some("a person"),
        None,
        "location",
        sampleActivity = Paged.empty[Event])))
      doc.getElementById("liability-sending-samples_answer") should containText("Yes")
      doc should containElementWithID("liability-returning-samples")

    }

    "not show sample return details when sample has no status" in {

      val doc = view(sampleDetailsView(SampleStatusTabViewModel("caseReference",
        isSampleBeingSent = false,
        Some("a person"),
        None,
        "location",
        sampleActivity = Paged.empty[Event])))
      doc.getElementById("liability-sending-samples_answer") should containText("No")
      doc shouldNot containElementWithID("liability-returning-samples")

    }

    "show sample location" in {

      val doc = view(sampleDetailsView(SampleStatusTabViewModel("caseReference",
        isSampleBeingSent = true,
        Some("a person"),
        None,
        "location",
        sampleActivity = Paged.empty[Event])))
      doc.getElementById("sample-status-value") should containText("location")

    }

    "show location edit link when operator has correct permission" in {

      val doc = view(sampleDetailsView(SampleStatusTabViewModel("caseReference",
        isSampleBeingSent = true,
        Some("a person"),
        None,
        "location",
        sampleActivity = Paged.empty[Event]))(requestWithPermissions(Permission.EDIT_SAMPLE),messages, appConfig))
      doc.getElementById("change-sample-status") should containElementWithTag("a")

    }

    "not show location edit link when operator does not have correct permission" in {

      val doc = view(sampleDetailsView(SampleStatusTabViewModel("caseReference",
        isSampleBeingSent = true,
        Some("a person"),
        None,
        "location",
        sampleActivity = Paged.empty[Event])))
      doc shouldNot containElementWithID("change-sample-status")

    }

  }

}
