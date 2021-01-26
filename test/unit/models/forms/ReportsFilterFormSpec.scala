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

import models.PseudoApplicationType.PseudoApplicationType
import models.PseudoCaseStatus.PseudoCaseStatus
import models._
import models.forms.FormUtils.textTransformingTo
import play.api.data.Forms.{set, text}
import utils.Cases

class ReportsFilterFormSpec extends ModelsBaseSpec {

  private val correspondenceCase = Cases.correspondenceCaseExample
  private val sampleEmptyCase    = Cases.correspondenceCaseExample.copy(application = Cases.corrExampleWithMissingFields)

  private val emptyCaseWithEmail = sampleEmptyCase.copy(
    application = Cases.corrExampleWithMissingFields.copy(contact = Contact("name", "valid@email", Some("123")))
  )

  private val emptyCaseWithInvalidEmail = correspondenceCase.copy(
    application = Cases.corrExampleWithMissingFields
      .copy(correspondenceStarter = Some("case-source"), contact = Contact("name", "email", Some("123")))
  )

  private val emptyFilters = ReportsFilter(Set.empty, Set.empty, Set.empty, Set.empty)

  private val params = Map(
    "status"    -> Seq("OPEN"),
    "caseType"  -> Seq("LIABILITY"),
    "caseQueue" -> Seq("queue"),
    "officer"   -> Seq("officer")
  )

  "ReportsFilterForm" should {
    "Bind valid form" in {
      ReportsFilterForm.form
        .fillAndValidate(emptyFilters)
        .fold(
          form => form.hasErrors shouldBe false,
          _ => "form should not have errors"
        )
    }

  }
}
