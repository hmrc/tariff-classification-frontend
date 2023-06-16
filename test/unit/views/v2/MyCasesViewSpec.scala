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

package views.v2

import models._
import models.viewmodels.{ApplicationTabViewModel, ApplicationsTab}
import utils.Cases
import views.ViewMatchers.{containElementWithClass, containElementWithID, containText, haveClass}
import views.ViewSpec
import views.html.v2.my_cases_view

import java.time.Instant

class MyCasesViewSpec extends ViewSpec {

  val referredEvents: Map[String, Event] =
    Map(
      "1" -> Event(
        "One",
        ReferralCaseStatusChange(
          CaseStatus.OPEN,
          Some("some comment"),
          None,
          "Laboratory analyst",
          Seq(ReferralReason.REQUEST_MORE_INFO)
        ),
        Operator("operator-id1", Some("Billy Bobbins")),
        "12345671",
        Instant.now()
      ),
      "2" -> Event(
        "Two",
        ReferralCaseStatusChange(
          CaseStatus.OPEN,
          Some("another comment"),
          None,
          "Trader",
          Seq(ReferralReason.REQUEST_SAMPLE)
        ),
        Operator("operator-id2", Some("Sally Sobbins")),
        "12345672",
        Instant.now()
      ),
      "3" -> Event(
        "Three",
        ReferralCaseStatusChange(
          CaseStatus.OPEN,
          Some("another comment"),
          None,
          "Other reason",
          Seq(ReferralReason.REQUEST_MORE_INFO)
        ),
        Operator("operator-id3", Some("Willy Wobbins")),
        "12345673",
        Instant.now()
      ),
      "4" -> Event(
        "Four",
        ReferralCaseStatusChange(
          CaseStatus.OPEN,
          Some("another comment for misc"),
          None,
          "Other reason",
          Seq(ReferralReason.REQUEST_MORE_INFO)
        ),
        Operator("operator-id4", Some("Gilly Gobbins")),
        "12345674",
        Instant.now()
      )
    )

  val completedEvents: Map[String, Event] =
    Map(
      "1" -> Event(
        "One",
        CompletedCaseStatusChange(
          CaseStatus.OPEN,
          Some("some comment"),
          Some("completedEvent1@event.com")
        ),
        Operator("operator-id11", Some("Philly Phobbins")),
        "12345681",
        Instant.now()
      ),
      "2" -> Event(
        "Two",
        CompletedCaseStatusChange(
          CaseStatus.OPEN,
          Some("some comment"),
          Some("completedEvent2@event.com")
        ),
        Operator("operator-id12", Some("Tilly Tobbins")),
        "12345682",
        Instant.now()
      ),
      "3" -> Event(
        "Three",
        CompletedCaseStatusChange(
          CaseStatus.OPEN,
          Some("some comment"),
          Some("completedEvent3@event.com")
        ),
        Operator("operator-id13", Some("Jilly Jobbins")),
        "12345683",
        Instant.now()
      ),
      "4" -> Event(
        "Four",
        CompletedCaseStatusChange(
          CaseStatus.OPEN,
          Some("some comment"),
          Some("completedEvent4@event.com")
        ),
        Operator("operator-id14", Some("Lilly Lobbins")),
        "12345684",
        Instant.now()
      )
    )

  val assignedToMeCasesTab: ApplicationTabViewModel =
    ApplicationTabViewModel(
      "message key",
      ApplicationsTab
        .assignedToMeCases(
          Seq(
            Cases.btiCaseExample,
            Cases.liabilityCaseExample,
            Cases.correspondenceCaseExample,
            Cases.miscellaneousCaseExample
          )
        )
        .applicationTabs
    )

  val referredByMeCasesTab: ApplicationTabViewModel =
    ApplicationTabViewModel(
      "applicationTab.referredByMe",
      ApplicationsTab
        .referredByMe(
          Seq(
            Cases.btiCaseExample.copy(status          = CaseStatus.REFERRED),
            Cases.liabilityCaseExample.copy(reference = "2", status = CaseStatus.REFERRED, referredDaysElapsed = 65),
            Cases.btiCaseExample.copy(reference       = "3", status = CaseStatus.SUSPENDED, daysElapsed = 30),
            Cases.newLiabilityLiveCaseExample
              .copy(reference                              = "4", status = CaseStatus.REFERRED, daysElapsed = 5, referredDaysElapsed = 6),
            Cases.correspondenceCaseExample.copy(reference = "6", status = CaseStatus.REFERRED, daysElapsed = 12),
            Cases.miscellaneousCaseExample.copy(reference  = "5", status = CaseStatus.REFERRED, daysElapsed = 35)
          ),
          referredEvents
        )
        .applicationTabs
    )

  val completedByMeCasesTab: ApplicationTabViewModel =
    ApplicationTabViewModel(
      "applicationTab.completedByMe",
      ApplicationsTab
        .completedByMe(
          Seq(
            Cases.btiCaseExample.copy(status               = CaseStatus.COMPLETED),
            Cases.liabilityCaseExample.copy(reference      = "2", status = CaseStatus.COMPLETED),
            Cases.correspondenceCaseExample.copy(reference = "3", status = CaseStatus.COMPLETED),
            Cases.miscellaneousCaseExample.copy(reference  = "4", status = CaseStatus.COMPLETED)
          ),
          completedEvents
        )
        .applicationTabs
    )

  def myCasesView: my_cases_view = injector.instanceOf[my_cases_view]

  "myCasesViewSpec" should {

    // Assigned to me Sub Menu

    "render successfully" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("my-cases-tabs")
    }

    "contain my_cases_secondary_navigation" in {
      val doc = view(myCasesView(assignedToMeCasesTab)(authenticatedManagerFakeRequest, messages, appConfig))

      doc should containElementWithID("my-cases-sub-nav")
    }

    "contain my cases component" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
      doc should containElementWithID("correspondence_tab")
      doc should containElementWithID("miscellaneous_tab")
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

    "contain correspondence table" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("applicationTab.correspondence-table")
    }

    "contain miscellaneous table" in {
      val doc = view(myCasesView(assignedToMeCasesTab))

      doc should containElementWithID("applicationTab.miscellaneous-table")

    }

    // Referred By Me Sub Menu

    "contain my_cases_secondary_navigation for Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab)(authenticatedManagerFakeRequest, messages, appConfig))

      doc should containElementWithID("my-cases-sub-nav")
    }

    "contain my cases component for Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
      doc should containElementWithID("correspondence_tab")
      doc should containElementWithID("miscellaneous_tab")
    }

    "contain a heading for Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("common-cases-heading")
    }

    "contain atar table in Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.atar-table")
    }

    "contain liabilities table in Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.liability-table")
    }

    "contain correspondence table in Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.correspondence-table")
    }

    "contain miscellaneous table in Referred by me" in {
      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.miscellaneous-table")
    }

    "contain a referral event in liabilities tab for Referred by me" in {

      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.liability-status-label-0-status")

    }

    "contain a referral event in atar tab for Referred by me" in {

      val doc = view(myCasesView(referredByMeCasesTab))

      doc should containElementWithID("applicationTab.atar-status-label-0-status")

    }

    "contain referral reason in  atar tab for Referred by me" in {

      val doc = view(myCasesView(referredByMeCasesTab))

      doc.getElementById("applicationTab.atar-status-refer-to-0").text shouldBe "Laboratory analyst"

    }

    "contain referral reason in  liability tab for Referred by me" in {

      val doc = view(myCasesView(referredByMeCasesTab))

      doc.getElementById("applicationTab.liability-status-refer-to-0").text shouldBe "Trader"
      doc.getElementById("applicationTab.atar-status-label-1-overdue").text shouldBe "OVERDUE"
      doc.getElementById("applicationTab.atar-elapsed-days-1")              should haveClass("live-red-text")
      doc.getElementById("applicationTab.liability-refer-days-0").text      shouldBe "65"
      doc.getElementById("applicationTab.liability-type-1").text            should include("LIVE")
      doc.getElementById("applicationTab.liability-status-1").text          should include("Other reason")

    }

    // Completed by me Sub Menu

    "contain my_cases_secondary_navigation for Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab)(authenticatedManagerFakeRequest, messages, appConfig))
      doc should containElementWithID("my-cases-sub-nav")
    }

    "contain my cases component for Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("atar_tab")
      doc should containElementWithID("liability_tab")
      doc should containElementWithID("correspondence_tab")
      doc should containElementWithID("miscellaneous_tab")
    }

    "contain a heading for Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("common-cases-heading")
    }

    "contain atar table in Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("applicationTab.atar-table")
    }

    "contain liabilities table in Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("applicationTab.liability-table")
    }

    "contain correspondence table in Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("applicationTab.correspondence-table")
    }

    "contain miscellaneous table in Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("applicationTab.miscellaneous-table")
    }

    "contain a completed status in ATaR table for Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc                                                             should containElementWithID("applicationTab.atar-status-label-0-status")
      doc                                                             should containElementWithClass("govuk-tag govuk-tag--green")
      doc.getElementById("applicationTab.atar-status-label-0-status") should containText("COMPLETED")
    }

    "contain a completed event in ATaR tab for Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("applicationTab.atar-completed-date-0")
    }

    "contain a completed event in Liabilities tab for Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("applicationTab.liability-completed-date-0")
    }

    "contain a completed event in Correspondence tab for Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("applicationTab.correspondence-completed-date-0")
    }

    "contain a completed event in Miscellaneous tab for Completed by me" in {
      val doc = view(myCasesView(completedByMeCasesTab))

      doc should containElementWithID("applicationTab.miscellaneous-completed-date-0")
    }

  }

}
