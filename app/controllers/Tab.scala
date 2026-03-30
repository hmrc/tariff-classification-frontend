/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

sealed abstract class Tab(val name: String) extends Product with Serializable

object Tab {
  // Common
  case object SAMPLE_TAB extends Tab("sample_status_tab")
  case object ATTACHMENTS_TAB extends Tab("attachments_tab")
  case object ACTIVITY_TAB extends Tab("activity_tab")
  case object KEYWORDS_TAB extends Tab("keywords_tab")
  case object RULING_TAB extends Tab("ruling_tab")
  case object APPEALS_TAB extends Tab("appeal_tab")

  // ATaRs
  private case object APPLICANT_TAB extends Tab("applicant_tab")
  private case object GOODS_TAB extends Tab("goods_tab")

  // Liabilities
  case object C592_TAB extends Tab("c592_tab")

  // Correspondence
  private case object CASE_DETAILS_TAB extends Tab("case_details_tab")
  case object CONTACT_TAB extends Tab("contact_details_tab")
  case object MESSAGES_TAB extends Tab("messages_tab")

  private case object USER_DETAIS_TAB extends Tab("user_details_tab")
  case object ATAR_TAB extends Tab("atar_tab")
  case object LIABILITY_TAB extends Tab("liability_tab")
  case object CORRESPONDENCE_TAB extends Tab("correspondence_tab")
  case object MISCELLANEOUS_TAB extends Tab("miscellaneous_tab")

  val values: Set[Tab] =
    Set(
      SAMPLE_TAB,
      ATTACHMENTS_TAB,
      ACTIVITY_TAB,
      KEYWORDS_TAB,
      RULING_TAB,
      APPEALS_TAB,
      APPLICANT_TAB,
      GOODS_TAB,
      C592_TAB,
      CASE_DETAILS_TAB,
      CONTACT_TAB,
      MESSAGES_TAB
    )

  val userTabs: Set[Tab] = Set(
    USER_DETAIS_TAB,
    ATAR_TAB,
    LIABILITY_TAB,
    CORRESPONDENCE_TAB,
    MISCELLANEOUS_TAB
  )

  def fromValue(value: String): Option[Tab] = values.find(_.name == value)
}
