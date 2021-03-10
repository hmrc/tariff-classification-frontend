/*
 * Copyright 2021 HM Revenue & Customs
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

import java.net.URLDecoder
import java.time.Instant

class InstantRangeTest extends ModelsBaseSpec {

  private val range = InstantRange(
    min = Instant.EPOCH,
    max = Instant.EPOCH.plusSeconds(1)
  )

  private val params: Map[String, Seq[String]] = Map(
    "min_x" -> Seq("1970-01-01T00:00:00Z"),
    "max_x" -> Seq("1970-01-01T00:00:01Z")
  )

  "Range Binder" should {

    "Unbind Populated Range to Query String" in {
      val populatedQueryParam: String =
        "min_x=1970-01-01T00:00:00Z&max_x=1970-01-01T00:00:01Z"
      URLDecoder.decode(InstantRange.bindable.unbind("x", range), "UTF-8") shouldBe populatedQueryParam
    }

    "Bind empty query string" in {
      InstantRange.bindable.bind("x", Map()) shouldBe None
    }

    "Bind populated query string" in {
      InstantRange.bindable.bind("x", params) shouldBe Some(Right(range))
    }

    "Bind populated query string missing min" in {
      InstantRange.bindable.bind("x", params.filterNot(_._1 == "min_x")) shouldBe Some(Right(range.copy(min = Instant.MIN)))
    }

    "Bind populated query string missing max" in {
      InstantRange.bindable.bind("x", params.filterNot(_._1 == "max_x")) shouldBe Some(Right(range.copy(max = Instant.MAX)))
    }
  }

}
