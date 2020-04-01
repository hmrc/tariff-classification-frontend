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

import java.security.Permissions
import java.time.Instant

import controllers.ActiveTab.Liability
import models.{CaseStatus, Operator, Paged, Permission, Queue}
import uk.gov.hmrc.play.test.UnitSpec
import utils.{Cases, Events}


class LiabilityViewModelSpec extends UnitSpec {
  private val pagedEvent = Paged(Seq(Events.event), 1, 1, 1)
  private val queues = Seq(Queue("", "", ""))

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
        hasPermissions = false))
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
  }

}
