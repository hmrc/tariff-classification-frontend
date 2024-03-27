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

package utils

import java.time.{Instant, LocalDate, ZoneOffset}

class DatesSpec extends UtilsBaseSpec {

  private val date: Instant = LocalDate
    .of(2018, 1, 1)
    .atStartOfDay(ZoneOffset.UTC)
    .toInstant

  "Format" should {

    "convert instant to string" in {
      val output = Dates.format(date)

      output shouldBe "01 Jan 2018"
    }

    "convert Option(instant) to string" in {
      val output = Dates.format(Some(date))

      output shouldBe "01 Jan 2018"
    }

    "convert None to string" in {
      val output = Dates.format(None)

      output shouldBe "None"
    }

  }
}
