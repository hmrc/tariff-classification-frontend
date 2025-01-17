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

package views.partials

import models.Permission
import models.viewmodels.correspondence.CaseDetailsViewModel
import utils.Cases.aCorrespondenceCase
import utils.Dates
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.partials.correspondence_case_details

import java.time.Instant

class CorrespondenceCaseDetailsViewSpec extends ViewSpec {

  "Correspondence Case Details" should {

    "render edit correspondence case Details when user has permission to edit correspondence" in {
      val c              = aCorrespondenceCase()
      val caseDetailsTab = CaseDetailsViewModel.fromCase(c)

      val doc = view(
        correspondence_case_details(caseDetailsTab)(
          requestWithPermissions(Permission.EDIT_CORRESPONDENCE),
          messages
        )
      )

      val editCorrespondence = doc.getElementById("edit-correspondence-details")
      editCorrespondence.text() shouldBe "Edit correspondence case details"
    }

    "not render edit correspondence case Details when user does not have permission to edit" in {
      val c              = aCorrespondenceCase()
      val caseDetailsTab = CaseDetailsViewModel.fromCase(c)

      val doc = view(correspondence_case_details(caseDetailsTab))

      doc shouldNot containElementWithID("edit-correspondence-details")
    }

    "render summary when present" in {
      val c              = aCorrespondenceCase()
      val caseDetailsTab = CaseDetailsViewModel.fromCase(c)

      val doc = view(correspondence_case_details(caseDetailsTab))

      val summary = doc.getElementById("summary")
      summary.text() shouldBe "A short summary"
    }

    "render detailed description when present" in {
      val c              = aCorrespondenceCase()
      val caseDetailsTab = CaseDetailsViewModel.fromCase(c)

      val doc = view(correspondence_case_details(caseDetailsTab))

      val detailedDescription = doc.getElementById("detailed-description")
      detailedDescription.text() shouldBe "A detailed desc"
    }

    "render case created date when present" in {
      val c              = aCorrespondenceCase()
      val caseDetailsTab = CaseDetailsViewModel.fromCase(c)

      val doc = view(correspondence_case_details(caseDetailsTab))

      val createdDate = doc.getElementById("case-created")
      createdDate.text() shouldBe Dates.format(Instant.now())
    }

    "render Boards file number when present" in {
      val c              = aCorrespondenceCase().copy(caseBoardsFileNumber = Some("SOC/554/2015/JN"))
      val caseDetailsTab = CaseDetailsViewModel.fromCase(c)

      val doc = view(correspondence_case_details(caseDetailsTab))

      val boardFileNumber = doc.getElementById("boards-file-number")
      boardFileNumber.text() shouldBe "SOC/554/2015/JN"
    }

  }
}
