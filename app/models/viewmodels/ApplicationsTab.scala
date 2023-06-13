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

import models._

case class ApplicationsTab(
  tabMessageKey: String,
  applicationType: ApplicationType,
  elementId: String,
  searchResult: Paged[Case],
  referralEvent: Option[Map[String, Event]]  = None,
  completedEvent: Option[Map[String, Event]] = None
)

case class ApplicationTabViewModel(headingMessageKey: String, applicationTabs: List[ApplicationsTab])

object ApplicationsTab {

  def atar(
    searchResult: Paged[Case]                  = Paged.empty,
    referralEvent: Option[Map[String, Event]]  = None,
    completedEvent: Option[Map[String, Event]] = None
  ): ApplicationsTab =
    ApplicationsTab(
      "applicationTab.atar",
      ApplicationType.ATAR,
      "atar_tab",
      searchResult,
      referralEvent,
      completedEvent
    )

  def liability(
    searchResult: Paged[Case]                  = Paged.empty,
    referralEvent: Option[Map[String, Event]]  = None,
    completedEvent: Option[Map[String, Event]] = None
  ): ApplicationsTab =
    ApplicationsTab(
      "applicationTab.liability",
      ApplicationType.LIABILITY,
      "liability_tab",
      searchResult,
      referralEvent,
      completedEvent
    )

  def correspondence(
    searchResult: Paged[Case]                  = Paged.empty,
    referralEvent: Option[Map[String, Event]]  = None,
    completedEvent: Option[Map[String, Event]] = None
  ): ApplicationsTab =
    ApplicationsTab(
      "applicationTab.correspondence",
      ApplicationType.CORRESPONDENCE,
      "correspondence_tab",
      searchResult,
      referralEvent,
      completedEvent
    )

  def miscellaneous(
    searchResult: Paged[Case]                  = Paged.empty,
    referralEvent: Option[Map[String, Event]]  = None,
    completedEvent: Option[Map[String, Event]] = None
  ): ApplicationsTab =
    ApplicationsTab(
      "applicationTab.miscellaneous",
      ApplicationType.MISCELLANEOUS,
      "miscellaneous_tab",
      searchResult,
      referralEvent,
      completedEvent
    )

  def assignedToMeCases(cases: Seq[Case]): ApplicationTabViewModel = {

    val assignedCases =
      cases.filter(aCase => aCase.status == CaseStatus.OPEN)

    val atars = assignedCases.filter(_.application.isBTI)

    val liabilities = assignedCases.filter(_.application.isLiabilityOrder)

    val correspondence = assignedCases.filter(_.application.isCorrespondence)

    val miscellaneous = assignedCases.filter(_.application.isMisc)

    ApplicationTabViewModel(
      "applicationTab.assignedToMe",
      List(
        ApplicationsTab.atar(Paged(atars)),
        ApplicationsTab.liability(Paged(liabilities)),
        ApplicationsTab.correspondence(Paged(correspondence)),
        ApplicationsTab.miscellaneous(Paged(miscellaneous))
      )
    )
  }

  def referredByMe(cases: Seq[Case], referralEvent: Map[String, Event]): ApplicationTabViewModel = {

    val referredCases =
      cases.filter(aCase => aCase.status == CaseStatus.REFERRED || aCase.status == CaseStatus.SUSPENDED)

    val atars: Seq[Case] = referredCases.filter(_.application.isBTI)

    val liabilities = referredCases.filter(_.application.isLiabilityOrder)

    val correspondence = referredCases.filter(_.application.isCorrespondence)

    val miscellaneous = referredCases.filter(_.application.isMisc)

    ApplicationTabViewModel(
      "applicationTab.referredByMe",
      List(
        ApplicationsTab.atar(Paged(atars), Some(referralEvent)),
        ApplicationsTab.liability(Paged(liabilities), Some(referralEvent)),
        ApplicationsTab.correspondence(Paged(correspondence)),
        ApplicationsTab.miscellaneous(Paged(miscellaneous), Some(referralEvent))
      )
    )
  }

  def completedByMe(cases: Seq[Case], completedEvent: Map[String, Event]): ApplicationTabViewModel = {

    val completeByMe =
      cases.filter(aCase => aCase.status == CaseStatus.COMPLETED)

    val atars: Seq[Case] = completeByMe.filter(_.application.isBTI)

    val liabilities = completeByMe.filter(_.application.isLiabilityOrder)

    val correspondence = completeByMe.filter(_.application.isCorrespondence)

    val miscellaneous = completeByMe.filter(_.application.isMisc)

    ApplicationTabViewModel(
      "applicationTab.completedByMe",
      List(
        ApplicationsTab.atar(Paged(atars), None, Some(completedEvent)),
        ApplicationsTab.liability(Paged(liabilities), None, Some(completedEvent)),
        ApplicationsTab.correspondence(Paged(correspondence), None, Some(completedEvent)),
        ApplicationsTab.miscellaneous(Paged(miscellaneous), None, Some(completedEvent))
      )
    )
  }

  def gateway(cases: Seq[Case]): ApplicationTabViewModel = {

    val gatewayCases =
      cases.filter(aCase => aCase.status == CaseStatus.NEW)

    val atars = gatewayCases.filter(_.application.isBTI)

    val liabilities = gatewayCases.filter(_.application.isLiabilityOrder)

    val correspondenceCases = gatewayCases.filter(_.application.isCorrespondence)

    val miscellaneous = gatewayCases.filter(_.application.isMisc)

    ApplicationTabViewModel(
      "applicationTab.gateway",
      List(
        ApplicationsTab.atar(Paged(atars)),
        ApplicationsTab.liability(Paged(liabilities)),
        ApplicationsTab.correspondence(Paged(correspondenceCases)),
        ApplicationsTab.miscellaneous(Paged(miscellaneous))
      )
    )
  }

  def casesByTypes(cases: Seq[Case]): ApplicationTabViewModel = {

    val atars = cases
      .filter(_.application.isBTI)
      .sortBy(aCase => (aCase.status.toString, aCase.daysElapsed, aCase.reference))(
        Ordering.Tuple3(Ordering.String.reverse, Ordering.Long.reverse, Ordering.String)
      )

    val liabilities = cases
      .filter(_.application.isLiabilityOrder)
      .sortBy(aCase => (aCase.status.toString, aCase.daysElapsed, aCase.reference))(
        Ordering.Tuple3(Ordering.String.reverse, Ordering.Long.reverse, Ordering.String)
      )

    val correspondence = cases
      .filter(_.application.isCorrespondence)
      .sortBy(aCase => (aCase.status.toString, aCase.daysElapsed, aCase.reference))(
        Ordering.Tuple3(Ordering.String.reverse, Ordering.Long.reverse, Ordering.String)
      )

    val miscellaneous = cases
      .filter(_.application.isMisc)
      .sortBy(aCase => (aCase.status.toString, aCase.daysElapsed, aCase.reference))(
        Ordering.Tuple3(Ordering.String.reverse, Ordering.Long.reverse, Ordering.String)
      )

    ApplicationTabViewModel(
      "applicationTab.userCases",
      List(
        ApplicationsTab.atar(Paged(atars)),
        ApplicationsTab.liability(Paged(liabilities)),
        ApplicationsTab.correspondence(Paged(correspondence)),
        ApplicationsTab.miscellaneous(Paged(miscellaneous))
      )
    )
  }
}
