/*
 * Copyright 2018 HM Revenue & Customs
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

package unit.uk.gov.hmrc.tariffclassificationfrontend.utils

import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters._

object CasePayloads {

  val btiCase: String = jsonOf(CaseExamples.caseExample)
  val gatewayCases: String = jsonOf(Seq(CaseExamples.caseExample))

  private def jsonOf[T](obj: T)(implicit tjs : Writes[T]): String = {
    Json.toJson(obj).toString()
  }

}
