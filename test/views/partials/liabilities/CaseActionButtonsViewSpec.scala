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

package views.partials.liabilities

import models._
import models.forms.UploadAttachmentForm
import models.viewmodels.CaseViewModel
import play.api.data.Form
import utils.Cases
import utils.Cases._
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.partials.liabilities.case_action_buttons_partial

class CaseActionButtonsViewSpec extends ViewSpec {

  def caseActionButtonsPartial: case_action_buttons_partial = injector.instanceOf[case_action_buttons_partial]

  def uploadAttachmentForm: Form[String] = UploadAttachmentForm.form

  "Case action buttons" should {

    "render the action this case button for new case" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.NEW), withLiabilityApplication())

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithReleaseOrSuppressPermissions)
        )
      )

      doc                                                  should containElementWithID("action-this-case-button")
      doc.getElementById("action-this-case-button").text shouldBe messages("case.v2.liability.action_this_case.button")
    }

    "not render the action this case button" in {

      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.NEW), withLiabilityApplication())

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions)
        )
      )

      doc shouldNot containElementWithID("action-this-case-button")
    }

    "render the change case status button for open case with complete case permission" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.OPEN), withLiabilityApplication())

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithCompleteCasePermission)
        )
      )

      doc should containElementWithID("change-case-status-button")
      doc.getElementById("change-case-status-button").text shouldBe messages(
        "case.v2.liability.change_case_status.button"
      )
    }

    "not render the change case status button" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.NEW), withLiabilityApplication())

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions)
        )
      )

      doc shouldNot containElementWithID("change-case-status-button")
    }

    "render the take off referral button for a referred case and reopen case permission" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.REFERRED))

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithCompleteCasePermission)
        )
      )

      doc should containElementWithID("take-off-referral-button")
      doc.getElementById("take-off-referral-button").text shouldBe messages(
        "case.v2.liability.take_off_referral.button"
      )
    }

    "not render the take off referral button for a not referred case" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.OPEN))

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithCompleteCasePermission)
        )
      )

      doc shouldNot containElementWithID("take-off-referral-button")
    }

    "not render the take off referral button for a case without permissions" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.REFERRED))

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions)
        )
      )

      doc shouldNot containElementWithID("take-off-referral-button")
    }

    "render the reopen case button for a referred case and reopen case permission" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.SUSPENDED))

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithCompleteCasePermission)
        )
      )

      doc                                      should containElementWithID("reopen-case")
      doc.getElementById("reopen-case").text shouldBe messages("case.v2.liability.reopen.button")
    }

    "not render the reopen case button for a not referred case" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.OPEN))

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithCompleteCasePermission)
        )
      )

      doc shouldNot containElementWithID("reopen-case")
    }

    "not render the reopen case button for a case without permissions" in {
      val c = aLiabilityCase(withReference("reference"), withStatus(CaseStatus.SUSPENDED))

      val doc = view(
        caseActionButtonsPartial(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions)
        )
      )

      doc shouldNot containElementWithID("reopen-case")
    }
  }
}
