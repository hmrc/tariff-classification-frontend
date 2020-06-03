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

package models

class ReusableTabIndexerTest extends ModelsBaseSpec {

  "ReusableTabIndexer" should {

    "start by default with expected first index" in {
      val tabIndexer = ReusableTabIndexer()
      tabIndexer.nextTabIndex() shouldBe 0
    }

    "increment by default with expected increment" in {
      val tabIndexer = ReusableTabIndexer()
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndex() shouldBe 2
    }

    "increment by jump with expected increment" in {
      val tabIndexer = ReusableTabIndexer()
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndexWithJump(50) shouldBe 50
    }

    "return current index without consuming index when calling current tab index" in {
      val tabIndexer = ReusableTabIndexer(startTabIndex = 5)
      tabIndexer.nextTabIndex()
      tabIndexer.currentTabIndex() shouldBe 5
      tabIndexer.currentTabIndex() shouldBe 5
    }

    "start at specified index when supplied" in {
      val tabIndexer = ReusableTabIndexer(startTabIndex = 10)
      tabIndexer.nextTabIndex() shouldBe 10
    }

    "increment by specified increment when supplied" in {
      val tabIndexer = ReusableTabIndexer(indexIncrement = 100)
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndex() shouldBe 200
    }

  }

}
