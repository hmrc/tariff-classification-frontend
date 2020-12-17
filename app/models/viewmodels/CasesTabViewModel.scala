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

import models._

import java.time.Instant

case class CasesTab(tabMessageKey: String, elementId: String, searchResult: Paged[Case])

case class CasesTabViewModel(headingMessageKey: String, caseType: ApplicationType, casesTabs: List[CasesTab])

object CasesTab {

  def act(searchResult: Paged[Case]  = Paged.empty) = CasesTab("cases.opencases.tab_ACT", "act_tab", searchResult)
  def cap(searchResult: Paged[Case]  = Paged.empty) = CasesTab("cases.opencases.tab_CAP", "cap_tab", searchResult)
  def cars(searchResult: Paged[Case] = Paged.empty) = CasesTab("cases.opencases.tab_CARS", "cars_tab", searchResult)
  def elm(searchResult: Paged[Case]  = Paged.empty) = CasesTab("cases.opencases.tab_ELM", "elm_tab", searchResult)
  def flex(searchResult: Paged[Case] = Paged.empty) = CasesTab("cases.opencases.tab_FLEX", "flex_tab", searchResult)
  def tta(searchResult: Paged[Case]  = Paged.empty) = CasesTab("cases.opencases.tab_TTA", "tta_tab", searchResult)
  def ttb(searchResult: Paged[Case]  = Paged.empty) = CasesTab("cases.opencases.tab_TTB", "ttb_tab", searchResult)
  def ttc(searchResult: Paged[Case]  = Paged.empty) = CasesTab("cases.opencases.tab_TTC", "ttc_tab", searchResult)

}

object CasesTabViewModel {

  def atarCases(allQueueCases: Seq[Case]): CasesTabViewModel = {

    val actCases  = allQueueCases.filter(c => c.queueId.contains(Queues.act.id) && c.application.isBTI)
    val capCases  = allQueueCases.filter(c => c.queueId.contains(Queues.cap.id) && c.application.isBTI)
    val carsCases = allQueueCases.filter(c => c.queueId.contains(Queues.cars.id) && c.application.isBTI)
    val elmCases  = allQueueCases.filter(c => c.queueId.contains(Queues.elm.id) && c.application.isBTI)

    CasesTabViewModel(
      "cases.opencases.atar.heading",
      ApplicationType.ATAR,
      List(
        CasesTab.act(Paged(actCases)),
        CasesTab.cap(Paged(capCases)),
        CasesTab.cars(Paged(carsCases)),
        CasesTab.elm(Paged(elmCases))
      )
    )
  }

  def liabilityCases(allQueueCases: Seq[Case]): CasesTabViewModel = {

    val liabilityCases = allQueueCases.filter(_.application.isLiabilityOrder)
    val actCases       = liabilityCases.filter(c => c.queueId.contains(Queues.act.id))
    val capCases       = liabilityCases.filter(c => c.queueId.contains(Queues.cap.id))
    val carsCases      = liabilityCases.filter(c => c.queueId.contains(Queues.cars.id))
    val elmCases       = liabilityCases.filter(c => c.queueId.contains(Queues.elm.id))

    CasesTabViewModel(
      "cases.opencases.liability.heading",
      ApplicationType.LIABILITY,
      List(
        CasesTab.act(Paged(actCases)),
        CasesTab.cap(Paged(capCases)),
        CasesTab.cars(Paged(carsCases)),
        CasesTab.elm(Paged(elmCases))
      )
    )
  }

  def correspondenceCases(allQueueCases: Seq[Case]): CasesTabViewModel = {

    val correspondenceCases = allQueueCases.filter(_.application.isCorrespondence)

    val actCases  = correspondenceCases.filter(_.queueId.contains(Queues.act.id))
    val capCases  = correspondenceCases.filter(_.queueId.contains(Queues.cap.id))
    val carsCases = correspondenceCases.filter(_.queueId.contains(Queues.cars.id))
    val elmCases  = correspondenceCases.filter(_.queueId.contains(Queues.elm.id))

    CasesTabViewModel(
      "cases.opencases.correspondence.heading",
      ApplicationType.CORRESPONDENCE,
      List(
        CasesTab.act(Paged(actCases)),
        CasesTab.cars(Paged(capCases)),
        CasesTab.elm(Paged(carsCases)),
        CasesTab.flex(Paged(elmCases))
      )
    )
  }

  def miscellaneous = CasesTabViewModel(
    "cases.opencases.miscellaneous.heading",
    ApplicationType.ATAR,
    List(
      CasesTab.act(),
      CasesTab.cars(),
      CasesTab.elm(),
      CasesTab.flex(),
      CasesTab.tta(),
      CasesTab.ttb(),
      CasesTab.ttc()
    )
  )

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
    CaseStatus.OPEN,
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
