/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.forms

import play.api.data.Mapping
import uk.gov.hmrc.tariffclassificationfrontend.forms.mappings.FormMappings.fieldNonEmpty

import scala.util.Try

object FormUtils {

  def textTransformingTo[A](reader: String => A, writer: A => String, errorKey: String = "error.empty.default"): Mapping[A] = {
    fieldNonEmpty(errorKey)
      .verifying("Invalid entry", s => Try(reader(s)).isSuccess)
      .transform[A](reader, writer)
  }

}
