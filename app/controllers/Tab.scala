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
  // Common
  case object SampleTab extends Tab("sample_status_tab")
  case object AttachmentsTab extends Tab("attachments_tab")
  case object ActivityTab extends Tab("activity_tab")
  case object KeywordsTab extends Tab("keywords_tab")
  case object RulingTab extends Tab("ruling_tab")
  case object AppealsTab extends Tab("appeal_tab")

  // ATaRs
  case object ApplicantTab extends Tab("applicant_tab")
  case object GoodsTab extends Tab("goods_tab")

  // Liabilities
  case object C592Tab extends Tab("c592_tab")

  val values =
    Set(SampleTab, AttachmentsTab, ActivityTab, KeywordsTab, RulingTab, AppealsTab, ApplicantTab, GoodsTab, C592Tab)

  def fromValue(value: String): Option[Tab] = values.find(_.name == value)
}
