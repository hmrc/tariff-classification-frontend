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

import models.forms.mappings.FormMappings.{oneOf, textNonEmpty}
import models.{CaseRejection, RejectReason}
import play.api.data.Form
import play.api.data.Forms.mapping

object RejectCaseForm {

  lazy val form: Form[CaseRejection] = Form(
    mapping(
      "reason" -> oneOf("error.empty.reject.reason", RejectReason)
        .transform[RejectReason.Value](RejectReason.withName, _.toString),
      "note" -> textNonEmpty("error.empty.reject.note")
    )(CaseRejection.apply)(o => Some(Tuple.fromProductTyped(o)))
  )

}
