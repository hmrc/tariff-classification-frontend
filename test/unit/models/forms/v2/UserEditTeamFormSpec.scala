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

package models.forms.v2

import models.ModelsBaseSpec

class UserEditTeamFormSpec extends ModelsBaseSpec {

  "Bind from request" should {

    "Bind a blank form" in {
      val form = UserEditTeamForm.editTeamsForm.bindFromRequest(
        Map(
          "memberOfTeams" -> Seq()
        )
      )

      form.hasErrors shouldBe false
      form.errors should have(size(0))
    }

  }

  "Fill" should {
    "populate a correct form" in {
      val form = UserEditTeamForm.editTeamsForm.fill(
        Set("1", "2", "3", "4")
      )

      form.hasErrors shouldBe false
      form.data shouldBe Map(
        "memberOfTeams[0]" -> "1",
        "memberOfTeams[1]" -> "2",
        "memberOfTeams[2]" -> "3",
        "memberOfTeams[3]" -> "4"
      )
    }
  }
}
