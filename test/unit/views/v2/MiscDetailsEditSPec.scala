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

import models.forms.v2.MiscDetailsForm
import models.viewmodels.CaseViewModel
import utils.Cases
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.v2.misc_details_edit

class MiscDetailsEditSPec extends ViewSpec {

  def mistDetailsEdit: misc_details_edit = injector.instanceOf[misc_details_edit]

  private val sampleCase = Cases.miscellaneousCaseExample
  private val operator   = Cases.operatorWithoutPermissions

  "Misc Details Edit View" should {

    "render the misc fields correctly" in {
      val doc =
        view(
          mistDetailsEdit(
            sampleCase,
            MiscDetailsForm.miscDetailsForm(sampleCase),
            CaseViewModel.fromCase(sampleCase, operator)
          )
        )

      doc should containElementWithID("misc-details-edit-form")
    }

    "render the misc view with all the fields" in {
      val doc =
        view(
          mistDetailsEdit(
            sampleCase,
            MiscDetailsForm.miscDetailsForm(sampleCase),
            CaseViewModel.fromCase(sampleCase, operator)
          )
        )

      doc should containElementWithID("misc-details-edit-form")
      doc should containElementWithID("summary")
      doc should containElementWithID("detailedDescription")
      doc should containElementWithID("typeMisc")
    }
  }

}
