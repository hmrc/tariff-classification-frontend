/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

sealed case class ActiveTab(name: String)

object ActiveTab {

  object Applicant extends ActiveTab("tab-item-Applicant")

  object Item extends ActiveTab("tab-item-Item")

  object Sample extends ActiveTab("tab-item-Sample")

  object Attachments extends ActiveTab("tab-item-Attachments")

  object Activity extends ActiveTab("tab-item-Activity")

  object Keywords extends ActiveTab("tab-item-Keywords")

  object Ruling extends ActiveTab("tab-item-Ruling")

  object Appeals extends ActiveTab("tab-item-Appeals")

  object Liability extends ActiveTab("tab-item-Liability")

}
