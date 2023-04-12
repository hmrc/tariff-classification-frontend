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

package views.partials

import models.viewmodels.atar.GoodsTabViewModel
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.application_details

class ApplicationDetailsViewSpec extends ViewSpec {

  "Application Details" should {

    "not render default negative text on optional fields when not present" in {

      val `case` = aCase(
        withOptionalApplicationFields(),
        withoutAttachments()
      )

      val goodsTab = GoodsTabViewModel.fromCase(`case`)


      val doc = view(application_details(goodsTab))


      doc.getElementById("app-details-confidential-info") shouldNot containText(messages("answer.none"))
      doc shouldNot containElementWithID("app-details-related-reference")
      doc.getElementById("app-details-legal-proceedings") shouldNot containText(messages("answer.no"))
    }

    "Render optional fields when present" in {

      val `case` = aCase(
        withOptionalApplicationFields(
          confidentialInformation = Some("confidential info"),
          otherInformation        = Some("other info"),
          reissuedBTIReference    = Some("reissued bti"),
          relatedBTIReference     = Some("related bti"),
          knownLegalProceedings   = Some("legal proceedings"),
          envisagedCommodityCode  = Some("envisaged code")
        ),
        withAttachment(attachment("FILE_ID"))
      )

      val goodsTab = GoodsTabViewModel.fromCase(`case`)


      val doc = view(application_details(goodsTab))


      doc                                                        should containElementWithID("app-details-previous-ruling-reference")
      doc                                                        should containElementWithID("app-details-envisaged-code")
      doc.getElementById("app-details-envisaged-code")           should containText("envisaged code")
      doc                                                        should containElementWithID("app-details-confidential-info")
      doc.getElementById("app-details-confidential-info")        should containText("confidential info")
      doc                                                        should containElementWithID("app-details-similar-ruling-reference")
      doc.getElementById("app-details-similar-ruling-reference") should containText("related bti")
      doc                                                        should containElementWithID("app-details-legal-challenges")
      doc.getElementById("app-details-legal-challenges")         should containText("legal proceedings")
    }

    "Render the correct number of relatedBtiReferences " in {

      val `case` = aCase(
        withOptionalApplicationFields(
          relatedBTIReferences = List("related BTI 1", "related BTI 2")
        )
      )

      val goodsTab = GoodsTabViewModel.fromCase(`case`)


      val doc = view(application_details(goodsTab))

      //Then
      doc                                                        should containElementWithID("app-details-similar-ruling-reference")
      doc.getElementById("app-details-similar-ruling-reference") should containText("related BTI 1 related BTI 2")

    }
  }
}
