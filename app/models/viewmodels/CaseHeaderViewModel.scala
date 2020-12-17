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

import models._

case class CaseHeaderViewModel(
  caseType: String,
  businessName: Option[String],
  goodsName: String,
  referenceNumber: String,
  caseStatus: CaseStatusViewModel
)

object CaseHeaderViewModel {
  def fromCase(c: Case): CaseHeaderViewModel = {
    CaseHeaderViewModel(
      c.application.`type`.prettyName,
      c.application.businessName,
      c.application.goodsName,
      c.reference,
      CaseStatusViewModel.fromCase(c)
    )
  }
}
