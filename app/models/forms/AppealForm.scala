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

package models.forms

import play.api.data.{Form, Forms, Mapping}
import models.forms.FormUtils.textTransformingTo
import models.AppealStatus.AppealStatus
import models.AppealType.AppealType
import models.{AppealStatus, AppealType}

object AppealForm {

  private val appealStatusMapping: Mapping[AppealStatus] = Forms.mapping[AppealStatus, AppealStatus](
    "status" -> textTransformingTo(AppealStatus.withName, _.toString, "error.empty.appealStatus")
  )(identity)(Some(_))

  val appealStatusForm: Form[AppealStatus] = Form[AppealStatus](appealStatusMapping)

  private val appealTypeMapping: Mapping[AppealType] = Forms.mapping[AppealType, AppealType](
    "type" -> textTransformingTo(AppealType.withName, _.toString, "error.empty.appealType")
  )(identity)(Some(_))

  val appealTypeForm: Form[AppealType] = Form[AppealType](appealTypeMapping)

}
