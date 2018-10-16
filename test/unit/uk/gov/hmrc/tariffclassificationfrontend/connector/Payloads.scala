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


import java.time.ZonedDateTime

import play.api.libs.json.{Json, OWrites, Writes}
import uk.gov.hmrc.tariffclassificationfrontend.models._

object Payloads {
  implicit val eventDetailsWrites: OWrites[Details] = Json.writes[Details]
  implicit val eventWrites: OWrites[Event] = Json.writes[Event]
  implicit val attachmentWrites: OWrites[Attachment] = Json.writes[Attachment]
  implicit val eoriDetailsWrites: OWrites[EORIDetails] = Json.writes[EORIDetails]
  implicit val contactWrites: OWrites[Contact] = Json.writes[Contact]
  implicit val appealWrites: OWrites[Appeal] = Json.writes[Appeal]
  implicit val decisionWrites: OWrites[Decision] = Json.writes[Decision]
  implicit val applicationWrites: OWrites[Application] = Json.writes[Application]
  implicit val caseWrites: OWrites[Case] = Json.writes[Case]

  private val eoriDetailsExample = EORIDetails("eori", "trader-name", "line1", "line2", "line3", "postcode", "country")
  private val contactExample = Contact("name", "email", "phone")
  private val applicationExample = Application(eoriDetailsExample, contactExample, "BTI", None, false, "Laptop", "Personal Computer", None, None, None, None, None, None, false, false, "liability-status", "liability-port", "liability-entry-number", ZonedDateTime.now())
  private val caseExample = Case("1", "OPEN", ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, "gateway", applicationExample, None, Seq())


  val gatewayCases: String = jsonOf(Seq(caseExample))

  private def jsonOf[T](obj: T)(implicit tjs : Writes[T]): String = {
    Json.toJson(obj).toString()
  }

}
