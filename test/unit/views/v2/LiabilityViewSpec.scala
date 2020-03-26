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

package views.v2

import controllers.ActiveTab
import models.{Operator, Permission}
import models.request.AuthenticatedRequest
import models.viewmodels.{AttachmentsTabViewModel, LiabilityViewModel}
import org.jsoup.select.Elements
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Request
import play.twirl.api.Html
import utils.Cases
import utils.Cases.{aCase, withBTIApplication, withLiabilityApplication, withReference}
import views.ViewMatchers.{containElementWithID, containText, haveAttribute}
import views.{CaseDetailPage, ViewSpec, html}
import views.html.v2.liability_view

class LiabilityViewSpec extends ViewSpec with GuiceOneAppPerSuite{

  private def request[A](operator: Operator, request: Request[A]) = new AuthenticatedRequest(operator, request)

  "Liability View" should {

    "render with case reference" in {

      val c = aCase(withReference("reference"), withLiabilityApplication())

      def liabilityView = app.injector.instanceOf[liability_view]

      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        AttachmentsTabViewModel(c.reference, Seq(Cases.storedAttachment), Some(Cases.letterOfAuthority),
          Seq(Cases.storedOperatorAttachment)))(request, messages, appConfig))

      doc.getElementById("case-reference") should containText(c.reference)
    }

    "render C592 tab always" in {

      val c = aCase(withReference("reference"), withLiabilityApplication())

      def liabilityView = app.injector.instanceOf[liability_view]

      val doc = view(liabilityView(LiabilityViewModel.fromCase(c, Cases.operatorWithoutPermissions),
        AttachmentsTabViewModel(c.reference, Seq(Cases.storedAttachment), Some(Cases.letterOfAuthority),
          Seq(Cases.storedOperatorAttachment)))(request, messages, appConfig))

      doc should containElementWithID("c592_tab")
    }

  }
}
