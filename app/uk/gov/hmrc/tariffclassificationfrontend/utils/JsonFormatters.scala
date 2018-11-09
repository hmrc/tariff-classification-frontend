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

package uk.gov.hmrc.tariffclassificationfrontend.utils

import play.api.libs.json._
import uk.gov.hmrc.play.json.Union
import uk.gov.hmrc.tariffclassificationfrontend.models._

object JsonFormatters {

  implicit val caseStatusFormat = EnumJson.format(CaseStatus)
  implicit val caseStatusChangeFormat = Json.format[CaseStatusChange]
  implicit val noteFormat = Json.format[Note]
  implicit val eventDetailsFormat = Union.from[Details]("type")
    .and[CaseStatusChange]("CASE_STATUS_CHANGE")
    .and[Note]("NOTE")
    .format
  implicit val eventFormat = Json.format[Event]
  implicit val attachmentFormat = Json.format[Attachment]
  implicit val appealFormat = Json.format[Appeal]
  implicit val contactFormat = Json.format[Contact]
  implicit val eoriDetailsFormat = Json.format[EORIDetails]
  implicit val decisionFormat = Json.format[Decision]
  implicit val liabilityOrderFormat = Json.format[LiabilityOrder]
  implicit val btiApplicationFormat = Json.format[BTIApplication]
  implicit val applicationFormat = Union.from[Application]("type")
    .and[BTIApplication]("BTI")
    .and[LiabilityOrder]("LIABILITY_ORDER")
    .format
  implicit val caseFormat = Json.format[Case]

}

object EnumJson {

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) =>
        try {
          JsSuccess(enum.withName(s))
        } catch {
          case _: NoSuchElementException =>
            throw new InvalidEnumException(enum.getClass.getSimpleName, s)
        }
      case _ => JsError("String value expected")
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  implicit def format[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }

}

class InvalidEnumException(className: String, input: String)
  extends RuntimeException(s"Enumeration expected of type: '$className', but it does not contain '$input'")
