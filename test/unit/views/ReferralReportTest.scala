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

package views

import models.{CaseReportGroup, Queue, ReportResult}

class ReferralReportTest extends ViewSpec {

  "Referral Report" should {
    val queue1 = Queue("id1", "", "")
    val queue2 = Queue("id2", "", "")

    val report = new ReferralReport(
      Seq(
        ReportResult(Map(CaseReportGroup.QUEUE -> Some("id1")), Seq(1, 2, 3)),
        ReportResult(Map(CaseReportGroup.QUEUE -> Some("id2")), Seq(4, 5))
      )
    )

    "Calculate Count" in {
      report.count shouldBe 5
    }

    "Calculate Average" in {
      report.average shouldBe 3
    }

    "Calculate Count for group" in {
      report.countFor(queue1) shouldBe 3
      report.countFor(queue2) shouldBe 2
    }

    "Calculate Average for group" in {
      report.averageFor(queue1) shouldBe 2
      report.averageFor(queue2) shouldBe 5
    }
  }

}
