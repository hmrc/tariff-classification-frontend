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

import play.api.data.Forms._
import play.api.data.Form

import models.forms.FormConstraints.defined

object MandatoryBooleanForm {

  def form(key: String = "errors"): Form[Boolean] = Form[Boolean](
    mapping[Boolean, Boolean](
      // Booleans aren't mandatory by default - Have to do similar to the below to enforce it is submitted
      "state" -> optional(boolean).verifying(defined(s"$key.form.state.required")).transform(_.get, Some(_))
    )(identity)(Some(_))
  )

}
