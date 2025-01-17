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

package models

class OperatorSpec extends ModelsBaseSpec {
  "Operator 'manager'" should {

    "return true for an operator with manager role" in {
      val op = Operator("1", role = Role.CLASSIFICATION_MANAGER)
      op.manager shouldBe true
    }

    "return false for an operator without manager role" in {
      val op = Operator("1", role = Role.CLASSIFICATION_OFFICER)
      op.manager shouldBe false
    }

  }

  "Operator 'safeName'" should {

    "return the name if it's present" in {
      val name = "Name"
      val op   = Operator("1", name = Some(name), role = Role.CLASSIFICATION_MANAGER)
      op.safeName shouldBe name
    }

    "return the pid if name is not present" in {
      val pid = "1"
      val op  = Operator(pid, role = Role.CLASSIFICATION_OFFICER)
      op.safeName shouldBe "PID " + pid
    }

  }

  "Operator 'getMemberTeamNames'" should {

    "return names of teams the operator belongs to" in {
      val teamIds   = Seq("1", "2")
      val teamNames = Seq("Gateway", "ACT")
      val op        = Operator("1", memberOfTeams = teamIds, role = Role.CLASSIFICATION_MANAGER)
      op.getMemberTeamNames shouldBe teamNames
    }

  }

  "Operator 'isGateway'" should {

    "return true if operator belongs to gateway team" in {
      val teamIds = Seq("1", "2")
      val op      = Operator("1", memberOfTeams = teamIds, role = Role.CLASSIFICATION_MANAGER)
      op.isGateway shouldBe true
    }

    "return false if operator does not belong to gateway team" in {
      val teamIds = Seq("2")
      val op      = Operator("1", memberOfTeams = teamIds, role = Role.CLASSIFICATION_MANAGER)
      op.isGateway shouldBe false
    }

  }
}
