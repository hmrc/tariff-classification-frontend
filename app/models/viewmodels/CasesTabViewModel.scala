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

import models.{ApplicationType, BTIApplication, Case, CaseStatus, Contact, EORIDetails, Paged, Sample}

case class CasesTab(tabMessageKey: String, elementId : String,  searchResult: Paged[Case])

case class CasesTabViewModel(headingMessageKey: String, caseType: ApplicationType, casesTabs: List[CasesTab])

object CasesTab {
  
  def act(searchResult : Paged[Case] = Paged.empty) =  CasesTab("cases.opencases.tab_ACT", "act_tab", searchResult)
  def cap(searchResult : Paged[Case] = Paged.empty) =  CasesTab("cases.opencases.tab_CAP", "cap_tab", searchResult)
  def cars(searchResult : Paged[Case] = Paged.empty) =  CasesTab("cases.opencases.tab_CARS", "cars_tab", searchResult)
  def elm(searchResult : Paged[Case] = Paged.empty) =  CasesTab("cases.opencases.tab_ELM", "elm_tab", searchResult)
  def flex(searchResult : Paged[Case] = Paged.empty) =  CasesTab("cases.opencases.tab_FLEX", "flex_tab", searchResult)
  def tta(searchResult : Paged[Case] = Paged.empty) =  CasesTab("cases.opencases.tab_TTA", "tta_tab", searchResult)
  def ttb(searchResult : Paged[Case] = Paged.empty) =  CasesTab("cases.opencases.tab_TTB", "ttb_tab", searchResult)
  def ttc(searchResult : Paged[Case] = Paged.empty) =  CasesTab("cases.opencases.tab_TTC", "ttc_tab", searchResult)
  
}

object CasesTabViewModel {
  val contactExample: Contact = Contact("name", "email", Some("phone"))

  val eoriDetailsExample: EORIDetails =
    EORIDetails("eori", "trader-business-name", "line1", "line2", "line3", "postcode", "country")

  val btiApplicationExample: BTIApplication = BTIApplication(
    eoriDetailsExample,
    contactExample,
    None,
    offline = false,
    "Laptop",
    "Personal Computer",
    None,
    None,
    None,
    None,
    Nil,
    None,
    None,
    sampleToBeProvided = false,
    sampleToBeReturned = false
  )


  def atar = CasesTabViewModel(
    "cases.opencases.atar.heading",
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

  def liability = CasesTabViewModel(
    "cases.opencases.liability.heading",
    ApplicationType.LIABILITY,
    List(
      CasesTab.act(),
      CasesTab.cap(),
      CasesTab.cars(),
      CasesTab.elm(),
      CasesTab.flex(),
      CasesTab.tta(),
      CasesTab.ttb(),
      CasesTab.ttc()
    )
  )

  def correspondence = CasesTabViewModel(
    "cases.opencases.correspondence.heading",
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
}
