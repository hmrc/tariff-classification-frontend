/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.{Clock, LocalDate, ZoneId}

import uk.gov.hmrc.play.test.UnitSpec

class CaseSpec extends UnitSpec {

  "Case 'Elapsed Days'" should {
    val zone = ZoneId.of("UTC")
    val now = LocalDate.of(2018, 1, 20).atStartOfDay().atZone(zone).toInstant
    val created = LocalDate.of(2018,1,1).atStartOfDay().atZone(zone)
    val closed = LocalDate.of(2018,1,10).atStartOfDay().atZone(zone)

    "calculate for a closed case" in {
      val c = Case("", CaseStatus.COMPLETED, null, created, Some(closed), None, None, None, null, None, Seq.empty)
      c.elapsedDays shouldBe 9
    }

    "calculate for an open case" in {
      val clockWithFixedTime = Clock.fixed(now, zone)
      val c = Case("", CaseStatus.OPEN, null, created, None, None, None, None, null, None, Seq.empty)
      c.elapsedDays(clockWithFixedTime) shouldBe 19
    }

  }

}
