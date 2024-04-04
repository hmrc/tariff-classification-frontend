/*
 * Copyright 2024 HM Revenue & Customs
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

package models

case class ReusableTabIndexer(startTabIndex: Int = 0, indexIncrement: Int = 1) {

  private val tabIncrement             = indexIncrement
  private var nextIndex                = startTabIndex - tabIncrement
  val nextTabIndex: () => Int          = { () => nextIndex += tabIncrement; nextIndex }
  val nextTabIndexWithJump: Int => Int = { (jump: Int) => nextIndex += jump; nextIndex }
  val currentTabIndex: () => Int       = { () => nextIndex }
}
