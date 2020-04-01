package views.partials.liabilities.status

import views.ViewSpec
import views.html.partials.liabilities.sample.sample_details_liability

class SampleDetailsViewSpec extends ViewSpec {

  def sampleDetailsView: sample_details_liability = app.injector.instanceOf[sample_details_liability]


  "sample_details_liability view" should {

    "render correctly" in {

    }

  }



}
