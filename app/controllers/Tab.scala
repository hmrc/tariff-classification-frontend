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

package controllers

sealed abstract class Tab(val name: String) extends Product with Serializable

object Tab {
  case object C592_TAB extends Tab("c592_tab")
  case object RULING_TAB extends Tab("ruling_tab")
  case object SAMPLE_TAB extends Tab("sample_status_tab")
  case object ATTACHMENTS_TAB extends Tab("attachments_tab")
  case object ACTIVITY_TAB extends Tab("activity_tab")
  case object KEYWORDS_TAB extends Tab("keywords_tab")

  val values = Set(C592_TAB, RULING_TAB, SAMPLE_TAB, ATTACHMENTS_TAB, ACTIVITY_TAB, KEYWORDS_TAB)

  def fromValue(value: String): Option[Tab] = values.find(_.name == value)

  def findAnchorInEnum(enumString: String): Option[Tab] =
    Tab.values.find(element => "#" + element.name.trim == enumString.toLowerCase().trim)
}
