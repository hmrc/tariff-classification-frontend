/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.tariffclassificationfrontend.utils

import play.api.libs.json.Json
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.caseFormat

object CasePayloads {

  val btiCase: String = jsonOf(Cases.btiCaseExample)
  val simpleBtiCase: String = jsonOf(Cases.simpleCaseExample)
  val gatewayCases: String = jsonOf(Seq(Cases.btiCaseExample))

  def jsonOf(obj: Case): String = {
    Json.toJson(obj).toString()
  }

  def jsonOf(obj: Seq[Case]): String = {
    Json.toJson(obj).toString()
  }

}
