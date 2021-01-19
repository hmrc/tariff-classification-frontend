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
import models.PseudoReportColumns.PseudoReportColumns
import models._
import models.forms.FormConstraints._
import models.forms.FormUtils._
import play.api.data.Form
import play.api.data.Forms._

object ReportSettingsForm {

  val form: Form[ReportSettings] = Form(
    mapping(
      "dates" -> optional[ReportDates](set(textTransformingTo(PseudoCaseStatus.withName, _.toString))),
      "grouping" -> PseudoGroupingType(
        textTransformingTo(PseudoGroupingType.withName, _.toString)
      ),
      "columns" -> optional[Set[PseudoReportColumns]](set(textTransformingTo(PseudoReportColumns.withName, _.toString))
    )(ReportSettings.apply)(ReportSettings.unapply)
  )

}
