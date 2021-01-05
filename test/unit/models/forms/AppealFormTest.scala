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

package models.forms

import models.{AppealStatus, AppealType, ModelsBaseSpec}

class AppealFormTest extends ModelsBaseSpec {

  "Status Form: Bind from request" should {
    "Bind empty" in {
      val form = AppealForm.appealStatusForm.bindFromRequest(Map())

      form.hasErrors shouldBe true
    }

    "Bind blank" in {
      val form = AppealForm.appealStatusForm.bindFromRequest(Map("status" -> Seq("")))

      form.hasErrors shouldBe true
    }

    "Bind valid enum" in {
      val form = AppealForm.appealStatusForm.bindFromRequest(Map("status" -> Seq(AppealStatus.IN_PROGRESS.toString)))

      form.hasErrors shouldBe false
    }

    "Bind invalid enum" in {
      val form = AppealForm.appealStatusForm.bindFromRequest(Map("status" -> Seq("other")))

      form.hasErrors shouldBe true
    }
  }

  "Status Form: Fill" should {

    "populate some" in {
      val form = AppealForm.appealStatusForm.fill(AppealStatus.IN_PROGRESS)

      form.hasErrors shouldBe false
      form.data      shouldBe Map("status" -> "IN_PROGRESS")
    }
  }

  "Type Form: Bind from request" should {
    "Bind empty" in {
      val form = AppealForm.appealStatusForm.bindFromRequest(Map())

      form.hasErrors shouldBe true
    }

    "Bind blank" in {
      val form = AppealForm.appealTypeForm.bindFromRequest(Map("type" -> Seq("")))

      form.hasErrors shouldBe true
    }

    "Bind valid enum" in {
      val form = AppealForm.appealTypeForm.bindFromRequest(Map("type" -> Seq(AppealType.REVIEW.toString)))

      form.hasErrors shouldBe false
    }

    "Bind invalid enum" in {
      val form = AppealForm.appealTypeForm.bindFromRequest(Map("type" -> Seq("other")))

      form.hasErrors shouldBe true
    }
  }

  "Type Form: Fill" should {

    "populate some" in {
      val form = AppealForm.appealTypeForm.fill(AppealType.REVIEW)

      form.hasErrors shouldBe false
      form.data      shouldBe Map("type" -> "REVIEW")
    }
  }

}
