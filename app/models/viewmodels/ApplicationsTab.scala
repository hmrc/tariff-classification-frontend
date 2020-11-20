package models.viewmodels

import models.{ApplicationType, Case, Paged}

case class ApplicationsTab(tabMessageKey: String, elementId : String,  searchResult: Paged[Case])

case class ApplicationTabViewModel(headingMessageKey: String, caseType: ApplicationType, applicationTabs: List[ApplicationsTab])

object ApplicationsTab {

  def atar(searchResult : Paged[Case] = Paged.empty) =  ApplicationsTab("applicationTab.assignedToMe.atar", "atar_tab", searchResult)
  def liability(searchResult : Paged[Case] = Paged.empty) =  ApplicationsTab("applicationTab.assignedToMe.liability", "liability_tab", searchResult)
  def correspondence(searchResult : Paged[Case] = Paged.empty) =  ApplicationsTab("applicationTab.assignedToMe.correspondence", "correspondence_tab", searchResult)
  def miscellaneous(searchResult : Paged[Case] = Paged.empty) =  ApplicationsTab("applicationTab.assignedToMe.miscellaneous", "miscellaneous_tab", searchResult)

}

