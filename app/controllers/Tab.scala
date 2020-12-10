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

//TODO move to models
object Tab extends Enumeration {
  val C592_TAB        = Value("c592_tab")
  val RULING_TAB      = Value("ruling_tab")
  val SAMPLE_TAB      = Value("sample_status_tab")
  val ATTACHMENTS_TAB = Value("attachments_tab")
  val ACTIVITY_TAB    = Value("activity_tab")
  val KEYWORDS_TAB    = Value("keywords_tab")

  implicit def toString(value: Value): String = value.toString

  def findAnchorInEnum(enumString: String): Option[Tab.Value] =
    Tab.values.find(element => "#" + element.toLowerCase().trim == enumString.toLowerCase().trim)

}
