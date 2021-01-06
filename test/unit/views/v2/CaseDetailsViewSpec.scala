/*
 * Copyright 2021 HM Revenue & Customs
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

import models.viewmodels.correspondence.CaseDetailsViewModel
import utils.Cases.{aCase, withCorrespondenceApplication}
import views.ViewMatchers.containText
import views.ViewSpec
import views.html.partials.correspondence_case_details

class CaseDetailsViewSpec extends ViewSpec {

  "Case Details" should {

    "render successfully" in {
      val caseDetails = aCase(withCorrespondenceApplication)

      val caseDetailsTab = CaseDetailsViewModel.fromCase(caseDetails)

      val doc = view(correspondence_case_details(caseDetailsTab))
      println(doc)
      doc.getElementById("case-details-summary") should containText("A short summary")
    }
  }


}
