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

package models.viewmodels

import models.Case
import utils.Dates

case class C592ViewModel(entryNumber: String, entryDate: String, btiCase: String )

object C592ViewModel {
  def fromCase(c: Case): C592ViewModel = {
    val liabilityOrder = c.application.asLiabilityOrder
    C592ViewModel(liabilityOrder.entryNumber.getOrElse(""), liabilityOrder.entryDate.map(Dates.format).getOrElse(""), "" )
  }

}
