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

package models.forms.v2

import models.Keyword
import models.forms.mappings.FormMappings.oneOf
import models.forms.v2.EditKeywordAction.EditKeywordAction
import play.api.data.Forms.{of, tuple}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object EditKeywordAction extends Enumeration {
  type EditKeywordAction = Value
  val DELETE, RENAME = Value
}

object EditApprovedKeywordForm {
  private def keyWordFormat(allKeywords: Seq[Keyword]): Formatter[String] =
    new Formatter[String] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {

        val keywordName = data.getOrElse(key, "")

        data.get("action") match {
          case None => Right(keywordName)
          case Some(action) if EditKeywordAction.DELETE.toString == action =>
            Right(action)
          case Some(action) if EditKeywordAction.RENAME.toString == action && keywordName.isEmpty =>
            Left(Seq(FormError(key, "management.manage-keywords.edit-approved-keywords.empty.keyword.renamed")))
          case Some(_) if allKeywords.map(_.name).contains(keywordName) =>
            if (allKeywords.filter(_.name == keywordName).head.approved) {
              Left(Seq(FormError(key, "management.create-keyword.error.duplicate.keyword")))
            } else {
              Left(Seq(FormError(key, "management.create-keyword.error.rejected.keyword")))
            }
          case _ => Right(keywordName)
        }
      }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    }

  def formWithAuto(allKeywords: Seq[Keyword]): Form[(EditKeywordAction, String)] = Form(
    tuple(
      "action" -> oneOf("error.empty.action", EditKeywordAction)
        .transform[EditKeywordAction](EditKeywordAction.withName, _.toString),
      "keywordName" -> of[String](keyWordFormat(allKeywords))
    )
  )

}
