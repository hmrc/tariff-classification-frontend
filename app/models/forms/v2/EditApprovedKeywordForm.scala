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

import models.forms.mappings.FormMappings.oneOf
import models.forms.v2.EditKeywordAction.EditKeywordAction
import play.api.data.Form
import play.api.data.Forms.{text, tuple}
import play.api.data.validation._

object EditKeywordAction extends Enumeration {
  type EditKeywordAction = Value
  val DELETE, RENAME = Value
}

object EditApprovedKeywordForm {
  def nonExistingKeyword(allKeywords: Seq[String]): Constraint[(EditKeywordAction, String)] =
    Constraint {
      case (EditKeywordAction.DELETE, _) => Valid
      case (EditKeywordAction.RENAME, name: String) if allKeywords.contains(name) =>
        Invalid("management.create-keyword.error.duplicate.keyword")
      case _ => Valid
    }

  val nonEmptyKeyword: Constraint[(EditKeywordAction, String)] = Constraint {
    case (EditKeywordAction.DELETE, _)                             => Valid
    case (EditKeywordAction.RENAME, name: String) if name.nonEmpty => Valid
    case _                                                         => Invalid("management.create-keyword.error.empty.keyword")
  }

  def formWithAuto(allKeywords: Seq[String]): Form[(EditKeywordAction, String)] = Form(
    tuple(
      "action" -> oneOf("error.empty.action", EditKeywordAction)
        .transform[EditKeywordAction](EditKeywordAction.withName, _.toString),
      "keywordName" -> text
    ).verifying(nonEmptyKeyword, nonExistingKeyword(allKeywords))
  )

}