package views.v2

import controllers.ActiveTab
import models.{Operator, Permission}
import models.request.AuthenticatedRequest
import models.viewmodels.LiabilityViewModel
import org.jsoup.select.Elements
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Request
import play.twirl.api.Html
import utils.Cases.{aCase, withBTIApplication, withLiabilityApplication, withReference}
import views.ViewMatchers.{containText, haveAttribute}
import views.{CaseDetailPage, ViewSpec, html}
import views.html.v2.liability_view

class LiabilityViewSpec extends ViewSpec with GuiceOneAppPerSuite{

  private def request[A](operator: Operator, request: Request[A]) = new AuthenticatedRequest(operator, request)

  "Liability View" should {

    "render with case reference" in {

      val c = aCase(withReference("reference"), withLiabilityApplication())

      def liabilityView = app.injector.instanceOf[liability_view]

      val doc = view(liabilityView(LiabilityViewModel.fromCase(c))(request, messages, appConfig))

      doc.getElementById("case-reference") should containText(c.reference)
    }

  }
}
