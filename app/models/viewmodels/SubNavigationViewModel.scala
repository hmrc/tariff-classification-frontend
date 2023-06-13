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

import play.api.mvc.QueryStringBindable

sealed abstract class SubNavigationTab(val id: String) extends Product with Serializable

object SubNavigationTab {
  val values: Set[SubNavigationTab] = Set(
    ATaRTab,
    LiabilitiesTab,
    CorrespondenceTab,
    MiscellaneousTab,
    AssignedToMeTab,
    ReferredByMeTab,
    CompletedByMeTab,
    ManagerToolsUsersTab,
    ManagerToolsKeywordsTab,
    ManagerToolsReportsTab
  )

  implicit def subNavigationTabQueryStringBindable(
    implicit stringBindable: QueryStringBindable[String]
  ): QueryStringBindable[SubNavigationTab] =
    new QueryStringBindable[SubNavigationTab] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, SubNavigationTab]] =
        stringBindable.bind(key, params).map {
          case Right(value) =>
            values
              .collectFirst { case tab if tab.id == value => Right(tab) }
              .getOrElse(
                Left("Invalid subnavigation tab")
              )
          case Left(_) => Left("Invalid subnavigation tab")
        }

      override def unbind(key: String, value: SubNavigationTab): String = stringBindable.unbind(key, value.id)
    }

}

case object ATaRTab extends SubNavigationTab("sub_nav_atar_tab")

case object LiabilitiesTab extends SubNavigationTab("sub_nav_liability_tab")

case object CorrespondenceTab extends SubNavigationTab("sub_nav_correspondence_tab")

case object MiscellaneousTab extends SubNavigationTab("sub_nav_miscellaneous_tab")

case object AssignedToMeTab extends SubNavigationTab("sub_nav_assigned_to_me_tab")

case object ReferredByMeTab extends SubNavigationTab("sub_nav_referred_by_me_tab")

case object CompletedByMeTab extends SubNavigationTab("sub_nav_completed_by_me_tab")

case object ManagerToolsUsersTab extends SubNavigationTab("sub_nav_manager_tools_users_tab")

case object ManagerToolsKeywordsTab extends SubNavigationTab("sub_nav_manager_tools_keywords_tab")

case object ManagerToolsReportsTab extends SubNavigationTab("sub_nav_manager_tools_reports_tab")

case class SubNavigationViewModel(selectedTab: SubNavigationTab)
