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

import models.{CaseStatus, ReferralCaseStatusChange, ReferralReason}
import models.viewmodels.{ApplicationTabViewModel, ApplicationsTab}
import utils.Cases
import views.ViewMatchers.{containElementWithID, haveClass}
import views.ViewSpec
import views.html.v2.my_cases_view

class MyCasesViewSpec extends ViewSpec {

  val referredEvents: Map[String, ReferralCaseStatusChange] =
    Map(
      "1" -> ReferralCaseStatusChange(
        CaseStatus.OPEN,
        Some("some comment"),
        None,
        "Laboratory analyst",
        Seq(ReferralReason.REQUEST_MORE_INFO)
      ),
      "2" -> ReferralCaseStatusChange(
        CaseStatus.OPEN,
        Some("another comment"),
        None,
        "Trader",
        Seq(ReferralReason.REQUEST_SAMPLE)
      ),
      "4" -> ReferralCaseStatusChange(
        CaseStatus.OPEN,
        Some("another comment"),
        None,
        "Other reason",
        Seq(ReferralReason.REQUEST_MORE_INFO)
      )
    )

  val assignedToMeCasesTab =
    ApplicationTabViewModel(
      "message key",
      ApplicationsTab.assignedToMeCases(Seq(Cases.btiCaseExample, Cases.liabilityCaseExample, Cases.corrCaseExample)).applicationTabs
    )

  val referredByMeCasesTab =
    ApplicationTabViewModel(
      "applicationTab.referredByMe",
      ApplicationsTab
        .referredByMe(
          Seq(
            Cases.btiCaseExample.copy(status          = CaseStatus.REFERRED),
            Cases.liabilityCaseExample.copy(reference = "2", status = CaseStatus.REFERRED, referredDaysElapsed = 65),
            Cases.btiCaseExample.copy(reference       = "3", status = CaseStatus.SUSPENDED, daysElapsed = 30),
            Cases.newLiabilityLiveCaseExample
              .copy(reference = "4", status = CaseStatus.REFERRED, daysElapsed = 5, referredDaysElapsed = 6)
          ),
          referredEvents
        )
        .applicationTabs
    )

  def myCasesView: my_cases_view = injector.instanceOf[my_cases_view]

  "myCasesViewSpec" should {

    "render successfully" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("my-cases-tabs")
    }

    "contain my_cases_secondary_navigation" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("my-cases-sub-nav")
    }

    "contain my cases component" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
      doc should containElementWithID("correspondence_tab")
      //doc should containElementWithID("misc_tab")
    }

    "contain a heading" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("common-cases-heading")
    }

    "contain atar table" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("applicationTab.atar-table")
    }

    "contain liabilities table" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("applicationTab.liability-table")
    }

    "contain my_cases_secondary_navigation for Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("my-cases-sub-nav")
    }

    "contain my cases component for Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
      //doc should containElementWithID("correspondence_tab")
      //doc should containElementWithID("misc_tab")
    }

    "contain a heading for referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("common-cases-heading")
    }

    "contain atar table in Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.atar-table")
    }

    "contain liabilities table in referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.liability-table")
    }

    "contain a referral event in  liabilities tab for ReferredbyMe" in {

      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.liability-status-label-0-status")

    }

    "contain a referral event in  atar tab for ReferredbyMe" in {

      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.atar-status-label-0-status")

    }

    "contain referral reason in  atar tab for ReferredbyMe" in {

      val doc = view(myCasesView(referredByMeCasesTab))

      doc.getElementById("applicationTab.atar-status-refer-to-0").text shouldBe ("Laboratory analyst")

    }

    "contain referral reason in  liability tab for ReferredbyMe" in {

      val doc = view(myCasesView(referredByMeCasesTab))

      doc.getElementById("applicationTab.liability-status-refer-to-0").text shouldBe ("Trader")
      doc.getElementById("applicationTab.atar-status-label-1-overdue").text shouldBe "OVERDUE"
      doc.getElementById("applicationTab.atar-elapsed-days-1")              should haveClass("live-red-text")
      doc.getElementById("applicationTab.liability-refer-days-0").text      shouldBe "65"
      doc.getElementById("applicationTab.liability-type-1").text            should include("LIVE")
      doc.getElementById("applicationTab.liability-status-1").text          should include("Other reason")

    }

    "contain correspondence table" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("applicationTab.correspondence-table")
    }

    //Uncomment the following tests when the components are implemented
    /*
        "contain liabilities miscellaneous" in {
          val doc = view(myCasesView("title", applicationsTab))

          doc should containElementWithID("miscellaneous")
        }*/
  }

}
