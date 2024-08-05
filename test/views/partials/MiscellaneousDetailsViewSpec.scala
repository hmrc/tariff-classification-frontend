/*
 * Copyright 2024 HM Revenue & Customs
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

import models.Permission
import models.viewmodels.miscellaneous.DetailsViewModel
import utils.Cases.aMiscellaneousCase
import utils.Dates
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.partials.miscellaneous_case_details

import java.time.Instant

class MiscellaneousDetailsViewSpec extends ViewSpec {

  "Miscellaneous Details" should {

    "render edit Miscellaneous Details when user has permission to edit miscellaneous" in {
      val c              = aMiscellaneousCase()
      val caseDetailsTab = DetailsViewModel.fromCase(c)

      val doc = view(
        miscellaneous_case_details(caseDetailsTab)(
          requestWithPermissions(Permission.EDIT_MISCELLANEOUS),
          messages
        )
      )

      val editMisc = doc.getElementById("edit-miscellaneous-details")
      editMisc.text() shouldBe "Edit details"
    }

    "not render edit miscellaneous details when user does not have permission to edit" in {
      val c              = aMiscellaneousCase()
      val caseDetailsTab = DetailsViewModel.fromCase(c)

      val doc = view(miscellaneous_case_details(caseDetailsTab))

      doc shouldNot containElementWithID("edit-miscellaneous-details")
    }

    "render case name when present" in {
      val c              = aMiscellaneousCase()
      val caseDetailsTab = DetailsViewModel.fromCase(c)

      val doc = view(miscellaneous_case_details(caseDetailsTab))

      val caseName = doc.getElementById("case-name")
      caseName.text() shouldBe "name"
    }

    "render detailed description when present" in {
      val c              = aMiscellaneousCase()
      val caseDetailsTab = DetailsViewModel.fromCase(c)

      val doc = view(miscellaneous_case_details(caseDetailsTab))

      val detailedDescription = doc.getElementById("detailed-description")
      detailedDescription.text() shouldBe "A detailed description"
    }

    "render case created date when present" in {
      val c              = aMiscellaneousCase()
      val caseDetailsTab = DetailsViewModel.fromCase(c)

      val doc = view(miscellaneous_case_details(caseDetailsTab))

      val createdDate = doc.getElementById("date-created")
      createdDate.text() shouldBe Dates.format(Instant.now())
    }

    "render Boards file number when present" in {
      val c              = aMiscellaneousCase()
      val caseDetailsTab = DetailsViewModel.fromCase(c)

      val doc = view(miscellaneous_case_details(caseDetailsTab))

      val boardFileNumber = doc.getElementById("boards-file-number")
      boardFileNumber.text() shouldBe "SOC/554/2015/JN"
    }

    "render case type when present" in {
      val c              = aMiscellaneousCase()
      val caseDetailsTab = DetailsViewModel.fromCase(c)

      val doc = view(miscellaneous_case_details(caseDetailsTab))

      val summary = doc.getElementById("case-type")
      summary.text() shouldBe "Harmonised System"
    }

  }
}
