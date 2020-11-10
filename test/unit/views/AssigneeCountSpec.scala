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

package views

import models.Operator
import utils.Cases.{aCase, withAssignee}

class AssigneeCountSpec extends ViewSpec {

  "AssigneeCount for operator with two names" should {

    val summaryJoeSmith = AssigneeCount(Operator("1", Some("Joe  Smith")), 1)

    "return name" in {
      summaryJoeSmith.name shouldBe "Joe  Smith"
    }

    "return first name" in {
      summaryJoeSmith.firstName shouldBe "Joe"
    }

    "return last name" in {
      summaryJoeSmith.lastName shouldBe "Smith"
    }

  }

  "AssigneeCount for operator with one name" should {

    val summaryJoe = AssigneeCount(Operator("1", Some("Joe")), 1)

    "return name" in {
      summaryJoe.name shouldBe "Joe"
    }

    "return first name" in {
      summaryJoe.firstName shouldBe "Joe"
    }

    "return last name" in {
      summaryJoe.lastName shouldBe "Joe"
    }

  }

  "AssigneeCount for operator with no name" should {

    val summaryNone = AssigneeCount(Operator("1", None), 1)

    "return name" in {
      summaryNone.name shouldBe "PID 1"
    }

    "return first name" in {
      summaryNone.firstName shouldBe "PID"
    }

    "return last name" in {
      summaryNone.lastName shouldBe "1"
    }

  }

  "AssigneeCount" should {
    "create summary for list of cases" in {

      val op1 = Operator("1", Some("Ann Smith"))
      val op2 = Operator("2", Some("Zac Jones"))
      val op3 = Operator("3") // 'name' is "PID 3"

      val cases = Seq(
        aCase(withAssignee(Some(op1))),
        aCase(withAssignee(Some(op1))),
        aCase(withAssignee(Some(op2))),
        aCase(withAssignee(Some(op3))),
        aCase(withAssignee(Some(op3))),
        aCase(withAssignee(Some(op3))),
        aCase(withAssignee(None)),
        aCase()
      )

      val summaries = AssigneeCount.apply(cases)

      summaries.head shouldBe AssigneeCount(op3, 3) // 3, PID
      summaries(1)   shouldBe AssigneeCount(op2, 1) // Jones, Zac
      summaries(2)   shouldBe AssigneeCount(op1, 2) // Smith, Ann

      summaries.size shouldBe 3
    }
  }

}
