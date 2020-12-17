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

package views

object CaseDetailPage extends Enumeration {
  type CaseDetailPage = Value
  val TRADER              = Value("trader")
  val RULING              = Value("ruling")
  val EDIT_RULING         = Value("edit.ruling")
  val APPLICATION_DETAILS = Value("application")
  val SAMPLE_DETAILS      = Value("sample")
  val ACTIVITY            = Value("activity")
  val ATTACHMENTS         = Value("attachments")
  val KEYWORDS            = Value("keywords")
  val APPEAL              = Value("appeal")
}
