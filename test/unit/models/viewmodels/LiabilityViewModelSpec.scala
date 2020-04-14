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

package models.viewmodels

import java.time.Instant

import models.{CaseStatus, Permission}
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases

class LiabilityViewModelSpec extends UnitSpec {

  private val caseHeaderViewModel = CaseHeaderViewModel("Liability",
    "trader-business-name", "good-name",
    "1",
    "CANCELLED",
    isLive = false)

  val openCase = Cases.liabilityCaseExample.copy(status = CaseStatus.OPEN)
  val referredCase = Cases.liabilityCaseExample.copy(status = CaseStatus.REFERRED)
  val rejectedCase = Cases.liabilityCaseExample.copy(status = CaseStatus.REJECTED)
  val suspendedCase = Cases.liabilityCaseExample.copy(status = CaseStatus.SUSPENDED)
  val completedCase = Cases.liabilityCaseExample.copy(status = CaseStatus.COMPLETED)

  val newCase = Cases.liabilityCaseExample.copy(status = CaseStatus.NEW)
  val cancelledCase = Cases.liabilityCaseExample.copy(status = CaseStatus.CANCELLED)

  val casesWithRulingTab = Seq(openCase, referredCase, rejectedCase, suspendedCase, completedCase)
  val casesWithoutRulingTab = Seq(newCase, cancelledCase)

  val operator = Cases.operatorWithCompleteCasePermission
  val operatorWithoutPermission = Cases.operatorWithoutCompleteCasePermission

  def buildLiabilityModel(
                           caseHeaderViewModel: CaseHeaderViewModel = caseHeaderViewModel,
                           isNewCase: Boolean = false,
                           hasPermissions: Boolean = false,
                           showRulingTab: Boolean = false,
                           showChangeCaseStatus: Boolean = false,
                           showTakeOffReferral: Boolean = false,
                           showReopen: Boolean = false
                         ): LiabilityViewModel = {
    LiabilityViewModel(
      caseHeaderViewModel = caseHeaderViewModel,
      isNewCase = isNewCase,
      hasPermissions = hasPermissions,
      showRulingTab = showRulingTab,
      showChangeCaseStatus = showChangeCaseStatus,
      showTakeOffReferral = showTakeOffReferral,
      showReopen = showReopen
    )
  }

  "showActionThisCase" should {

    "not show action this case button when isNewCase = false and hasPermissions = false" in {

      buildLiabilityModel(isNewCase = false, hasPermissions = false).showActionThisCase shouldBe false
    }

    "not show action this case button when isNewCase = false and hasPermissions = true" in {

      buildLiabilityModel(isNewCase = false, hasPermissions = true).showActionThisCase shouldBe false
    }

    "not show action this case button when isNewCase = true and hasPermissions = false" in {

      buildLiabilityModel(isNewCase = true, hasPermissions = false).showActionThisCase shouldBe false
    }

    "show action this case button when isNewCase = true and hasPermissions = true" in {

      buildLiabilityModel(isNewCase = true, hasPermissions = true).showActionThisCase shouldBe true
    }
  }

  "showChangeCaseStatus" should {

    "show change case status button when case status is OPEN and user has COMPLETE CASE permission" in {

      val liabilityViewModel = LiabilityViewModel.fromCase(openCase, operator)
      liabilityViewModel.showChangeCaseStatus shouldBe true
    }

    "not show change case status button when case status is not OPEN" in {

      val liabilityViewModel = LiabilityViewModel.fromCase(referredCase, operator)
      liabilityViewModel.showChangeCaseStatus shouldBe false
    }

    "not show change case status button when user has not COMPLETE CASE permission" in {

      val liabilityViewModel = LiabilityViewModel.fromCase(referredCase, operatorWithoutPermission)
      liabilityViewModel.showChangeCaseStatus shouldBe false
    }
  }

  "showTakeOffReferral" should {

    "show take off referral button when case status is REFERRED and user has REOPEN_CASE permission" in {

      val liabilityViewModel = LiabilityViewModel.fromCase(referredCase, operator)
      liabilityViewModel.showTakeOffReferral shouldBe true
    }

    "not show take off referral button when case status is not REFERRED" in {

      val liabilityViewModel = LiabilityViewModel.fromCase(openCase, operator)
      liabilityViewModel.showTakeOffReferral shouldBe false
    }


    "not show take off referral button when user has not REOPEN_CASE permission" in {

      val liabilityViewModel = LiabilityViewModel.fromCase(referredCase, operatorWithoutPermission)
      liabilityViewModel.showTakeOffReferral shouldBe false
    }

  }

  "fromCase" should {

    "create a cancelled view model" in {

      val createdDateTime = Instant.now
      val op = Cases.operatorWithoutPermissions

      val c = Cases.liabilityCaseExample.copy(
        status = CaseStatus.CANCELLED,
        queueId = Some("queueId"),
        application = Cases.liabilityCaseExample.application.asLiabilityOrder.copy(
          entryDate = Some(Instant.parse("2020-03-03T10:15:30.00Z"))),
        assignee = Some(op),
        createdDate = createdDateTime)

      assert(LiabilityViewModel.fromCase(c, op) === LiabilityViewModel(CaseHeaderViewModel("Liability",
        "trader-business-name", "good-name",
        "1",
        "CANCELLED",
        isLive = false),
        isNewCase = false,
        hasPermissions = false,
        showRulingTab = false,
        showChangeCaseStatus = false,
        showTakeOffReferral = false,
        showReopen = false)
      )
    }

    "create a complete view model if it has an expired ruling" in {

      val c = Cases.liabilityCaseWithExpiredRuling
      val op = Cases.operatorWithoutPermissions

      assert(LiabilityViewModel.fromCase(c, op).caseHeaderViewModel.caseStatus === "EXPIRED")

    }

    "create a completed view model" in {

      val c = Cases.liabilityCaseExample.copy(status = CaseStatus.COMPLETED)
      val op = Cases.operatorWithoutPermissions

      assert(LiabilityViewModel.fromCase(c, op).caseHeaderViewModel.caseStatus === "COMPLETED")

    }

    "create a viewModel with isNewCase is set to true" in {

      val c = Cases.liabilityCaseExample.copy(status = CaseStatus.NEW)
      val op = Cases.operatorWithoutPermissions

      assert(LiabilityViewModel.fromCase(c, op).isNewCase === true)
    }

    "create a viewModel with isNewCase is set to false" in {

      val c = Cases.liabilityCaseExample
      val op = Cases.operatorWithoutPermissions

      assert(LiabilityViewModel.fromCase(c, op).isNewCase === false)
    }

    "create a viewModel with hasPermissions flag set to false" in {
      val c = Cases.liabilityCaseExample
      val op = Cases.operatorWithoutPermissions.copy(permissions = Set())

      assert(LiabilityViewModel.fromCase(c, op).hasPermissions === false)
    }

    "create a viewModel with hasPermissions flag set to false when operator doesn't have required permission" in {
      val c = Cases.liabilityCaseExample
      val op = Cases.operatorWithoutPermissions.copy(permissions = Set(Permission.VIEW_CASES))

      assert(LiabilityViewModel.fromCase(c, op).hasPermissions === false)
    }

    "create a viewModel with hasPermissions flag set to true when operator has the required permission" in {
      val c = Cases.liabilityCaseExample
      val op = Cases.operatorWithoutPermissions.copy(permissions = Set(Permission.RELEASE_CASE))

      assert(LiabilityViewModel.fromCase(c, op).hasPermissions === true)
    }

    casesWithRulingTab.foreach { c =>
      s"create a viewModel with showRulingTab flag is set to true when Case status is ${c.status}" in {

        val op = Cases.operatorWithoutPermissions

        assert(LiabilityViewModel.fromCase(c, op).showRulingTab === true)
      }
    }

    casesWithoutRulingTab.foreach { c =>
      s"create a viewModel with showRulingTab flag is set to false when Case status is ${c.status}" in {

        val op = Cases.operatorWithoutPermissions

        assert(LiabilityViewModel.fromCase(c, op).showRulingTab === false)
      }
    }
  }
}
