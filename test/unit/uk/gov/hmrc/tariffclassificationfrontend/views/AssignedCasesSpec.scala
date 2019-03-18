/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.views

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus._
import uk.gov.hmrc.tariffclassificationfrontend.models.Operator
import uk.gov.tariffclassificationfrontend.utils.Cases._

class AssignedCasesSpec extends UnitSpec {


  "AssignedCases" should {
    "create for missing operator" in {
      val maybeCases = AssignedCases.apply(Seq.empty, None)
      maybeCases shouldBe empty
    }

    "create for operator with no cases " in {
      val assigned = AssignedCases.apply(Seq(buildCaseFor("2", OPEN)), Some("1")).get

      assigned.name shouldBe ""
      assigned.openCases shouldBe empty
      assigned.otherCases shouldBe empty
    }

    "create for operator with open cases " in {
      val openCase = buildCaseFor("1", OPEN)

      val assigned = AssignedCases.apply(Seq(openCase), Some("1")).get

      assigned.name shouldBe "Test User"
      assigned.openCases shouldBe Seq(openCase)
      assigned.otherCases shouldBe empty
    }

    "create for operator with multiple cases " in {
      val openCase = buildCaseFor("1", OPEN)
      val referredCase = buildCaseFor("1", REFERRED)
      val suspendedCase = buildCaseFor("1", SUSPENDED)

      val assigned = AssignedCases.apply(Seq(openCase, referredCase, suspendedCase), Some("1")).get

      assigned.name shouldBe "Test User"
      assigned.openCases shouldBe Seq(openCase)
      assigned.otherCases shouldBe Seq(referredCase, suspendedCase)
    }
  }

  private def buildCaseFor(op: String, status: CaseStatus) = {
    aCase(
      withStatus(status),
      withAssignee(Some(Operator(op, Some("Test User"))))
    )
  }

}
