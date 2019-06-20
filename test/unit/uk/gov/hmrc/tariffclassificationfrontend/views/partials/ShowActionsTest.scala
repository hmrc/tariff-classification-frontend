/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec

class ShowActionsTest extends ViewSpec {

  "Show Actions" should {

    "Show Refer" in {
      ShowActions(CaseStatus.OPEN)(requestWithPermissions(Permission.REFER_CASE)).refer shouldBe true
      ShowActions(CaseStatus.OPEN)(requestWithPermissions()).refer shouldBe false
    }

    "Show Reject" in {
      ShowActions(CaseStatus.OPEN)(requestWithPermissions(Permission.REJECT_CASE)).reject shouldBe true
      ShowActions(CaseStatus.OPEN)(requestWithPermissions()).reject shouldBe false
    }

    "Show Suspend" in {
      ShowActions(CaseStatus.OPEN)(requestWithPermissions(Permission.SUSPEND_CASE)).suspend shouldBe true
      ShowActions(CaseStatus.OPEN)(requestWithPermissions()).suspend shouldBe false
    }

    "Show Release" in {
      ShowActions(CaseStatus.NEW)(requestWithPermissions(Permission.RELEASE_CASE)).release shouldBe true
      ShowActions(CaseStatus.NEW)(requestWithPermissions()).release shouldBe false
    }

    "Show Suppress" in {
      ShowActions(CaseStatus.NEW)(requestWithPermissions(Permission.SUPPRESS_CASE)).suppress shouldBe true
      ShowActions(CaseStatus.NEW)(requestWithPermissions()).suppress shouldBe false
    }

    "Show Reopen" in {
      ShowActions(CaseStatus.SUSPENDED)(requestWithPermissions(Permission.REOPEN_CASE)).reopen shouldBe true
      ShowActions(CaseStatus.SUSPENDED)(requestWithPermissions()).reopen shouldBe false
    }
  }

}
