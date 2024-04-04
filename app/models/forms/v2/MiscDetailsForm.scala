/*
 * Copyright 2024 HM Revenue & Customs
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

  def miscDetailsForm(existingMisc: Case): Form[Case] =
    Form[Case](
      mapping[Case, String, String, Option[String], String, Option[String]](
        "summary"             -> textNonEmpty("Enter a short case description"),
        "contactName"         -> textNonEmpty("Enter a case contact name"),
        "detailedDescription" -> optional(text),
        "caseType"            -> oneOf("error.empty.miscCaseType", MiscCaseType),
        "boardsFileNumber"    -> optional(text)
      )(form2Misc(existingMisc))(misc2Form)
    ).fillAndValidate(existingMisc)

  private def form2Misc(existingCase: Case): (
    String,
    String,
    Option[String],
    String,
    Option[String]
  ) => Case = {
    case (
        summary,
        contactName,
        detailedDescription,
        caseType,
        boardsFileNumber
        ) =>
      existingCase.copy(
        caseBoardsFileNumber = boardsFileNumber,
        application = existingCase.application.asMisc.copy(
          name                = summary,
          contactName         = Some(contactName),
          detailedDescription = detailedDescription,
          caseType            = MiscCaseType.withName(caseType)
        )
      )
  }

  private def misc2Form(existingCase: Case): Option[
    (
      String,
      String,
      Option[String],
      String,
      Option[String]
    )
  ] = {
    val existingMisc = existingCase.application.asMisc

    Some(
      (
        existingMisc.name,
        existingMisc.contactName.getOrElse(""),
        existingMisc.detailedDescription,
        existingMisc.caseType.toString,
        existingCase.caseBoardsFileNumber
      )
    )
  }
}
