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

import models._
import models.forms.mappings.Constraints
import models.forms.mappings.FormMappings._
import play.api.data.Form
import play.api.data.Forms._

object MiscDetailsForm extends Constraints {

  def miscDetailsForm(existingCorrespondence: Case): Form[Case] =
    Form[Case](
      mapping[
        Case,
        String,
        String,
        String
      ](
        "summary"             -> textNonEmpty("Enter a summary"),
        "detailedDescription" -> text,
        "caseType"            -> oneOf("error.empty.miscCaseType", MiscCaseType)
      )(form2Misc(existingCorrespondence))(misc2Form)
    ).fillAndValidate(existingCorrespondence)

  private def form2Misc(existingCase: Case): (
    String,
    String,
    String
  ) => Case = {
    case (
        summary,
        detailedDescription,
        caseType
        ) =>
      existingCase.copy(
        application = existingCase.application.asMisc.copy(
          name                = summary,
          detailedDescription = Some(detailedDescription),
          caseType            = MiscCaseType.withName(caseType)
        )
      )
  }

  private def misc2Form(existingCase: Case): Some[(String, Option[String], String)] = {
    val existingCorrespondence = existingCase.application.asMisc

    Some(
      (
        existingCorrespondence.name,
        existingCorrespondence.detailedDescription,
        existingCorrespondence.caseType.toString
      )
    )
  }
}
