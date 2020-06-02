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

package models.forms

import uk.gov.hmrc.play.test.UnitSpec
import models.{CancelReason, ModelsBaseSpec, RulingCancellation}

class CancelRulingFormTest extends ModelsBaseSpec {

  "Bind from request" should {
    "Bind empty should succeed" in {
      val form = CancelRulingForm.form.bindFromRequest(Map())

      form.hasErrors shouldBe true
    }

    "Bind blank should fail" in {
      val form = CancelRulingForm.form.bindFromRequest(Map("reason" -> Seq("")))

      form.hasErrors shouldBe true
    }

    "Bind valid enum and note should succeed" in {
      val form = CancelRulingForm.form.bindFromRequest(Map("reason" -> Seq(CancelReason.ANNULLED.toString),"note" -> Seq("hi")))

      form.hasErrors shouldBe false
    }

    "Bind invalid enum should fail" in {
      val form = CancelRulingForm.form.bindFromRequest(Map("reason" -> Seq("other")))

      form.hasErrors shouldBe true
    }
  }

  "Fill" should {

    "populate value" in {
      val form = CancelRulingForm.form.fill(RulingCancellation(CancelReason.INVALIDATED_WRONG_CLASSIFICATION.toString, "some note text"))

      form.hasErrors shouldBe false
      form.data shouldBe Map("reason" -> "INVALIDATED_WRONG_CLASSIFICATION", "note" -> "some note text")
    }
  }



}
