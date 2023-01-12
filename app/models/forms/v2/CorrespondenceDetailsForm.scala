/*
 * Copyright 2023 HM Revenue & Customs
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

object CorrespondenceDetailsForm extends Constraints {

  def correspondenceDetailsForm(existingCorrespondence: Case): Form[Case] =
    Form[Case](
      mapping[Case, String, String, Option[String], Option[String]](
        "summary"             -> textNonEmpty("Enter a case description"),
        "detailedDescription" -> text,
        "boardsFileNumber"    -> optional(text),
        "relatedBTIReference" -> optional(text)
      )(form2Correspondence(existingCorrespondence))(correspondence2Form)
    ).fillAndValidate(existingCorrespondence)

  private def form2Correspondence(existingCase: Case): (
    String,
    String,
    Option[String],
    Option[String]
  ) => Case = {
    case (
        summary,
        detailedDescription,
        boardsFileNumber,
        relatedBTIReference
        ) =>
      existingCase.copy(
        caseBoardsFileNumber = boardsFileNumber,
        application = existingCase.application.asCorrespondence.copy(
          summary             = summary,
          detailedDescription = detailedDescription,
          relatedBTIReference = relatedBTIReference
        )
      )
  }

  private def correspondence2Form(existingCase: Case): Option[
    (
      String,
      String,
      Option[String],
      Option[String]
    )
  ] = {
    val existingCorrespondence = existingCase.application.asCorrespondence

    Some(
      (
        existingCorrespondence.summary,
        existingCorrespondence.detailedDescription,
        existingCase.caseBoardsFileNumber,
        existingCorrespondence.relatedBTIReference
      )
    )
  }
}
