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

package models.forms

import models.forms.mappings.FormMappings
import play.api.data.Form
import play.api.data.Forms._

object KeywordForm {
  val form: Form[String] = Form(
    mapping(
      "keyword" -> FormMappings.textNonEmpty("error.empty.keyword")
    )(identity)(Some(_))
  )

  def formWithAuto(allKeywords: Seq[String]): Form[String] = Form(
    mapping(
      "keyword" -> FormMappings
        .textNonEmpty("management.create-keyword.error.empty.keyword")
        .verifying(
          "management.create-keyword.error.duplicate.keyword",
          keyword => !allKeywords.contains(keyword.toUpperCase)
        )
    )(identity)(Some(_))
  )

  def formWithAutoReverse(allKeywords: Seq[String]): Form[String] = Form(
    mapping(
      "keyword" -> FormMappings
        .textNonEmpty("management.manage-keywords.edit-approved-keywords.empty.keyword")
        .verifying(
          "management.manage-keywords.edit-approved-keywords.error",
          keyword => allKeywords.contains(keyword.toUpperCase)
        )
    )(identity)(Some(_))
  )

}
