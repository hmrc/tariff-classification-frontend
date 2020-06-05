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

class TabSpec extends ControllerBaseSpec {

  "findAnchorInEnum" should {

    "found enum when search as a anchor" in {
      val anchor = "#" + Tab.C592_TAB
      Tab.findAnchorInEnum(anchor).isDefined shouldBe true
    }

    "found enum when search as a anchor and contain trailing spaces" in {
      val anchor = "  #" + Tab.C592_TAB + "  "
      Tab.findAnchorInEnum(anchor).isDefined shouldBe true
    }

    "found enum when search as a anchor and contain upper case" in {
      val anchor = "#" + Tab.C592_TAB.toString.substring(0, 1).toUpperCase + Tab.C592_TAB.toString.substring(1)
      Tab.findAnchorInEnum(anchor).isDefined shouldBe true
    }

    "not found enum when search as a anchor without '#'" in {
      val anchor = Tab.C592_TAB
      Tab.findAnchorInEnum(anchor).isDefined shouldBe false
    }

    "not found enum when search as a anchor but invalid name" in {
      val anchor = Tab.C592_TAB.toString + Tab.ACTIVITY_TAB.toString
      Tab.findAnchorInEnum(anchor).isDefined shouldBe false
    }
  }
}
