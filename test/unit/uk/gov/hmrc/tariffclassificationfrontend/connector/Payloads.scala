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

package unit.uk.gov.hmrc.tariffclassificationfrontend.connector


import java.time.LocalDate

import play.api.libs.json.{Json, OWrites, Writes}
import uk.gov.hmrc.tariffclassificationfrontend.models.Case

object Payloads {

  implicit val writes: OWrites[Case] = Json.writes[Case]

  val gatewayCases: String = jsonOf(Seq(Case(
    "123", "description", "trader-name", LocalDate.of(2018, 1, 1), "status", "type", 1
  )))

  private def jsonOf[T](obj: T)(implicit tjs : Writes[T]): String = {
    Json.toJson(obj).toString()
  }

}
