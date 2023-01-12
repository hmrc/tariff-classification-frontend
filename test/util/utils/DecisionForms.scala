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

package utils

import models.forms.DecisionFormData

object DecisionForms {

  val validForm: DecisionFormData = DecisionFormData(
    "binding commodity code test",
    "valid goods Description",
    "valid method Search",
    "valid justification",
    "valid method Commercial Denomination",
    "valid method Exclusion",
    List.empty
  )

  val validFormWithAttachments: DecisionFormData =
    DecisionFormData(
      attachments = Seq("url.to.publish")
    )
}
