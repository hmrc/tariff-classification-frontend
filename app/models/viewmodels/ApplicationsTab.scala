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
import models.{CaseStatus, _}

case class ApplicationsTab(
  tabMessageKey: String,
  applicationType: ApplicationType,
  elementId: String,
  searchResult: Paged[Case],
  referralEvent: Option[Map[String, ReferralCaseStatusChange]] = None
)

case class ApplicationTabViewModel(headingMessageKey: String, applicationTabs: List[ApplicationsTab])

object ApplicationsTab {

  def atar(
    searchResult: Paged[Case]                                    = Paged.empty,
    referralEvent: Option[Map[String, ReferralCaseStatusChange]] = None
  ) =
    ApplicationsTab("applicationTab.atar", ApplicationType.ATAR, "atar_tab", searchResult, referralEvent)

  def liability(
    searchResult: Paged[Case]                                    = Paged.empty,
    referralEvent: Option[Map[String, ReferralCaseStatusChange]] = None
  ) =
    ApplicationsTab("applicationTab.liability", ApplicationType.LIABILITY, "liability_tab", searchResult, referralEvent)

  def correspondence(searchResult: Paged[Case] = Paged.empty,
    referralEvent: Option[Map[String, ReferralCaseStatusChange]] = None
  ) =
    ApplicationsTab("applicationTab.correspondence", ApplicationType.CORRESPONDENCE, "correspondence_tab", searchResult)

  def miscellaneous(searchResult: Paged[Case] = Paged.empty) =
    ApplicationsTab("applicationTab.miscellaneous", ApplicationType.MISCELLANEOUS, "miscellaneous_tab", searchResult)
//todo remove dummy corr stub
  def assignedToMeCases(cases: Seq[Case]): ApplicationTabViewModel = {

    val atars = cases.filter(aCase => aCase.application.isBTI && aCase.status == CaseStatus.OPEN)

    val liabilities = cases.filter(aCase => aCase.application.isLiabilityOrder && aCase.status == CaseStatus.OPEN)

    ApplicationTabViewModel(
      "applicationTab.assignedToMe",
      List(
        ApplicationsTab.atar(Paged(atars)),
        ApplicationsTab.liability(Paged(liabilities)),
        ApplicationsTab.correspondence(Paged(Seq(corrCaseExample))),
        ApplicationsTab.miscellaneous()
      )
    )
  }

  def referredByMe(cases: Seq[Case], referralEvent: Map[String, ReferralCaseStatusChange]): ApplicationTabViewModel = {

    val referredCases =
      cases.filter(aCase => aCase.status == CaseStatus.REFERRED || aCase.status == CaseStatus.SUSPENDED)

    val atars: Seq[Case] = referredCases.filter(_.application.isBTI)

    val liabilities = referredCases.filter(_.application.isLiabilityOrder)


    ApplicationTabViewModel(
      "applicationTab.referredByMe",
      List(
        ApplicationsTab.atar(Paged(atars), Some(referralEvent)),
        ApplicationsTab.liability(Paged(liabilities), Some(referralEvent)),
        ApplicationsTab.correspondence(Paged(Seq(corrCaseExample)), Some(referralEvent)),
        ApplicationsTab.miscellaneous()
      )
    )
  }

  def completedByMe = ApplicationTabViewModel(
    "applicationTab.completedByMe",
    List(
      ApplicationsTab.atar(),
      ApplicationsTab.liability(),
      ApplicationsTab.correspondence(),
      ApplicationsTab.miscellaneous()
    )
  )
//TODO remove dummy corr stub
  def gateway(cases: Seq[Case]) = {
    val atars = cases.filter(aCase =>
      aCase.application.isBTI && aCase.status == CaseStatus.NEW)

    val liabilities = cases.filter(aCase =>
      aCase.application.isLiabilityOrder && aCase.status == CaseStatus.NEW)

    ApplicationTabViewModel(
      "applicationTab.gateway",
      List(
        ApplicationsTab.atar(Paged(atars)),
        ApplicationsTab.liability(Paged(liabilities)),
        ApplicationsTab.correspondence(Paged(Seq(corrCaseExample))),
        ApplicationsTab.miscellaneous()
      )
    )
  }

  //TODO remove dummy stubs
  //TODO remove dummy correspondence example when writing queries for the tabs
  val corrApplicationExample: CorrespondenceApplication = CorrespondenceApplication(
    Some("Starter"),
    Some("Agent 007"),
    Address("New building", "Old Town", None, None),
    Contact("a name", "anemail@some.com", None),
    None,
    false,
    "A short summary",
    "A detailed desc",
    None,
    sampleToBeProvided = false,
    sampleToBeReturned = false
  )

  val corrCaseExample: Case = Case(
    "1",
    CaseStatus.NEW,
    Instant.now(),
    0,
    None,
    None,
    None,
    corrApplicationExample,
    None,
    Seq(),
    Set.empty,
    Sample(),
    Some(Instant.now()),
    Some(5),
    referredDaysElapsed = 0
  )

}
