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

import models.{Address, Permission}
import models.viewmodels.{C592ViewModel, PortOrComplianceOfficerContact, TraderContact}
import utils.Cases
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.partials.liabilities.c592_tab

class C592ViewSpec extends ViewSpec {

  def c592Tab: c592_tab = app.injector.instanceOf[c592_tab]
  

  "C592 View" should {
    "render successfully" in {
      val doc = view(c592Tab(Cases.c592ViewModel))

      doc should containElementWithID("liability-entry-date")

    }
    "contain Edit Details if operator has the required permissions" in {
      val doc = view(c592Tab(Cases.c592ViewModel)
      (requestWithPermissions(Permission.EDIT_LIABILITY),messages, appConfig))

      doc should containElementWithID("edit-liability-details")
    }

    "not contain Edit Details if operator does not have the required permissions" in {
      val doc = view(c592Tab(Cases.c592ViewModel)
      (requestWithPermissions(Permission.VIEW_CASES),messages, appConfig))

      doc shouldNot containElementWithID("edit-liability-details")
    }

    "show repayment section if one is required" in {
      val doc = view(c592Tab(Cases.c592ViewModel.copy(isRepaymentClaim = true))
      (requestWithPermissions(Permission.VIEW_CASES),messages, appConfig))

      doc should containElementWithID("dvr_number")
    }

    "not show repayment section if one is not required" in {
      val doc = view(c592Tab(Cases.c592ViewModel)
      (requestWithPermissions(Permission.VIEW_CASES),messages, appConfig))

      doc shouldNot containElementWithID("dvr_number")
    }
  }

}
