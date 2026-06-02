/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class SearchTabSpec extends AnyWordSpec with Matchers {

  import SearchTab._

  "SearchTab QueryStringBindable" should {

    "bind 'details' correctly" in {
      val result = searchTypeQueryStringBinder.bind("tab", Map("tab" -> Seq("details")))
      result shouldBe Some(Right(SearchTab.DETAILS))
    }

    "bind 'images' correctly" in {
      val result = searchTypeQueryStringBinder.bind("tab", Map("tab" -> Seq("images")))
      result shouldBe Some(Right(SearchTab.IMAGES))
    }

    "bind 'searchbox' correctly" in {
      val result = searchTypeQueryStringBinder.bind("tab", Map("tab" -> Seq("searchbox")))
      result shouldBe Some(Right(SearchTab.SEARCH_BOX))
    }

    "fail to bind invalid value" in {
      val result = searchTypeQueryStringBinder.bind("tab", Map("tab" -> Seq("invalid")))

      result            shouldBe defined
      result.get.isLeft shouldBe true
    }

    "unbind values correctly" in {
      searchTypeQueryStringBinder.unbind("tab", SearchTab.DETAILS)    shouldBe "tab=details"
      searchTypeQueryStringBinder.unbind("tab", SearchTab.IMAGES)     shouldBe "tab=images"
      searchTypeQueryStringBinder.unbind("tab", SearchTab.SEARCH_BOX) shouldBe "tab=searchbox"
    }
  }
}
