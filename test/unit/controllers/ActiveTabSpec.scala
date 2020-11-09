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

class ActiveTabSpec extends ControllerBaseSpec {

  "ActiveTab Binder" should {

    "Unbind Populated ActivityTab to Query String" in {
      ActiveTab.queryStringBindable.unbind("sort_by", ActiveTab.Activity) shouldBe "activeTab=tab-item-Activity"
    }

    "Bind populated query string" in {
      ActiveTab.queryStringBindable.bind("activeTab", Map("activeTab" -> Seq("tab-item-Activity"))) shouldBe Some(
        Right(ActiveTab.Activity)
      )
    }

    "Fail to bind to any value and return None" when {
      "a wrong key has been provided" in {
        ActiveTab.queryStringBindable.bind("wrongKey", Map("activeTab" -> Seq("tab-item-Activity"))) shouldBe None
      }

      "a wrong parameter key has been provided" in {
        ActiveTab.queryStringBindable.bind("activeTab", Map("wrongKey" -> Seq("tab-item-Activity"))) shouldBe None
      }

      "both wrong key and wrong parameter key has been provided" in {
        ActiveTab.queryStringBindable.bind("wrongKey", Map("wrongKey2" -> Seq("tab-item-Activity"))) shouldBe None
      }
    }

    "Fail to bind to any value and return Left with the appropriate message" when {
      "the params does not include the item on the list" in {
        ActiveTab.queryStringBindable.bind("activeTab", Map("activeTab" -> Seq("tab-item-not-on-List"))) shouldBe Some(
          Left("Unable to bind Active Tab to valid value")
        )
      }
    }
  }
}
