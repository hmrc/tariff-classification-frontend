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
import models.forms.FormConstraints._
import models.forms.FormUtils._
import play.api.data.Form
import play.api.data.Forms._
object ReportsFilterForm {
  val form: Form[ReportsFilter] = Form(
    mapping(
      "status"    -> set(textTransformingTo(PseudoCaseStatus.withName, (x: PseudoCaseStatus) => x.toString)),
      "caseType"  -> set(textTransformingTo(PseudoApplicationType.withName, (x: PseudoApplicationType) => x.toString)),
      "caseQueue" -> set(text),
      "officer"   -> set(text)
    )(ReportsFilter.apply)(ReportsFilter.unapply)
  )
}
