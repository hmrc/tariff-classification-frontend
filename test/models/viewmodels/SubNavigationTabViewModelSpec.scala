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

package models.viewmodels

import models.ModelsBaseSpec

class SubNavigationTabViewModelSpec extends ModelsBaseSpec {

  "SubNavigationTab Binder" should {

    "Unbind Populated SubNavigationTab to Query String" in {
      SubNavigationTab.subNavigationTabQueryStringBindable
        .unbind("subNavigation_tab", ReferredByMeTab) shouldBe "subNavigation_tab=sub_nav_referred_by_me_tab"
    }

    "Bind populated query string" in {
      SubNavigationTab.subNavigationTabQueryStringBindable
        .bind("subNavigation_tab", Map("subNavigation_tab" -> Seq("sub_nav_referred_by_me_tab"))) shouldBe Some(
        Right(ReferredByMeTab)
      )
    }

    "Fail to bind to any value and return None" when {
      "a wrong key has been provided" in {
        SubNavigationTab.subNavigationTabQueryStringBindable
          .bind("wrongKey", Map("subNavigation_tab" -> Seq("sub_nav_referred_by_me_tab"))) shouldBe None
      }

      "a wrong parameter key has been provided" in {
        SubNavigationTab.subNavigationTabQueryStringBindable
          .bind("subNavigation_tab", Map("wrongKey" -> Seq("sub_nav_referred_by_me_tab"))) shouldBe None
      }

      "both wrong key and wrong parameter key has been provided" in {
        SubNavigationTab.subNavigationTabQueryStringBindable
          .bind("wrongKey", Map("wrongKey2" -> Seq("sub_nav_referred_by_me_tab"))) shouldBe None
      }
    }

    "Fail to bind to any value and return Left with the appropriate message" when {
      "the params does not include the item on the list" in {
        SubNavigationTab.subNavigationTabQueryStringBindable
          .bind("subNavigation_tab", Map("subNavigation_tab" -> Seq("tab-item-not-on-List"))) shouldBe Some(
          Left("Invalid subnavigation tab")
        )
      }
    }
  }
}
