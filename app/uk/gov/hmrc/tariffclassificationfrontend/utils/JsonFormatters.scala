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
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest

object JsonFormatters {

  implicit val caseStatusFormat = EnumJson.format(CaseStatus)
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
  implicit val statusFormat = Json.format[Status]
  implicit val formatCaseStatusChange = Json.format[CaseStatusChange]
  implicit val formatNote = Json.format[Note]

  implicit val formatEventDetail = Union.from[Details]("type")
    .and[CaseStatusChange](EventType.CASE_STATUS_CHANGE.toString)
    .and[Note](EventType.NOTE.toString)
    .format
  implicit val eventFormat = Json.format[Event]
  implicit val newEventRequestFormat = Json.format[NewEventRequest]

  implicit val emailCompleteParamsFormat = Json.format[CaseCompletedEmailParameters]
  implicit val emailCompleteFormat = Json.format[CaseCompletedEmail]
  implicit val emailFormat = Union.from[Email[_]]("templateId")
    .and[CaseCompletedEmail]("digital_tariffs_case_completed")
    .format
}

object EnumJson {

  implicit def format[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(Reads.enumNameReads(enum), Writes.enumNameWrites)
  }

}
