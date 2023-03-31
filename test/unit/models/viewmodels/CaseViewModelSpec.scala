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

package models.viewmodels

import models.CaseStatus.CaseStatus
import models._
import utils.Cases

import java.time.Instant

class CaseViewModelSpec extends ModelsBaseSpec {

  val openCase: Case                      = Cases.liabilityCaseExample.copy(status = CaseStatus.OPEN)
  val referredCase: Case                  = Cases.liabilityCaseExample.copy(status = CaseStatus.REFERRED)
  val rejectedCase: Case                  = Cases.liabilityCaseExample.copy(status = CaseStatus.REJECTED)
  val suspendedCase: Case                 = Cases.liabilityCaseExample.copy(status = CaseStatus.SUSPENDED)
  val completedCase: Case                 = Cases.liabilityCaseExample.copy(status = CaseStatus.COMPLETED)
  val newCase: Case                       = Cases.liabilityCaseExample.copy(status = CaseStatus.NEW)
  val cancelledCase: Case                 = Cases.liabilityCaseExample.copy(status = CaseStatus.CANCELLED)
  val casesWithRulingTab: Seq[Case]       = Seq(openCase, referredCase, rejectedCase, suspendedCase, completedCase)
  val casesWithoutRulingTab: Seq[Case]    = Seq(newCase, cancelledCase)
  val operator: Operator                  = Cases.operatorWithCompleteCasePermission
  val operatorWithoutPermission: Operator = Cases.operatorWithoutCompleteCasePermission

  private val caseHeaderViewModel =
    CaseHeaderViewModel(
      ApplicationType.LIABILITY,
      Some("trader-business-name"),
      "good-name",
      "1",
      Some("case-source"),
      Contact("name", "email@email.com"),
      CaseStatusViewModel(None, Some(StatusTagViewModel("CANCELLED", "red")), None),
      isMigrated = false
    )

  def buildLiabilityModel(
    caseHeaderViewModel: CaseHeaderViewModel = caseHeaderViewModel,
    hasPermissions: Boolean                  = false,
    showChangeCaseStatus: Boolean            = false,
    showTakeOffReferral: Boolean             = false,
    showReopen: Boolean                      = false,
    showAppeal: Boolean                      = false,
    status: CaseStatus
  ): CaseViewModel =
    CaseViewModel(
      caseHeader           = caseHeaderViewModel,
      hasPermissions       = hasPermissions,
      showChangeCaseStatus = showChangeCaseStatus,
      showTakeOffReferral  = showTakeOffReferral,
      showReopen           = showReopen,
      showAppealTab        = showAppeal,
      caseStatus           = status
    )

  "showActionThisCase" should {

    "not show action this case button when isNewCase = false and hasPermissions = false" in {
      buildLiabilityModel(status = CaseStatus.OPEN).showActionThisCase shouldBe false
    }

    "not show action this case button when isNewCase = false and hasPermissions = true" in {
      buildLiabilityModel(status = CaseStatus.OPEN, hasPermissions = true).showActionThisCase shouldBe false
    }

    "not show action this case button when isNewCase = true and hasPermissions = false" in {
      buildLiabilityModel(status = CaseStatus.NEW).showActionThisCase shouldBe false
    }

    "show action this case button when isNewCase = true and hasPermissions = true" in {
      buildLiabilityModel(status = CaseStatus.NEW, hasPermissions = true).showActionThisCase shouldBe true
    }

  }

  "showChangeCaseStatus" should {

    "show change case status button when case status is OPEN and user has COMPLETE CASE permission" in {

      val caseViewModel = CaseViewModel.fromCase(openCase, operator)
      caseViewModel.showChangeCaseStatus shouldBe true
    }

    "not show change case status button when case status is not OPEN" in {

      val caseViewModel = CaseViewModel.fromCase(referredCase, operator)
      caseViewModel.showChangeCaseStatus shouldBe false
    }

    "not show change case status button when user has not COMPLETE CASE permission" in {

      val caseViewModel = CaseViewModel.fromCase(referredCase, operatorWithoutPermission)
      caseViewModel.showChangeCaseStatus shouldBe false
    }

  }

  "showTakeOffReferral" should {

    "show take off referral button when case status is REFERRED and user has REOPEN_CASE permission" in {

      val caseViewModel = CaseViewModel.fromCase(referredCase, operator)
      caseViewModel.showTakeOffReferral shouldBe true
    }

    "not show take off referral button when case status is not REFERRED" in {

      val caseViewModel = CaseViewModel.fromCase(openCase, operator)
      caseViewModel.showTakeOffReferral shouldBe false
    }

    "not show take off referral button when user has not REOPEN_CASE permission" in {

      val caseViewModel = CaseViewModel.fromCase(referredCase, operatorWithoutPermission)
      caseViewModel.showTakeOffReferral shouldBe false
    }

  }

  "showReopen" should {

    "show reopen button when case status is SUSPENDED and user has REOPEN_CASE permission" in {

      val caseViewModel = CaseViewModel.fromCase(suspendedCase, operator)
      caseViewModel.showReopen shouldBe true
    }

    "not reopen button when case status is not SUSPENDED" in {

      val caseViewModel = CaseViewModel.fromCase(openCase, operator)
      caseViewModel.showReopen shouldBe false
    }

    "not reopen button when user has not REOPEN_CASE permission" in {

      val caseViewModel = CaseViewModel.fromCase(suspendedCase, operatorWithoutPermission)
      caseViewModel.showReopen shouldBe false
    }

  }

  "fromCase" should {

    "create a cancelled view model" in {

      val createdDateTime = Instant.now
      val op              = Cases.operatorWithoutPermissions

      val c = Cases.liabilityCaseExample.copy(
        status  = CaseStatus.CANCELLED,
        queueId = Some("queueId"),
        application = Cases.liabilityCaseExample.application.asLiabilityOrder
          .copy(entryDate = Some(Instant.parse("2020-03-03T10:15:30.00Z"))),
        assignee    = Some(op),
        createdDate = createdDateTime
      )

      assert(
        CaseViewModel.fromCase(c, op) === CaseViewModel(
          CaseHeaderViewModel(
            ApplicationType.LIABILITY,
            Some("trader-business-name"),
            "good-name",
            "1",
            Some("trader-business-name"),
            Contact("name", "email", Some("phone")),
            CaseStatusViewModel(
              None,
              Some(StatusTagViewModel("CANCELLED", "red")),
              None
            ),
            isMigrated = false
          ),
          hasPermissions       = false,
          showChangeCaseStatus = false,
          showTakeOffReferral  = false,
          showReopen           = false,
          showAppealTab        = false,
          c.status
        )
      )
    }

    "create a complete view model if it has an expired ruling" in {

      val c  = Cases.liabilityCaseWithExpiredRuling
      val op = Cases.operatorWithoutPermissions

      assert(
        CaseViewModel
          .fromCase(c, op)
          .caseHeader
          .caseStatus
          .caseStatusTag
          .map(_.status)
          .get === "EXPIRED"
      )

    }

    "create a completed view model" in {

      val c  = Cases.liabilityCaseExample.copy(status = CaseStatus.COMPLETED)
      val op = Cases.operatorWithoutPermissions

      assert(
        CaseViewModel
          .fromCase(c, op)
          .caseHeader
          .caseStatus
          .caseStatusTag
          .map(_.status)
          .get === "COMPLETED"
      )

    }

    "create a viewModel with isNewCase is set to true" in {

      val c  = Cases.liabilityCaseExample.copy(status = CaseStatus.NEW)
      val op = Cases.operatorWithoutPermissions

      assert(CaseViewModel.fromCase(c, op).isNewCase === true)
    }

    "create a viewModel with isNewCase is set to false" in {

      val c  = Cases.liabilityCaseExample
      val op = Cases.operatorWithoutPermissions

      assert(CaseViewModel.fromCase(c, op).isNewCase === false)
    }

    "create a viewModel with hasPermissions flag set to false" in {
      val c  = Cases.liabilityCaseExample
      val op = Cases.operatorWithoutPermissions.copy(permissions = Set())

      assert(CaseViewModel.fromCase(c, op).hasPermissions === false)
    }

    "create a viewModel with hasPermissions flag set to false when operator doesn't have required permission" in {
      val c  = Cases.liabilityCaseExample
      val op = Cases.operatorWithoutPermissions.copy(permissions = Set(Permission.VIEW_CASES))

      assert(CaseViewModel.fromCase(c, op).hasPermissions === false)
    }

    "create a viewModel with hasPermissions flag set to true when operator has the required permission" in {
      val c  = Cases.liabilityCaseExample
      val op = Cases.operatorWithoutPermissions.copy(permissions = Set(Permission.RELEASE_CASE))

      assert(CaseViewModel.fromCase(c, op).hasPermissions === true)
    }

    casesWithRulingTab.foreach { c =>
      s"create a viewModel with showRulingTab flag is set to true when Case status is ${c.status}" in {

        val op = Cases.operatorWithoutPermissions

        assert(CaseViewModel.fromCase(c, op).showRulingAndKeywordsTabs === true)
      }
    }

    casesWithoutRulingTab.foreach { c =>
      s"create a viewModel with showRulingTab flag is set to false when Case status is ${c.status}" in {

        val op = Cases.operatorWithoutPermissions

        assert(CaseViewModel.fromCase(c, op).showRulingAndKeywordsTabs === false)
      }
    }

    "create a viewModel with showAppealTab flag true if case is COMPLETED and has the required permission" in {

      val c  = Cases.liabilityCaseExample.copy(status         = CaseStatus.COMPLETED)
      val op = Cases.operatorWithPermissions.copy(permissions = Set(Permission.APPEAL_CASE))

      assert(CaseViewModel.fromCase(c, op).showAppealTab === true)
    }

    "create a viewModel with showAppealTab flag false if case is not COMPLETED or CANCELLED and has the required permission" in {

      val c  = Cases.liabilityCaseExample.copy(status         = CaseStatus.OPEN)
      val op = Cases.operatorWithPermissions.copy(permissions = Set(Permission.APPEAL_CASE))

      assert(CaseViewModel.fromCase(c, op).showAppealTab === false)
    }

    "create a viewModel with showAppealTab flag false if case is CANCELLED and does not have the required permission" in {

      val c  = Cases.liabilityCaseExample.copy(status         = CaseStatus.CANCELLED)
      val op = Cases.operatorWithPermissions.copy(permissions = Set(Permission.VIEW_CASES))

      assert(CaseViewModel.fromCase(c, op).showAppealTab === false)
    }
  }
}
